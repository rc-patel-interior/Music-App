const http = require('http');

const html = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>ANNIE MUSIC</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body {
      font-family: 'Segoe UI', system-ui, sans-serif;
      background: #0d0d0d;
      color: #fff;
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
    }
    .container {
      max-width: 700px;
      width: 90%;
      text-align: center;
      padding: 3rem 2rem;
    }
    .logo {
      width: 120px;
      height: 120px;
      background: linear-gradient(135deg, #e91e63, #9c27b0);
      border-radius: 28px;
      margin: 0 auto 2rem;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 3rem;
      box-shadow: 0 8px 32px rgba(233, 30, 99, 0.4);
    }
    h1 {
      font-size: 2.8rem;
      font-weight: 800;
      letter-spacing: 3px;
      background: linear-gradient(135deg, #e91e63, #9c27b0);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin-bottom: 1rem;
    }
    .tagline {
      font-size: 1.1rem;
      color: #aaa;
      margin-bottom: 3rem;
      line-height: 1.6;
    }
    .features {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 1.2rem;
      margin-bottom: 3rem;
    }
    .feature-card {
      background: #1a1a1a;
      border: 1px solid #2a2a2a;
      border-radius: 16px;
      padding: 1.5rem;
      transition: transform 0.2s, border-color 0.2s;
    }
    .feature-card:hover {
      transform: translateY(-4px);
      border-color: #e91e63;
    }
    .feature-icon { font-size: 2rem; margin-bottom: 0.75rem; }
    .feature-title {
      font-size: 0.9rem;
      font-weight: 600;
      color: #e0e0e0;
      margin-bottom: 0.4rem;
    }
    .feature-desc { font-size: 0.8rem; color: #888; line-height: 1.4; }
    .tech-section { margin-bottom: 2rem; }
    .tech-section h2 { font-size: 1rem; color: #666; margin-bottom: 1rem; letter-spacing: 2px; text-transform: uppercase; }
    .tags {
      display: flex;
      flex-wrap: wrap;
      gap: 0.6rem;
      justify-content: center;
    }
    .tag {
      background: #1a1a1a;
      border: 1px solid #333;
      padding: 0.35rem 0.85rem;
      border-radius: 50px;
      font-size: 0.8rem;
      color: #ccc;
    }
    .android-badge {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      background: linear-gradient(135deg, #3ddc84, #00bfa5);
      color: #000;
      font-weight: 700;
      padding: 0.75rem 2rem;
      border-radius: 50px;
      font-size: 0.95rem;
      margin-top: 1rem;
    }
    .api-info {
      margin-top: 2rem;
      padding: 1rem 1.5rem;
      background: #111;
      border-radius: 12px;
      border: 1px solid #222;
      font-size: 0.82rem;
      color: #555;
    }
    .api-info span { color: #e91e63; }
  </style>
</head>
<body>
  <div class="container">
    <div class="logo">🎵</div>
    <h1>ANNIE MUSIC</h1>
    <p class="tagline">A modern Android music streaming app built with Kotlin &amp; Jetpack Compose.<br>Discover, search, and stream music across trending categories.</p>

    <div class="features">
      <div class="feature-card">
        <div class="feature-icon">🔍</div>
        <div class="feature-title">Search</div>
        <div class="feature-desc">Search millions of tracks instantly</div>
      </div>
      <div class="feature-card">
        <div class="feature-icon">🔥</div>
        <div class="feature-title">Trending</div>
        <div class="feature-desc">Browse Hindi, Punjabi, Bollywood &amp; more</div>
      </div>
      <div class="feature-card">
        <div class="feature-icon">▶️</div>
        <div class="feature-title">Playback</div>
        <div class="feature-desc">Background audio with Media3 ExoPlayer</div>
      </div>
      <div class="feature-card">
        <div class="feature-icon">🎨</div>
        <div class="feature-title">Dynamic UI</div>
        <div class="feature-desc">Adaptive colors from album art via Palette</div>
      </div>
    </div>

    <div class="tech-section">
      <h2>Tech Stack</h2>
      <div class="tags">
        <span class="tag">Kotlin</span>
        <span class="tag">Jetpack Compose</span>
        <span class="tag">Material 3</span>
        <span class="tag">Hilt DI</span>
        <span class="tag">Retrofit</span>
        <span class="tag">Media3 ExoPlayer</span>
        <span class="tag">Coil</span>
        <span class="tag">Coroutines</span>
        <span class="tag">MVVM</span>
        <span class="tag">Palette API</span>
      </div>
    </div>

    <div class="android-badge">
      <span>🤖</span> Android App (Min SDK 24 · Target SDK 34)
    </div>

    <div class="api-info">
      Powered by <span>annie.qzz.io</span> — music search, trending &amp; streaming API
    </div>
  </div>
</body>
</html>`;

const server = http.createServer((req, res) => {
  res.writeHead(200, { 'Content-Type': 'text/html' });
  res.end(html);
});

const PORT = 5000;
server.listen(PORT, '0.0.0.0', () => {
  console.log(`ANNIE MUSIC info page running at http://0.0.0.0:${PORT}`);
});
