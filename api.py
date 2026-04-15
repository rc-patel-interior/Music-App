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
    r'|\s*ft\..*$',
    re.IGNORECASE
)

def clean_title(raw: str) -> str:
    t = raw.split('|')[0].strip()
    t = _NOISE.sub('', t)
    return t.strip(' -|').strip()

def parse_artist(raw: str) -> str:
    parts = raw.split('|')
    if len(parts) >= 2:
        return parts[1].strip()
    if ' - ' in parts[0]:
        segments = parts[0].split(' - ', 1)
        candidate = segments[1].split('(')[0].strip()
        if len(candidate) < 60:
            return candidate
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
        url = entry.get('url', '') or ''
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
    'quiet': True,
    'no_warnings': True,
    'extract_flat': True,
    'skip_download': True,
}
_STREAM_OPTS = {
    'quiet': True,
    'no_warnings': True,
    'format': 'bestaudio[ext=m4a]/bestaudio/best',
    'skip_download': True,
}

def yt_search(query: str, count: int = 15) -> list:
    key = f's:{query}:{count}'
    cached = cache_get(key)
    if cached is not None:
        return cached
    try:
        with yt_dlp.YoutubeDL(_FLAT_OPTS) as ydl:
            info = ydl.extract_info(f'ytsearch{count}:{query}', download=False)
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
            info = ydl.extract_info(f'https://youtube.com/watch?v={vid_id}', download=False)
            raw = info.get('title', '')
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
    'hindi':         'new hindi songs 2024 audio',
    'punjabi':       'new punjabi songs 2024 audio',
    'bollywood':     'bollywood hits 2024 audio',
    'romantic':      'romantic hindi songs audio 2024',
    'international': 'top english pop songs 2024 audio',
}

# ─── API routes ───────────────────────────────────────────────────────────────
@app.route('/api/search')
def api_search():
    q = request.args.get('q', '').strip()
    if not q:
        return jsonify([])
    tracks = yt_search(f'{q} audio', 15)
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
        opts = dict(_FLAT_OPTS)
        with yt_dlp.YoutubeDL(opts) as ydl:
            info = ydl.extract_info(f'https://youtube.com/watch?v={vid_id}', download=False)
            raw_title = info.get('title', '')
            entries = info.get('entries') or []
            tracks = [t for t in (entry_to_track(e) for e in entries[:12]) if t]
            if not tracks:
                search_q = clean_title(raw_title) + ' audio'
                tracks = yt_search(search_q, 10)
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
