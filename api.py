import os
import json
import threading
import time
from concurrent.futures import ThreadPoolExecutor
from flask import Flask, jsonify, request, send_file, Response
from flask_cors import CORS
import yt_dlp

app = Flask(__name__)
CORS(app)

# Cache with TTL
_cache = {}
_cache_lock = threading.Lock()

def cache_get(key):
    with _cache_lock:
        entry = _cache.get(key)
        if entry and time.time() < entry['expires']:
            return entry['value']
    return None

def cache_set(key, value, ttl=3600):
    with _cache_lock:
        _cache[key] = {'value': value, 'expires': time.time() + ttl}

def make_thumbnail(video_id, thumbnails=None):
    if thumbnails:
        best = sorted(thumbnails, key=lambda t: t.get('width', 0), reverse=True)
        if best:
            return best[0].get('url')
    return f"https://i.ytimg.com/vi/{video_id}/hqdefault.jpg"

def entry_to_track(entry):
    if not entry:
        return None
    video_id = entry.get('id') or entry.get('url', '').replace('https://www.youtube.com/watch?v=', '')
    if not video_id:
        return None
    return {
        "v": video_id,
        "title": entry.get('title', 'Unknown'),
        "thumbnail": make_thumbnail(video_id, entry.get('thumbnails')),
        "duration": str(int(entry.get('duration') or 0)),
        "url": None
    }

def yt_search(query, count=15):
    cached = cache_get(f"s:{query}")
    if cached is not None:
        return cached
    opts = {
        'quiet': True,
        'no_warnings': True,
        'extract_flat': True,
        'skip_download': True,
    }
    try:
        with yt_dlp.YoutubeDL(opts) as ydl:
            info = ydl.extract_info(f"ytsearch{count}:{query}", download=False)
            tracks = [t for t in (entry_to_track(e) for e in info.get('entries', [])) if t]
            cache_set(f"s:{query}", tracks, ttl=1800)
            return tracks
    except Exception as e:
        print(f"[search] {query}: {e}")
        return []

def yt_stream(video_id):
    cached = cache_get(f"st:{video_id}")
    if cached is not None:
        return cached
    opts = {
        'quiet': True,
        'no_warnings': True,
        'format': 'bestaudio[ext=m4a]/bestaudio/best[height<=480]/best',
        'skip_download': True,
    }
    try:
        with yt_dlp.YoutubeDL(opts) as ydl:
            info = ydl.extract_info(f"https://youtube.com/watch?v={video_id}", download=False)
            result = {
                "title": info.get('title', ''),
                "thumbnail": make_thumbnail(video_id, info.get('thumbnails')),
                "url": info.get('url', '')
            }
            cache_set(f"st:{video_id}", result, ttl=3600)
            return result
    except Exception as e:
        print(f"[stream] {video_id}: {e}")
        return None

# Trending category queries
TRENDING_QUERIES = {
    "hindi":         "new hindi songs 2024",
    "punjabi":       "new punjabi songs 2024",
    "bollywood":     "bollywood hits 2024",
    "romantic":      "romantic hindi songs 2024",
    "international": "top english pop songs 2024"
}

@app.route('/api/search')
def api_search():
    q = request.args.get('q', '').strip()
    if not q:
        return jsonify([])
    tracks = yt_search(q + " audio song", 15)
    return jsonify(tracks)

@app.route('/api/trending')
def api_trending():
    cached = cache_get('trending')
    if cached is not None:
        return jsonify(cached)

    result = {}
    def fetch(cat, query):
        result[cat] = yt_search(query, 12)

    with ThreadPoolExecutor(max_workers=5) as pool:
        futures = [pool.submit(fetch, cat, q) for cat, q in TRENDING_QUERIES.items()]
        for f in futures:
            f.result()

    cache_set('trending', result, ttl=3600)
    return jsonify(result)

@app.route('/api/stream')
def api_stream():
    video_id = request.args.get('v', '').strip()
    if not video_id:
        return jsonify({'error': 'missing v'}), 400
    info = yt_stream(video_id)
    if not info:
        return jsonify({'error': 'failed to extract stream'}), 500
    return jsonify(info)

@app.route('/api/download')
def api_download():
    return api_stream()

@app.route('/api/suggested')
def api_suggested():
    video_id = request.args.get('v', '').strip()
    if not video_id:
        return jsonify([])
    cached = cache_get(f"sug:{video_id}")
    if cached is not None:
        return jsonify(cached)
    opts = {
        'quiet': True,
        'no_warnings': True,
        'extract_flat': True,
        'skip_download': True,
    }
    try:
        with yt_dlp.YoutubeDL(opts) as ydl:
            info = ydl.extract_info(f"https://youtube.com/watch?v={video_id}", download=False)
            entries = []
            for key in ['related_videos', 'entries']:
                entries = info.get(key) or []
                if entries:
                    break
            tracks = [t for t in (entry_to_track(e) for e in entries[:12]) if t]
            if not tracks:
                tracks = yt_search(info.get('title', '') + " song", 10)
            cache_set(f"sug:{video_id}", tracks, ttl=3600)
            return jsonify(tracks)
    except Exception as e:
        print(f"[suggested] {video_id}: {e}")
        return jsonify([])

# ─── Web page + APK download ─────────────────────────────────────────────────

HTML = open('server_page.html').read() if os.path.exists('server_page.html') else "<h1>ANNIE MUSIC API</h1>"

@app.route('/')
def index():
    return HTML, 200, {'Content-Type': 'text/html'}

@app.route('/download/annie-music.apk')
def apk_download():
    apk_path = os.path.join(os.path.dirname(__file__), 'public', 'annie-music.apk')
    if os.path.exists(apk_path):
        return send_file(apk_path,
                         mimetype='application/vnd.android.package-archive',
                         as_attachment=True,
                         download_name='annie-music.apk')
    return "APK not found", 404

if __name__ == '__main__':
    print("Starting ANNIE MUSIC API server on port 5000...")
    app.run(host='0.0.0.0', port=5000, threaded=True)
