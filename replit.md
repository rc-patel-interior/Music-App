# ANNIE MUSIC

An Android music streaming application built with Kotlin and Jetpack Compose.

## Project Overview

ANNIE MUSIC is a native Android app that allows users to search for music, browse trending tracks (organized by categories like Hindi, Punjabi, Bollywood, etc.), and stream audio.

## Tech Stack

- **Language:** Kotlin 1.9.0
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM with Hilt Dependency Injection
- **Networking:** Retrofit + Moshi + OkHttp
- **Media Playback:** Media3 ExoPlayer + MediaSession (background service)
- **Image Loading:** Coil
- **Dynamic Colors:** Palette API (extracts colors from album art)
- **Concurrency:** Kotlin Coroutines

## Build System

- **Build Tool:** Gradle (Groovy DSL)
- **Android Gradle Plugin:** 8.2.2
- **Target SDK:** 34 (Android 14)
- **Min SDK:** 24 (Android 7.0)

## Project Structure

```
app/src/main/java/com/annie/music/
├── api/          # Retrofit interfaces, data models (Moshi), NetworkModule
├── data/         # MusicRepository
├── media/        # MusicService (MediaSessionService), MediaModule
├── ui/           # MainActivity, MusicViewModel, MainScreen
│   ├── screens/  # HomeScreen, SearchScreen, LibraryScreen, FullPlayerScreen
│   └── theme/    # Color, Theme definitions
└── AnnieMusicApp.kt  # Hilt Application class
```

## API

Backend: `https://annie.qzz.io/`
- `GET /api/search?q=<query>` — search tracks
- `GET /api/trending` — get trending tracks by category
- `GET /api/suggested?v=<videoId>` — get suggested tracks
- `GET /api/stream?v=<videoId>` — get stream URL
- `GET /api/download?v=<videoId>` — get download URL

## Replit Environment

Since Replit does not support an Android emulator, a Node.js web server (`server.js`) is configured to run on port 5000 and display an informational landing page about the app.

### Workflow
- **Start application**: `node server.js` (port 5000)
