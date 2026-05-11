# eShort - Short Video Platform

A modern Android short-video application inspired by TikTok and Instagram Reels. Users share short videos by pasting links from social media platforms.

## Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  eShort User    │     │  Firebase         │     │  eShort Admin   │
│  Android App    │◄───►│  Backend          │◄───►│  Android App    │
│  (Kotlin/       │     │  - Auth           │     │  (Kotlin/       │
│   Compose)      │     │  - Firestore      │     │   Compose)      │
└─────────────────┘     │  - Storage        │     └─────────────────┘
                        │  - Realtime DB    │
                        │  - FCM            │
                        └──────────────────┘
```

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI Framework | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt (Dagger) |
| Video Player | ExoPlayer (Media3) |
| Image Loading | Coil |
| Auth | Firebase Auth + Google Sign-In |
| Database | Cloud Firestore |
| Realtime | Firebase Realtime Database |
| Storage | Firebase Storage |
| Push | Firebase Cloud Messaging |
| Async | Kotlin Coroutines + Flow |

## Features

### User App
- **Google Sign-In** - One-tap authentication
- **TikTok-style Feed** - Vertical swipe, full-screen video playback
- **Link-Based Upload** - Paste links from TikTok, YouTube Shorts, Instagram Reels, Facebook Reels
- **Double-Tap Like** - Animated heart overlay
- **Comments System** - Real-time comments with likes
- **Real-time Chat** - 1:1 messaging with typing indicators, online status, read receipts
- **Search & Discovery** - Search users, hashtags, trending videos
- **User Profiles** - Avatar, bio, video grid, followers/following
- **Follow System** - Follow/unfollow creators
- **Push Notifications** - FCM-powered notifications
- **Dark Theme** - Premium dark UI with gradient effects

### Admin App
- **Dashboard** - Analytics overview with stat cards
- **User Management** - View, ban/unban users
- **Video Moderation** - Approve, reject, delete videos
- **Reports System** - Review and resolve user reports
- **Broadcast Notifications** - Send push notifications to all users

## Setup

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Firebase project with enabled services

### Firebase Setup
1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Enable Authentication (Google Sign-In)
3. Create Firestore Database
4. Enable Firebase Storage
5. Enable Realtime Database
6. Download `google-services.json` and place in `app/` directory
7. Deploy security rules from `firebase/` directory

### Build
```bash
# User App
cd eshort-user
./gradlew assembleDebug

# Admin App
cd eshort-admin
./gradlew assembleDebug
```

## Project Structure

```
eshort-user/
├── app/src/main/java/com/eshort/app/
│   ├── di/                  # Hilt dependency injection
│   ├── data/
│   │   ├── model/           # Data classes (User, Video, Chat, etc.)
│   │   └── repository/      # Firebase repositories
│   ├── viewmodel/           # MVVM ViewModels
│   ├── ui/
│   │   ├── theme/           # Dark theme, colors, typography
│   │   └── screens/         # Compose screens
│   │       ├── splash/      # Animated splash screen
│   │       ├── auth/        # Google Sign-In
│   │       ├── home/        # TikTok-style video feed
│   │       ├── search/      # Search & discovery
│   │       ├── upload/      # Link-based video upload
│   │       ├── chat/        # Real-time messaging
│   │       ├── profile/     # User profiles & edit
│   │       └── comments/    # Comments sheet
│   ├── navigation/          # Compose Navigation
│   └── util/                # FCM service, helpers

eshort-admin/
├── app/src/main/java/com/eshort/admin/
│   ├── di/                  # Hilt module
│   ├── data/
│   │   ├── model/           # Admin data models
│   │   └── repository/      # Admin repository
│   ├── viewmodel/           # Admin ViewModel
│   ├── ui/
│   │   ├── theme/           # Admin purple theme
│   │   └── screens/
│   │       ├── dashboard/   # Analytics dashboard
│   │       ├── users/       # User management
│   │       ├── videos/      # Video moderation
│   │       ├── reports/     # Reports system
│   │       └── notifications/ # Broadcast notifications
│   └── navigation/          # Admin navigation
```

## Supported Video Platforms

| Platform | URL Pattern |
|----------|-------------|
| TikTok | `tiktok.com/*` |
| YouTube Shorts | `youtube.com/shorts/*` |
| Instagram Reels | `instagram.com/reel/*` |
| Facebook Reels | `facebook.com/reel/*`, `fb.watch/*` |

## Design

- Dark theme with `#FF2D55` primary (pink-red)
- Glassmorphism effects
- Smooth animations (Compose animation APIs)
- Gradient accents
- Rounded UI components
- Material 3 design system
