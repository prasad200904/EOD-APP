# Changelog

All notable changes to the **WorkCore** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Security
- **Firestore Access Control**: Updated `firestore.rules` helper functions (`isAdmin`, `isEmployee`, `myEmployeeId`) to resolve profiles via `/users/$(request.auth.uid)` and Firebase Auth custom claims, resolving `PERMISSION_DENIED` errors on EOD submissions.
- **Logcat Sanitization**: Removed plaintext password printing from `WorkCoreAuth` logs in `WorkCoreViewModel.kt`.

### Fixed
- **ProGuard / R8 Obfuscation**: Added keep rules in `app/proguard-rules.pro` for Gson and custom form data classes (`*FormData`, `StudentEntry`, `CustomTeamMetrics`, `CustomFormSubmission`) to prevent JSON serialization breakage in release builds.
- **EOD History Query**: Eliminated composite index errors by sorting EOD records in-memory (`.sortedByDescending { it.date }`).
- **People Page Callbacks**: Wired `onAddTeamClick` callback in `MainActivity.kt` to open the team creation screen.
- **Theme & Text Contrast**: Fixed white-on-white text contrast bugs across `TeamHistoryScreen.kt` and its detail modal to properly render with `DarkSlateDeep` on warm light surfaces.

### Performance
- **Sync Throttling**: Optimized background sync in `SyncManager.kt` from an aggressive 30-second sweep loop to a 15-minute fallback that only activates if pending items exist, preserving battery, bandwidth, and Firestore quota.

---

## [1.2.2] - 2026-10-22

**Version Code:** `5`  
**Git Commit:** `8b25480`  

### Added
- **Firebase Real-Time Sync**: Synchronized EOD submissions with Firestore in real-time.
- **Analytics & Crashlytics**: Integrated Firebase Crashlytics and custom event logging in `Analytics.kt`.
- **Automated Tests**: Added unit and Robolectric tests for authentication flow, screen rendering, and local persistence.
- **Release Automation**: Introduced `release.bat` and `release.ps1` for automated version bumping, Git tagging, and GitHub Releases via GitHub CLI (`gh`).

### Security
- Excluded sensitive files (`google-services.json`, `*.keystore`, `.env`, service account keys) from Git tracking in `.gitignore`.

---

## [1.2.1] - 2026-10-21

**Version Code:** `4`  
**Git Commit:** `2d7c949`  

### Changed
- **Admin Console Redesign**:
  - Streamlined Admin Dashboard to display live submission counts (`Submitted Today`, `On Leave`, `Total Missed`) rather than rollup analytics.
  - Relocated "Add Employee" and "Team Management" actions to the People (`SimpleEmployeesScreen`) screen.
- **Warm Light Theme**: Rolled out the warm light theme (WarmCream, WarmCardWhite, CoralPrimary, DarkSlateDeep) across all admin and employee screens.
- **People Management**: Added inline employee status toggling and account deletion capabilities.

---

## [1.2.0] - 2026-10-18

**Version Code:** `3`  
**Git Commit:** `1b0add3`  

### Added
- **Multi-Company Support**: Added database and schema support for parent company (**Sandspace Technologies**) and subsidiaries (**Academic Overseas**).
- **Custom EOD Forms**: Built dynamic Compose forms for Academic Overseas:
  - `CounsellorEodFormScreen`: Dynamic student entries, university selection, visa stage tracking.
  - `TelecallingEodFormScreen`: Call metrics, follow-up tracking, real-time conversion rates.
  - `MarketingEodFormScreen`: College visit logs, feedback tracking.
  - `SimpleEodFormScreen`: Lightweight description-only EOD submission.
- **Company Selector**: Added cascading filter chips (Company + Department) on Admin and Roster dashboards.
- **Team Analytics Reports**: Aggregated metric reporting tab inside `AnalyticsScreen` for custom teams.

### Changed
- Database schema migration from Room v12 to v13 with `CompanyEntity` and multi-company relationships.

---

## [1.1.0] - 2026-10-05

**Version Code:** `2`  
**Git Commit:** `96007db`  

### Added
- **Date Range Filters**: Added custom start date and end date filtering for EOD history screens.
- **Firestore Pagination**: Added cursor-based pagination for EOD history queries with a default limit of 50.
- **Offline Sync Manager**: Integrated local Room database with background network listeners for offline-first submission.

---

## [1.0.0] - 2026-09-29

**Version Code:** `1`  
**Git Commit:** `fae2d69`  

### Added
- **Initial Release of WorkCore**:
  - Android application built with Kotlin, Jetpack Compose, Room SQLite, and Firebase.
  - Daily EOD submission form for core engineering teams.
  - Role-based login (Admin and Employee).
  - Department roster tracking and daily submission monitoring.
