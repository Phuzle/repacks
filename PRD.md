# Product Requirements Document (PRD)

**Project:** Repacks for Android

**Package ID:** `com.phuzle.labs.repacks`

**Target Organization:** `Phuzle`

**License:** MIT / Open-Source

**Platforms:** Android 8.0+ (API Level 26–35)

---

## 1. Executive Summary & Vision

**Repacks** is a clean, native Android application built in Kotlin and Jetpack Compose. Its primary objective is to monitor release feeds from top video game repackers silently and notify users when new drops occur—without requiring a remote push-notification infrastructure (no FCM required) or external servers.

### Core Tenets

- **Zero Middleman Infrastructure:** Operates client-side via Android OS background workers (`WorkManager`).
- **Good-Citizen Traffic Model:** Respects original site bandwidth using HTTP caching (`ETags` / `Last-Modified`) and never serves direct download/magnet links in-app. All downloads route directly through the repacker's web domain via Chrome Custom Tabs to preserve original creator ad revenue and attribution.
- **Modern Android Native Feel:** Material 3 design, dynamic wallpaper-based accent colors (Material You), full Light/Dark/System theme switching, and fluid physics-based micro-interactions.

---

## 2. Project Initialization & Architecture Setup

### 2.1 Repository Setup (`gh` CLI)

Execute the following commands to initialize the repository under the `Phuzle` GitHub organization:

```bash
# 1. Create remote repository under Phuzle organization
gh repo create Phuzle/repacks \
  --public \
  --description "Native Android release aggregator & notification tracker for game repacks." \
  --license MIT \
  --gitignore Android

# 2. Clone and setup local workspace
git clone https://github.com/Phuzle/repacks.git
cd repacks

# 3. Create project documentation scaffolding
mkdir -p docs/architecture docs/design
touch docs/architecture/ARCHITECTURE.md
touch docs/design/DESIGN_SYSTEM.md

```

### 2.2 System Architecture Overview

```
+-----------------------------------------------------------------------------------+
|                                 ANDROID RUNTIME                                   |
|                                                                                   |
|  +---------------------------+             +-----------------------------------+  |
|  |     WorkManager Service   |             |       Jetpack Compose UI          |  |
|  |  (Periodic / On-Demand)   |             |   (Feed, Details, Config, About)  |  |
|  +-------------+-------------+             +-----------------+-----------------+  |
|                |                                             |                    |
|                v                                             v                    |
|  +---------------------------+             +-----------------------------------+  |
|  |  RssParser & Metadata     |             |      RepackViewModel (MVI)        |  |
|  |  Extractor (Jsoup/Regex)  |             |  - List State & Filters           |  |
|  +-------------+-------------+             |  - Update Manager State           |  |
|                |                           +-----------------+-----------------+  |
|                | (Write New Items)                           | (Read Streams)     |
|                v                                             v                    |
|  +-----------------------------------------------------------------------------+  |
|  |                           Room Database (Local Cache)                       |  |
|  |  - repacks table (guid, provider, slug, title, banner, tags, sizes, time)  |  |
|  |  - watchlist table (keywords)                                              |  |
|  +-----------------------------------------------------------------------------+  |
|                |                                             |                    |
|                v                                             v                    |
|  +---------------------------+             +-----------------------------------+  |
|  | NotificationManagerCompat |             |  CustomTabsIntent (Chrome Tabs)   |  |
|  |  (Rich BigPictureStyle)   |             |  (Direct Traffic to Site)         |  |
|  +---------------------------+             +-----------------------------------+  |
+-----------------------------------------------------------------------------------+

```

---

## 3. Technology Stack & Dependencies

| Layer                | Technology                     | Purpose                                                  |
| -------------------- | ------------------------------ | -------------------------------------------------------- |
| **Language**         | Kotlin 2.0+                    | Modern type safety, coroutines, and flow                 |
| **UI Toolkit**       | Jetpack Compose + Material 3   | Declarative UI, smooth gestures, dynamic theming         |
| **Local Storage**    | Room Database                  | Offline storage, caching, and reactive data streams      |
| **Key-Value Store**  | Jetpack Preferences DataStore  | Theme selection, provider toggles, filter configurations |
| **Background Sync**  | AndroidX WorkManager           | Doze-compatible periodic background polling              |
| **Networking**       | OkHttp 4.x + CacheInterceptor  | HTTP request handling, ETag caching, custom User-Agents  |
| **HTML/XML Parsing** | Jsoup + XML PullParser         | Extracting clean metadata, images, and tags from RSS     |
| **Image Loading**    | Coil 3.x (Compose)             | Async image rendering and notification bitmap decoders   |
| **In-App Browser**   | AndroidX Browser (Custom Tabs) | Edge-to-edge browser view for visiting original sources  |
| **Navigation**       | Jetpack Navigation Compose     | Deep-link route management and screen transitions        |

---

## 4. UI & UX Design System

### 4.1 Theme Engine

- **Themes Supported:**
- `System Default` (Follows OS schedule)
- `Pure Dark / AMOLED Black` (Background `#000000` for battery optimization)
- `Light Mode` (Clean neutral contrast)

- **Material You / Dynamic Colors:** Leverages `dynamicDarkColorScheme(context)` and `dynamicLightColorScheme(context)` on Android 12+ (API 31+) to tint surfaces, FABs, chips, and progress bars with system accent palettes.

### 4.2 Application Navigation Structure

The app employs a standard Bottom Navigation Bar with two primary destinations and modal/sub-page routing:

1. **Feed Tab (`/feed`):**

- **Hero Header:** Dynamic status pill showing last sync time and a manual pull-to-refresh spinner.
- **Quick Filters Row:** Horizontally scrollable chips (`All`, `FitGirl`, `DODI`, `SteamRIP`, `Watchlist Only`).
- **Repack Cards (`LazyColumn`):**
- 16:9 Banner image with smooth crossfade and shimmer loading placeholder.
- Repacker logo badge (top-left over image).
- Release title, relative timestamp (e.g., _25m ago_), repack size badge, and category tags.
- Tap action: Opens native detail view (`/{provider}/{slug}`).

2. **Detail View (`/{provider}/{slug}`):**

- Parallax edge-to-edge cover image with gradient scrim.
- Prominent metadata grid: **Repack Size**, **Original Size**, **File Reduction %**, **Release Date**.
- Cleaned genre and category pills.
- Repack changelog/features description in sanitized Markdown format.
- **Primary Bottom CTA:** Fixed elevated button `"Open on [Site Name]"` triggering Chrome Custom Tabs.

3. **Configure Tab (`/configure`):**

- **Providers Settings:** Toggles for individual sites with status indicators.
- **Filter Engine:** Adult/NSFW content filter, maximum size threshold slider (e.g., _Skip repacks > 80 GB_).
- **Watchlist Manager:** Keyword input chips to trigger priority notifications.
- **Sync & Schedule:** Polling interval slider (1 hr, 2 hrs, 6 hrs, 12 hrs), "Wi-Fi Only" toggle, "Quiet Hours" interval selector.
- **App Settings:** Theme selector (Light / Dark / System), Check for Updates, About & Disclaimers page.

---

## 5. Notification Strategy & Permission Lifecycle

### 5.1 Permission Request Flow (Android 13+ / API 33)

To avoid high drop-off and rejection rates, the app **never** requests `android.permission.POST_NOTIFICATIONS` on first launch.

```
+---------------------------------------------------------------------------+
|                          1. First Launch (Onboarding)                     |
|  User explores the feed; notifications are NOT requested immediately.     |
+-------------------------------------+-------------------------------------+
                                      |
                                      v
+---------------------------------------------------------------------------+
|                          2. Contextual Trigger                            |
|  User switches on a provider toggle OR adds a game to their Watchlist.   |
+-------------------------------------+-------------------------------------+
                                      |
                                      v
+---------------------------------------------------------------------------+
|                      3. In-App Educational Dialog                         |
|  Modal explains: "Repacks needs notification access to alert you when     |
|  your monitored games drop. We do not run background ads or spam."       |
+-------------------+-----------------------------------+-------------------+
                    | [Allow]                           | [Not Now]
                    v                                   v
+---------------------------------------+   +-------------------------------+
|     4. System Permission Prompt       |   |      5. Graceful Rejection    |
|   `requestPermissionLauncher.launch`  |   |  Toggle stays ON for feed,    |
+-------------------+-------------------+   |  banner appears: "In-app only |
                    |                       |  mode active (No alerts)".    |
          +---------+---------+             +-------------------------------+
          |                   |
          v [Granted]         v [Denied Permanently]
+-------------------+   +---------------------------------------------------+
|  6. Success State |   |             7. Fallback Settings Flow             |
|  Schedule Workers |   | Display actionable banner pointing to App Info    |
|  with alerts on.  |   | Settings: "Enable alerts in Android Settings".   |
+-------------------+   +---------------------------------------------------+

```

### 5.2 Rich Notification Blueprint

Notifications are constructed using `NotificationCompat.BigPictureStyle` to create high-impact alerts:

```kotlin
// Notification Construction Blueprint
val notification = NotificationCompat.Builder(context, CHANNEL_DROPS_ID)
    .setSmallIcon(R.drawable.ic_repack_monochrome)
    .setLargeIcon(providerAvatarBitmap) // Provider circular logo
    .setContentTitle("${repack.title}")
    .setContentText("${repack.providerName} • Size: ${repack.repackSize}")
    .setStyle(
        NotificationCompat.BigPictureStyle()
            .bigPicture(repackBannerBitmap)
            .setSummaryText(repack.shortDescription)
    )
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .setContentIntent(createPendingIntentToAppDetail(repack))
    .setAutoCancel(true)
    .build()

```

---

## 6. Feed Scraping & Anti-Bot Strategy

Direct scraping of HTML frontends often triggers Cloudflare 403 Forbidden challenges and DDoS-Guard IP rate limits. The app bypasses this by operating exclusively on structured feeds with robust HTTP client hygiene.

### 6.1 Supported Providers & Endpoints

| Provider            | Endpoint Strategy                                                          | Parsing Engine                                      |
| ------------------- | -------------------------------------------------------------------------- | --------------------------------------------------- |
| **FitGirl Repacks** | `[https://fitgirl-repacks.site/feed/](https://fitgirl-repacks.site/feed/)` | Standard RSS 2.0 XML + Jsoup description extraction |
| **DODI Repacks**    | `[https://dodi-repacks.site/feed/](https://dodi-repacks.site/feed/)`       | Standard RSS 2.0 XML + Regex field parser           |
| **SteamRIP**        | `[https://steamrip.com/feed/](https://steamrip.com/feed/)`                 | RSS 2.0 XML / Atom feed                             |
| **KaOsKrew**        | Forum release RSS thread                                                   | Atom XML parser                                     |

### 6.2 Cloudflare & Rate-Limiting Protection Engine

```kotlin
// OkHttp Client Configuration for Anti-Bot Hygiene
val okHttpClient = OkHttpClient.Builder()
    .cache(Cache(File(context.cacheDir, "http_cache"), 15L * 1024 * 1024)) // 15MB Cache
    .addInterceptor { chain ->
        val original = chain.request()
        val requestWithHeaders = original.newBuilder()
            // Standard browser headers prevent generic bot flags
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36")
            .header("Accept", "application/rss+xml, application/xml, text/xml; q=0.9, */*; q=0.8")
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()
        chain.proceed(requestWithHeaders)
    }
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()

```

- **HTTP 304 Handling:** Uses `ETag` and `If-Modified-Since` headers. If the feed has not changed since the last check, the server returns `304 Not Modified`, saving phone data and preventing IP flags.
- **Exponential Backoff:** If a provider returns `429 Too Many Requests` or `503 Service Unavailable`, `WorkManager` applies an exponential backoff policy (`BackoffPolicy.EXPONENTIAL`, minimum 15 minutes).

---

## 7. Concrete Feature Specifications

### 7.1 In-App Self-Updater (GitHub Releases API)

Since the app is distributed outside Google Play, it includes an integrated update manager:

- **Trigger:** Checks `[https://api.github.com/repos/Phuzle/repacks/releases/latest](https://api.github.com/repos/Phuzle/repacks/releases/latest)` on app launch (throttled to once every 24 hours) or via manual click in Settings.
- **Comparison:** Compares `tag_name` (e.g., `v1.2.0`) against current `BuildConfig.VERSION_NAME`.
- **Execution:**

1. Displays an in-app modal detailing release notes / changelog.
2. Downloads the APK directly using Android's `DownloadManager`.
3. Triggers native package installer via `FileProvider` (`ACTION_VIEW` intent with `application/vnd.android.package-archive`).

### 7.2 Custom Watchlist & Keyword Filter

- Allows users to add monitored game names (e.g., "Grand Theft Auto", "Persona", "Ghost of Tsushima").
- **Notification Routing:** When a new feed item drops:
- If Watchlist has entries and item matches keyword: **High-priority notification fired immediately**.
- If item does not match Watchlist but provider is enabled: **Saved to feed silently (or normal alert based on user preference)**.

### 7.3 Data Retention & Storage Hygiene

- **Auto-Cleanup Job:** A cleanup query runs during every sync cycle:

```sql
DELETE FROM repacks WHERE timestamp < :thirtyDaysAgo AND is_favorited = 0;

```

- **Image Caching:** Images loaded in Coil are stored in a dedicated LRU disk cache capped at 50 MB, preventing app data bloat.

### 7.4 About Page & Legal Disclaimers

- **Attribution:** Direct links to the Phuzle GitHub organization, source repository, and issue tracker.
- **Explicit Disclaimer:**
    > _"Repacks is an open-source metadata aggregator and RSS reader. This application does not host, index, or distribute any game files, torrents, magnets, or direct download links. All trademarks, titles, and cover art belong to their respective owners."_

---

## 8. Data Schema (Room Database)

### 8.1 Repack Entity (`repack_items`)

```kotlin
@Entity(
    tableName = "repack_items",
    indices = [
        Index(value = ["guid"], unique = true),
        Index(value = ["provider", "slug"], unique = true)
    ]
)
data class RepackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "guid") val guid: String,
    @ColumnInfo(name = "provider") val provider: String,          // e.g. "fitgirl-repacks.site"
    @ColumnInfo(name = "slug") val slug: String,                  // e.g. "cyberpunk-2077"
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "banner_url") val bannerUrl: String?,
    @ColumnInfo(name = "original_url") val originalUrl: String,
    @ColumnInfo(name = "original_size") val originalSize: String?,// e.g. "70.2 GB"
    @ColumnInfo(name = "repack_size") val repackSize: String?,    // e.g. "35.1 GB"
    @ColumnInfo(name = "genres") val genres: String?,             // JSON array string
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "is_nsfw") val isNsfw: Boolean = false,
    @ColumnInfo(name = "is_favorited") val isFavorited: Boolean = false
)

```

### 8.2 Watchlist Entity (`watchlist_items`)

```kotlin
@Entity(tableName = "watchlist_items")
data class WatchlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "keyword") val keyword: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

```

---

## 9. GitHub Actions CI/CD Pipeline (`release.yml`)

The repository is pre-configured to build, sign, and publish APKs whenever a new version tag is pushed:

```yaml
name: Build & Release APK

on:
    push:
        tags:
            - "v*"

jobs:
    build-release:
        runs-on: ubuntu-latest
        steps:
            - name: Checkout Code
              uses: actions/checkout@v4

            - name: Setup JDK 17
              uses: actions/setup-java@v4
              with:
                  distribution: "temurin"
                  java-version: 17

            - name: Setup Gradle
              uses: gradle/actions/setup-gradle@v3

            - name: Grant execute permission for gradlew
              run: chmod +x gradlew

            - name: Build Release APK
              run: ./gradlew assembleRelease

            - name: Sign Android APK
              uses: r0adkll/sign-android-release@v1
              id: sign_app
              with:
                  releaseDirectory: app/build/outputs/apk/release
                  signingKey: ${{ secrets.SIGNING_KEY }}
                  alias: ${{ secrets.KEY_ALIAS }}
                  keyStorePassword: ${{ secrets.KEYSTORE_PASSWORD }}
                  keyPassword: ${{ secrets.KEY_PASSWORD }}

            - name: Create GitHub Release
              uses: softprops/action-gh-release@v2
              with:
                  files: ${{ steps.sign_app.outputs.signedReleaseFile }}
                  generate_release_notes: true
                  draft: false
                  prerelease: false
              env:
                  GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

---

## 10. Verification & Acceptance Criteria

1. **Background Reliability:** When the phone is left locked and idle for 4 hours on Wi-Fi, `WorkManager` runs at least once and discovers new RSS drops.
2. **Notification Delivery:** When a new repack drops matching a Watchlist term, a rich `BigPictureStyle` notification appears displaying the hero image, repack size, and provider badge.
3. **Deep Navigation:** Tapping the notification opens the app directly to `/{provider}/{slug}`. Clicking _"Open on [Provider]"_ immediately launches the URL in a Chrome Custom Tab.
4. **Bandwidth Efficiency:** If no new releases have occurred, feed requests return `304 Not Modified` and consume less than 1 KB of network data per cycle.
5. **Theme Compliance:** The app accurately reflects dynamic theme changes (switching between dark mode, light mode, and system Material You colors) without requiring an app restart.
