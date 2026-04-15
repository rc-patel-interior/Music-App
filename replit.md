# ANNIE MUSIC

An Android music streaming application built with Kotlin and Jetpack Compose, powered by a yt-dlp backend.

## Architecture

- **Backend API**: Python Flask + yt-dlp (`api.py`) on port 5000
  - Serves the web landing page at `/`
  - Serves APK download at `/download/annie-music.apk`
  - Serves all API endpoints at `/api/*`
- **Android App**: Kotlin + Jetpack Compose, calling the Replit backend

## API Endpoints

All endpoints served by `api.py` using yt-dlp to pull live data from YouTube:

| Endpoint | Description |
|---|---|
| `GET /api/search?q=<query>` | Search YouTube, returns `List<Track>` |
| `GET /api/trending` | Trending by Hindi/Punjabi/Bollywood/Romantic/International |
| `GET /api/stream?v=<videoId>` | Get stream URL for a video |
| `GET /api/download?v=<videoId>` | Get download URL for a video |
| `GET /api/suggested?v=<videoId>` | Get suggested tracks |

### Data Models

```json
Track: { "v": "videoId", "title": "...", "thumbnail": "url", "duration": "secs" }
TrendingResponse: { "hindi": [...], "punjabi": [...], "bollywood": [...], "romantic": [...], "international": [...] }
StreamInfo: { "title": "...", "thumbnail": "url", "url": "streamUrl" }
```

## Android App Tech Stack

- **Language**: Kotlin 1.9.23
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Hilt DI
- **Networking**: Retrofit + Moshi + OkHttp
- **Media**: Media3 ExoPlayer + MediaSession (background playback)
- **Image Loading**: Coil + Palette API

## Build Configuration

- Android Gradle Plugin: 8.2.2
- Kotlin: 1.9.23 (must match Compose compiler 1.5.12 exactly)
- Compose BOM: 2024.06.00
- material-icons-extended: 1.6.8 (pinned — BOM resolution fails in CI without this)
- Target SDK: 34, Min SDK: 24

## Workflow

- **Start application**: `python api.py` (port 5000)

## APK

Built APK is at `public/annie-music.apk` (~22MB, debug build).
To rebuild after code changes, run the "Rebuild APK" workflow or use Gradle directly.

## Why the Build Was Failing on GitHub

Three compounding issues:
1. **Kotlin/Compose version mismatch**: Kotlin `1.9.0` + Compose compiler `1.5.12` is incompatible (needs Kotlin `1.9.23`)
2. **material-icons-extended not resolving**: Without an explicit version, the BOM fails to resolve the extended icons library in CI environments, causing `Unresolved reference: Pause/Download/SkipPrevious/SkipNext`
3. **Deprecated manifest attribute**: `package=` in `AndroidManifest.xml` is no longer supported
