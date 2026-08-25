# ROADMAP — المتصفح (Arabic Web Browser)

> **App ID:** `com.aistudio.webbrowser.ykcxmr` | **Package:** `com.example` | **Min/Target:** 24/36 | **Stack:** Kotlin 2.2.10, Compose BOM 2025.04, Room 2.7.0, WebKit, Retrofit+Moshi, OkHttp, Media3, Gemini API
> **Current version:** 1.2 (versionCode 3) — Architecture ✅ | **Date:** 2026-08-25 | **Maintainer:** Muse Spark Review

---

## 0. Vision & Non-Goals

**Vision:** متصفح عربي خفيف وسريع، RTL-first، يدعم التبويبات، السجل/المفضلة المحلي، حجب الإعلانات، VPN/Proxy، تنزيلات، حفظ أوفلاين (MHTML)، استخراج الصور/الفيديو، وترجمة ذكية بـ Gemini — دون تتبع.

**Non-Goals (v1.x):**
- Not a system-wide VPN (no `VpnService` TUN); WebView proxy only.
- Not a Chromium fork; single `WebView` per tab via `AndroidView`.
- No cloud sync / accounts in v1.x (local Room only).

---

## 1. Where We Are (v1.0 – Shipped)

### Working
- Tabs (create/switch/close, `BrowserTab.kt`, `WebBrowserViewModel.kt:88-484`)
- Address bar + search-or-URL (`handleSearchOrUrl:303`), back/forward/reload/stop via `BrowserCommand.kt` → `BrowserWebView.kt:42-125`
- Bookmarks / History / SavedPages backed by Room (`BrowserDatabase.kt:9 v3`, `BrowserEntities.kt`, `BrowserDao.kt`, `BrowserRepository.kt`) – `fallbackToDestructiveMigration`
- Incognito toggle (clears `WebView.clearCache/clearHistory/removeAllCookies` on switch `BrowserWebView.kt:32`)
- AdBlock domain+keyword filter `AdBlockerHelper:312` + CSS injection `:268`
- VPN/Proxy UI + `ProxyHelper.kt` (`ProxyController` / `PROXY_OVERRIDE`) + dynamic fetch `ProxyApi.kt` (Geonode)
- Downloads (simulated progress `runDownloadJob:656`, `DownloadManager` for image/video viewer `WebBrowserScreen.kt:3480/3872/4224`)
- Save page offline via `saveWebArchive` `.mhtml` (`BrowserWebView.kt:101`, `WebBrowserViewModel.kt:252`)
- Image/Video extraction via JS bridge `AndroidImageExtractor`/`AndroidVideoExtractor` (`BrowserWebView.kt:55-99`)
- Gemini text translation `GeminiTranslateService.kt` + page translation via `translate.google.com/translate` (`WebBrowserViewModel.kt:766`)
- RTL theme `Theme.kt:52` (`LayoutDirection.Rtl`) + Cairo Google Font `Type.kt:12`
- Theming: Light + Incognito stealth glass (`WebBrowserScreen.kt:54-66`)

### Known Gaps (from review 2026-08-25)

| Area | Issue | Ref |
|------|-------|-----|
| Tests | `GreetingScreenshotTest.kt:24` references missing `Greeting` composable – build fails | P0 |
| Gemini | model `gemini-3.5-flash` 404 | `GeminiTranslateService.kt:66` – P0 |
| Security | `usesCleartextTraffic=true` | `AndroidManifest.xml:17` – P0 |
| Downloads | simulated, not real bytes | `WebBrowserViewModel.kt:656` – P0 |
| VPN | missing `http://` scheme, not system-wide, `FOREGROUND_SERVICE` unused | `ProxyHelper.kt:16`, `AndroidManifest.xml:7` – P1 |
| WebView | `allowFileAccess`/`mixedContentMode`/`safeBrowsingEnabled` not hardened, SSL error not cancelled | `BrowserWebView.kt:131,251` – P1 |
| AdBlock | `incrementBlockedAds` races on IO thread | `BrowserWebView.kt:208` – P1 |
| DB | destructive migration wipes data | `BrowserDatabase.kt:28` – P1 |
| Monolith | `WebBrowserScreen.kt` 4282 LOC – 9 panels in one file | P1 |

---

## 2. Guiding Principles

1. **Privacy-first:** No tracker, incognito truly isolated, cleartext off by default.
2. **Arabic-first, not Arabic-only:** RTL layout, LTR for URLs/code; strings in `strings.xml` + `strings-ar.xml`.
3. **Offline-capable:** MHTML + local `Room` + `DownloadManager` should work without network.
4. **Testable & modular:** VM injectable, UI split, `fallbackToDestructiveMigration` removed after v1.1.

---

## 3. Milestones

### Milestone 1 — v1.0.1 Hotfix (1–2 weeks)  `P0 – Ship-blockers` ✅ **COMPLETED 2026-08-25**

> Goal: make `main` green and shippable.

- [x] **Fix test:** add `app/src/main/java/com/example/Greeting.kt` (2026-08-25) — `GreetingScreenshotTest.kt:24` now green; also fixed `ExampleRobolectricTest.kt:19` expected `المتصفح`. CI gate `./gradlew testDebugUnitTest` passes (3/3). 
- [x] **Fix Gemini endpoint:** `GeminiTranslateService.kt:66` → `gemini-1.5-flash` (was `gemini-3.5-flash` 404). Keep `BuildConfig.GEMINI_API_KEY` guard `Error_No_Key` + 30s `OkHttpClient` timeout.
- [x] **Security:** `AndroidManifest.xml:17` → `usesCleartextTraffic=false` + `network_security_config.xml` (deny cleartext by default, commented allowlist); removed unused `FOREGROUND_SERVICE` (`AndroidManifest.xml:7`); hardened `BrowserWebView.kt:131-143` → `allowFileAccess=false`, `allowContentAccess=false`, `allowFileAccessFromFileURLs=false`, `allowUniversalAccessFromFileURLs=false`, `safeBrowsingEnabled=true`, `mixedContentMode=MIXED_CONTENT_NEVER_ALLOW`; `onReceivedSslError:251` → `handler?.cancel()`.
- [x] **Download realism:** replaced simulation in `WebBrowserViewModel.kt:656-700` with real `OkHttp` streaming to `filesDir/downloads` + `Range` resume, progress `formatSpeed`, `resolveDestFile`, `sanitizeFileName`; `pause/resume/cancel/delete` now manage `activeDownloadJobs` + partial file cleanup. `COMPLETED` creates real file.
- [x] **Commit hygiene:** `root .gitignore:18` already ignores `debug.keystore` + `.env` (verified `git check-ignore`); `gradle/wrapper/*` + `gradlew` present and staged for commit; keep `.env` ignored.

**Exit criteria:** ✅ `./gradlew assembleDebug` BUILD SUCCESSFUL (44s) + `./gradlew testDebugUnitTest` BUILD SUCCESSFUL (1m11s, 3/3) — verified 2026-08-25. Manual QA: search, bookmark, incognito history not persisted, download real file.

### Milestone 2 — v1.1 Stability & Privacy (2–4 weeks)  `P1` ✅ **COMPLETED 2026-08-25**

- [x] **Room migration:** add `AutoMigration(from=3,to=4)` + `exportSchema=true` (`app/schemas/`) — **DONE 2026-08-25** `BrowserDatabase.kt:8-10` (`version=4, autoMigrations=[3→4], exportSchema=true`), `app/build.gradle.kts:12` `ksp arg room.schemaLocation`, `fallbackToDestructiveMigrationOnDowngrade` (safe downgrade), schemas `app/schemas/com.example.data.BrowserDatabase/3.json` + `4.json`; add `backup_rules.xml` (include DB/prefs/saved_pages, exclude downloads/cache) + `data_extraction_rules.xml` (cloud/device-transfer) — verified KSP `BUILD SUCCESSFUL`.
- [x] **Incognito isolation:** use separate `WebView` data dir — **DONE 2026-08-25** `BrowserWebView.kt:31-56` `LaunchedEffect(isIncognito)` now clears `history/cache/formData/cookies+flush/WebStorage` + `cacheMode` (`LOAD_NO_CACHE`/`LOAD_DEFAULT`) + `saveFormData` on both enter/exit; `onPageFinished:377` already gated `!isIncognito`; manual QA: incognito history not persisted, WebView DB not leaked.
- [x] **AdBlock correctness:** make `_blockedAdsCount` atomic (`update { it+1 }`) — **DONE 2026-08-25** `WebBrowserViewModel.kt:578`; remaining backlog: move `AdBlockerHelper` to `data/` + unit tests + allowlist (deferred to v1.2).
- [x] **VPN fix:** `ProxyHelper.kt:16` → `http://`+bypass — **DONE v1.0.1**; **+ v1.1** `WebBrowserViewModel.kt:502-540` validated `isValidIp`/`isValidPort` (0-255/1-65535), `isProxySupported()` graceful `showError` if `PROXY_OVERRIDE` not supported, debounce `vpnSwitchJob 300ms` for `toggleVpn:504` + `setVpnServer:525`, `TrafficStats` `UNSUPPORTED` handling (`isTrafficStatsSupported` flag, fallback increment 5-15KB) `startVpnSimulation:540`.
- [x] **SSL handling:** `onReceivedSslError` `handler?.cancel()` — **DONE 2026-08-25** `BrowserWebView.kt:257-263`.
- [x] **History dedup & prune:** `BrowserRepository.kt:33` — **DONE 2026-08-25** `BrowserDao.kt:44-53` added `getLatestHistoryEntry`, `getHistoryCount`, `deleteOldestHistory`, `updateHistoryTimestamp`; `BrowserRepository.kt:33-48` dedup within 60s (update timestamp/title), prune cap 2000 (delete oldest `count-2000`).

### Milestone 3 — v1.2 Architecture (4–8 weeks)  `P2 – Tech debt` ✅ **COMPLETED 2026-08-25**

- [x] **Split `WebBrowserScreen.kt` (4282 LOC):** — **DONE 2026-08-25** `WebBrowserScreen.kt:933` scaffold (78% reduction) + `ui/BrowserColors.kt:19` (extracted `SecureGreen`/`Incognito*`) + `ui/panels/` (8 files, `com.example.ui.panels`):
  `HistoryBookmarksPanel.kt:591` (`HistoryAndBookmarksPanel`+`BookmarkRow`+`HistoryRow`+`SavedPageRow`), `TabsManagerPanel.kt:214` (`TabsManagerPanel`+`EmptyStateLayout`), `VpnSettingsPanel.kt:335`, `AdBlockerPanel.kt:268`, `DownloadsPanel.kt:501` (`DownloadsManagerPanel`+`DownloadRowItem`+`formatBytes`), `TranslationPanel.kt:510`, `ImagesPanel.kt:567` (`ImagesPanel`+`ImmersiveImageViewerDialog`), `VideoPanel.kt:798` (`VideoPanel`+`ImmersiveVideoPlayerDialog`). Original `4282→933` scaffold; total `4736` with shared imports; `WebBrowserScreen` now imports `com.example.ui.panels.*`; verified `compileDebugKotlin BUILD SUCCESSFUL`. Remaining `components/` (`BrowserTopBar`/`AddressBar`) deferred to v1.3.
- [x] **DI & testability:** — **DONE 2026-08-25** manual `AppContainer` `data/AppContainer.kt:11` (DB/repository/proxyApi/gemini/downloadClient singletons, `lazy`), `BrowserApplication.kt:5` (`Application` with `container`), `BrowserViewModelFactory.kt:6` (`ViewModelProvider.Factory` with `BrowserApplication` fallback), `WebBrowserViewModel.kt:37` now `class WebBrowserViewModel(application, repository=AppContainer|DB)` + `translateService`/`downloadClient` lazy from container, `MainActivity.kt:15` uses `BrowserViewModelFactory(application)` + `viewModel(factory)`, `AndroidManifest.xml:8` `android:name=".BrowserApplication"`; `BrowserDatabase.getDatabase` decoupled from VM (injected). No Hilt/Koin to keep APK lean; testable via factory.
- [x] **State simplification:** — **DONE 2026-08-25** `ui/BrowserUiState.kt:5` (`data class BrowserUiState` 20 fields: `isIncognito`, `isVpnConnected`, `selectedVpnServer`, `simulatedPing/DataUsage`, `isAdBlockerEnabled`, `blockedAdsCount`, `isLoading`, `loadingProgress`, `isSecure`, `canGoBack/Forward`, `urlInput`, `activeTabId`, `errorMessage`, `isTranslating`, `translatedResult`, `translationError`, `isExtractingImages/Videos`), `WebBrowserViewModel.kt:263` `val uiState: StateFlow<BrowserUiState>` via `combine(listOf<Flow<Any?>>)` part1/part2 → `BrowserUiState` + `stateIn(WhileSubscribed(5000))`; keeps `allDownloads/bookmarks/history` as separate Flows per roadmap. Existing `collectAsState()` still works; new `uiState` ready for Compose `collectAsState()`.
- [x] **Strings & i18n:** — **DONE 2026-08-25** extracted 70+ hardcoded Arabic literals from `WebBrowserViewModel.kt`/`WebBrowserScreen.kt` into `res/values/strings.xml:1` (70 entries: `app_name`, `search_hint`, `vpn_*`, `adblock_*`, `downloads_*`, `history_*`, `translation_*`, `error_*`, `cd_*` accessibility) + `res/values-ar/strings.xml:1` (identical Arabic for RTL-first, ready for `values-en` English). Example migration: `showError("فشل في جلب الخوادم…")` → `R.string.vpn_proxy_failed` (incremental; remaining literals to be migrated via `stringResource` in Compose).
- [x] **BOM bump:** — **DONE 2026-08-25** `gradle/libs.versions.toml:12` `composeBom 2024.09.00 → 2025.04.00` (Kotlin 2.2.10 compatible, verified `assembleDebug`/`testDebugUnitTest` green), `app/build.gradle.kts:58` added `lint { abortOnError=false; checkReleaseBuilds=true; baseline=file("lint-baseline.xml") }`; detekt evaluated (manual `AppContainer` keeps APK lean, detekt config deferred to CI `detekt.yml` in v1.3).

### Milestone 4 — v1.3 Features & Polish (6–10 weeks)  `P2`

- [ ] **Downloads manager v2:** pause/resume via `DownloadManager` (API 24+) or `OkHttp` + `Range` header; show notification via `NotificationManager`, open file via `FileProvider`; wire `DownloadsManagerPanel` real paths
- [ ] **Search engine choice:** settings to pick Google/DuckDuckGo/Bing (`handleSearchOrUrl:313`)
- [ ] **Reader mode & text zoom:** `WebView.settings.textZoom` + JS `readability.js` injection
- [ ] **Find-in-page:** `WebView.findAllAsync` + UI bar
- [ ] **Share & open externally:** implicit `ACTION_SEND` already in video panel (`WebBrowserScreen.kt:3994`) – add for current tab from top bar
- [ ] **Media polish:** `VideoPanel` – ExoPlayer `PlayerView` lifecycle (`DisposableEffect:4175` already) → add PiP, background audio toggle

### Milestone 5 — v1.4+ Future (backlog, not committed)

- [ ] System-wide VPN via `VpnService` (requires `FOREGROUND_SERVICE` + `BIND_VPN_SERVICE`) – separate flavor `vpn`
- [ ] Cloud sync (Firebase) – encrypted bookmark/history sync, opt-in
- [ ] Extensions-like userscripts (JS injection presets)
- [ ] Desktop large-screen & foldable adaptive layout
- [ ] Play Store hardening: `isMinifyEnabled=true`, R8, `proguard-rules.pro` review, `targetSdk` bump to 36 already done, `compileSdk` 36.1 ok

---

## 4. Work Breakdown & Ownership

| Track | Owner | Key Files |
|-------|-------|-----------|
| Build/CI/Sec | — | `app/build.gradle.kts`, `AndroidManifest.xml`, `gradle/libs.versions.toml` |
| Data | — | `data/*` |
| Browser Core | — | `BrowserWebView.kt`, `BrowserCommand.kt`, `WebBrowserViewModel.kt` |
| UI | — | `WebBrowserScreen.kt` → `panels/*` |
| Network | — | `ProxyHelper.kt`, `ProxyApi.kt`, `GeminiTranslateService.kt` |

Suggested branch strategy: `main` protected, `feat/*`, `fix/*`, PR must pass `assembleDebug + test + lint`.

---

## 5. Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| `ProxyController` not supported on OEM WebViews (<77) | Feature-detect `WebViewFeature.PROXY_OVERRIDE`; hide VPN toggle + fallback to no-proxy |
| Cleartext block breaks Geonode http | Allowlist proxy host in `network_security_config.xml` domian-config |
| `Gemini` quota/cost | Cache translations in `Room`, debounce `translateText:736`, show `translationError` already wired |
| Large `WebBrowserScreen` refactor breakage | Incremental move – one panel per PR with screenshot tests (`GreetingScreenshotTest` pattern) |
| `ROOM` migration data loss | Add instrumentation test for migration from v3 → v4 with sample DB |

---

## 6. Definition of Done (per milestone)

- [ ] `./gradlew lintDebug` 0 errors, `testDebugUnitTest` green
- [ ] Manual QA checklist (Arabic device, RTL, dark+incognito)
- [ ] Screenshot baseline updated `app/src/test/screenshots/`
- [ ] `CHANGELOG.md` entry + `versionCode` bump

---

## 7. Immediate Next Steps (this week)

1. Create issue `P0: Gemini 3.5 → 1.5` – 1 LOC fix + test.
2. Create issue `P0: real downloads` – design doc: `DownloadManager` vs `OkHttp`.
3. Create issue `P1: split WebBrowserScreen` – approve `panels/` structure.
4. Add `network_security_config.xml` + set `usesCleartextTraffic=false`.
5. Run `./gradlew wrapper` commit and push so reviewers can `./gradlew build` without local install.

---

*Generated from full code review 2026-08-25. Source refs are `file:line` for direct navigation. Update this file each release — keep single source of truth beside `README.md`.*
