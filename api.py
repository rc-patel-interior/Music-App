import os
import re
import threading
import time
from concurrent.futures import ThreadPoolExecutor
from flask import Flask, jsonify, request, send_file
from flask_cors import CORS
import yt_dlp

app = Flask(__name__)
CORS(app)

# ─── Cache ────────────────────────────────────────────────────────────────────
_cache: dict = {}
_cache_lock = threading.Lock()

def cache_get(key):
    with _cache_lock:
        entry = _cache.get(key)
        if entry and time.time() < entry['exp']:
            return entry['val']
    return None

def cache_set(key, val, ttl=1800):
    with _cache_lock:
        _cache[key] = {'val': val, 'exp': time.time() + ttl}

# ─── Title / Artist helpers ───────────────────────────────────────────────────
_NOISE = re.compile(
    r'[\(\[]\s*(?:official\s+)?(?:music\s+)?(?:lyric(?:s)?\s+)?'
    r'(?:video|audio|song|hd|4k|hq|official)\s*[\)\]]'
    r'|[\(\[]\s*full\s+(?:hd\s+)?song\s*[\)\]]'
    r'|[\(\[]\s*official\s*[\)\]]'
    r'|\s+-\s+official\s+(?:music\s+)?(?:video|audio)'
    r'|\s+-\s+(?:full\s+)?(?:hd\s+)?song'
    r'|\s+-\s+lyrics?'
    r'|\s*#\S+'
    r'|\s*\|\s*T-Series.*$'
    r'|\s*\|\s*\w[\w\s]*Records.*$',
    re.IGNORECASE
)
_FT = re.compile(r'\s+(?:ft|feat)\.?\s+.+$', re.IGNORECASE)

def clean_title(raw: str) -> str:
    t = raw.split('|')[0].strip()
    t = _NOISE.sub('', t)
    t = _FT.sub('', t)
    return t.strip(' -|').strip()

def parse_artist(raw: str) -> str:
    parts = raw.split('|')
    if len(parts) >= 2:
        cand = parts[1].strip()
        if len(cand) < 60:
            return cand
    p0 = parts[0]
    if ' - ' in p0:
        segs = p0.split(' - ', 1)
        candidate = segs[0].strip()
        if 3 < len(candidate) < 50 and not any(w in candidate.lower() for w in ['official', 'song', 'audio', 'video']):
            return candidate
        candidate2 = segs[1].split('(')[0].strip()
        if len(candidate2) < 60:
            return candidate2
    return ''

def make_thumb(vid_id: str, thumbs=None) -> str:
    if thumbs:
        best = sorted(thumbs, key=lambda t: t.get('width', 0) or 0, reverse=True)
        for t in best:
            url = t.get('url', '')
            if url and 'default' not in url.split('/')[-1]:
                return url
    return f'https://i.ytimg.com/vi/{vid_id}/hqdefault.jpg'

def fmt_duration(secs) -> str:
    if not secs:
        return ''
    try:
        s = int(secs)
        m, s = divmod(s, 60)
        h, m = divmod(m, 60)
        return f'{h}:{m:02}:{s:02}' if h else f'{m}:{s:02}'
    except Exception:
        return ''

def is_valid_vid_id(vid_id: str) -> bool:
    return bool(vid_id and re.match(r'^[A-Za-z0-9_-]{11}$', vid_id))

def entry_to_track(entry) -> dict | None:
    if not entry:
        return None
    vid_id = entry.get('id', '') or ''
    if not is_valid_vid_id(vid_id):
        url = entry.get('url', '') or entry.get('webpage_url', '') or ''
        m = re.search(r'(?:v=|youtu\.be/)([A-Za-z0-9_-]{11})', url)
        vid_id = m.group(1) if m else ''
    if not is_valid_vid_id(vid_id):
        return None
    raw = entry.get('title', '') or ''
    return {
        'v':         vid_id,
        'title':     clean_title(raw),
        'artist':    parse_artist(raw),
        'thumbnail': make_thumb(vid_id, entry.get('thumbnails')),
        'duration':  fmt_duration(entry.get('duration')),
        'url':       None,
    }

# ─── yt-dlp helpers ───────────────────────────────────────────────────────────
_FLAT_OPTS = {
    'quiet':        True,
    'no_warnings':  True,
    'extract_flat': True,
    'skip_download':True,
}
_STREAM_OPTS = {
    'quiet':        True,
    'no_warnings':  True,
    'format':       'bestaudio[ext=m4a]/bestaudio/best',
    'skip_download':True,
}

def yt_search(query: str, count: int = 15) -> list:
    key = f's:{query}:{count}'
    cached = cache_get(key)
    if cached is not None:
        return cached
    try:
        with yt_dlp.YoutubeDL(_FLAT_OPTS) as ydl:
            info   = ydl.extract_info(f'ytsearch{count}:{query}', download=False)
            tracks = [t for t in (entry_to_track(e) for e in info.get('entries', [])) if t]
            cache_set(key, tracks, ttl=1800)
            return tracks
    except Exception as e:
        print(f'[search] {query}: {e}')
        return []

def yt_get_stream(vid_id: str) -> dict | None:
    key = f'st:{vid_id}'
    cached = cache_get(key)
    if cached is not None:
        return cached
    try:
        with yt_dlp.YoutubeDL(_STREAM_OPTS) as ydl:
            info   = ydl.extract_info(f'https://youtube.com/watch?v={vid_id}', download=False)
            raw    = info.get('title', '')
            result = {
                'title':     clean_title(raw),
                'artist':    parse_artist(raw),
                'thumbnail': make_thumb(vid_id, info.get('thumbnails')),
                'url':       info.get('url', ''),
                'duration':  fmt_duration(info.get('duration')),
            }
            cache_set(key, result, ttl=3600)
            return result
    except Exception as e:
        print(f'[stream] {vid_id}: {e}')
        return None

# ─── Trending queries ─────────────────────────────────────────────────────────
TRENDING_QUERIES = {
    'hindi':         'new hindi songs 2025 audio',
    'punjabi':       'new punjabi songs 2025 audio',
    'bollywood':     'bollywood hits 2025 audio',
    'romantic':      'best romantic hindi songs 2025 audio',
    'international': 'top english pop songs 2025 audio',
}

# ─── API routes ───────────────────────────────────────────────────────────────
@app.route('/api/search')
def api_search():
    q = request.args.get('q', '').strip()
    if not q:
        return jsonify([])
    tracks = yt_search(f'{q} audio song', 15)
    return jsonify(tracks)

@app.route('/api/trending')
def api_trending():
    cached = cache_get('trending')
    if cached is not None:
        return jsonify(cached)
    result: dict = {}
    def fetch(cat, query):
        result[cat] = yt_search(query, 12)
    with ThreadPoolExecutor(max_workers=5) as pool:
        list(pool.map(lambda kv: fetch(*kv), TRENDING_QUERIES.items()))
    cache_set('trending', result, ttl=3600)
    return jsonify(result)

@app.route('/api/stream')
def api_stream():
    vid_id = request.args.get('v', '').strip()
    print(f'[stream] request v={repr(vid_id)}')
    if not is_valid_vid_id(vid_id):
        return jsonify({'error': f'invalid video id: {repr(vid_id)}'}), 400
    info = yt_get_stream(vid_id)
    if not info or not info.get('url'):
        return jsonify({'error': 'stream extraction failed'}), 500
    return jsonify(info)

@app.route('/api/download')
def api_download_url():
    return api_stream()

@app.route('/api/suggested')
def api_suggested():
    vid_id = request.args.get('v', '').strip()
    if not is_valid_vid_id(vid_id):
        return jsonify([])
    cached = cache_get(f'sug:{vid_id}')
    if cached is not None:
        return jsonify(cached)
    try:
        # Get the video title first to search for similar tracks
        stream_cached = cache_get(f'st:{vid_id}')
        if stream_cached and stream_cached.get('title'):
            title  = stream_cached['title']
            artist = stream_cached.get('artist', '')
        else:
            with yt_dlp.YoutubeDL({**_FLAT_OPTS, 'extract_flat': False}) as ydl:
                info   = ydl.extract_info(f'https://youtube.com/watch?v={vid_id}', download=False)
                title  = clean_title(info.get('title', ''))
                artist = parse_artist(info.get('title', ''))

        # Search for similar songs based on title and artist
        if artist:
            q1 = f'{artist} songs audio'
        else:
            q1 = f'{title} similar songs audio'

        tracks = yt_search(q1, 12)
        # Filter out the current track
        tracks = [t for t in tracks if t.get('v') != vid_id][:10]

        if not tracks:
            tracks = yt_search(f'{title} audio', 10)
            tracks = [t for t in tracks if t.get('v') != vid_id][:10]

        cache_set(f'sug:{vid_id}', tracks, ttl=3600)
        return jsonify(tracks)
    except Exception as e:
        print(f'[suggested] {vid_id}: {e}')
        return jsonify([])

# ─── Web page + APK ───────────────────────────────────────────────────────────
@app.route('/')
def index():
    try:
        html = open('server_page.html').read()
    except Exception:
        html = '<h1>ANNIE MUSIC</h1>'
    return html, 200, {'Content-Type': 'text/html'}

@app.route('/download/annie-music.apk')
def apk_download():
    path = os.path.join(os.path.dirname(__file__), 'public', 'annie-music.apk')
    if os.path.exists(path):
        return send_file(path, mimetype='application/vnd.android.package-archive',
                         as_attachment=True, download_name='annie-music.apk')
    return 'APK not found', 404

if __name__ == '__main__':
    print('Starting ANNIE MUSIC API on port 5000...')
    app.run(host='0.0.0.0', port=5000, threaded=True)
