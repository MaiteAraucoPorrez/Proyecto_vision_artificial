# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**App:** `appp1` (package: `org.ucb.appp1`)
**Type:** Kotlin Multiplatform (KMP) with Compose Multiplatform
**Modules:** `shared` (commonMain, androidMain, iosMain), `androidApp`, `iosApp`
**Development:** Windows (iOS does not compile here)
**Architecture:** Clean Architecture + MVVM/MVI
**DI:** Koin
**Network:** Ktor (available, not currently used in vision feature)
**Local DB:** Room 3 (androidx.room3) — **pending for 100% (historial)**
**Build:** Gradle Kotlin DSL with Version Catalog (`gradle/libs.versions.toml`)

**Key Versions:**
- AGP: 9.3.3
- Kotlin: 2.4.21
- Compose Multiplatform: 1.12.2
- minSdk: 24, compileSdk: 37, targetSdk: 36
- JVM Target: 17 (shared), 11 (androidApp with coreLibraryDesugaring)

**Current Milestone:** 40% (13 Nov) — Gallery + ML Kit Image Labeling + Text Recognition working
**Next Milestone:** 100% (16 Nov) — Room historial, CameraX realtime, UI polish, iOS Vision Framework

---

## Commands

### Build & Verify (run before pushing)
```bash
# Full check (detekt + tests + assemble)
./gradlew :shared:check :androidApp:assembleDebug

# Detekt only (must pass with 0 issues)
./gradlew :shared:detekt

# Generate Detekt config (if needed)
./gradlew :shared:detektGenerateConfig

# Download Localise.biz strings (Loco)
./gradlew :shared:downloadLocoStrings
```

### Run Tests
```bash
# All shared tests
./gradlew :shared:commonTest

# Single test class
./gradlew :shared:commonTest --tests "org.ucb.appp1.FindAliasUseCaseTest"

# Android instrumented tests (requires emulator/device)
./gradlew :androidApp:connectedAndroidTest
```

### Generate Secrets (API keys)
```bash
# Reads tmdb.apiKey from local.properties or TMDB_API_KEY env var
./gradlew :shared:generateSecrets
```

---

## Architecture

### Module Structure (Actual — 40%)
```
shared/src/
├── commonMain/kotlin/org/ucb/appp1/
│   ├── core/                    # Core utilities
│   │   └── util/                # SafeCall, Time (currentTimestamp expect)
│   ├── di/                      # Koin modules
│   │   ├── DataModule.kt        # VisionRepository, ImageAnalyzerDataSource (stub)
│   │   ├── DomainModule.kt      # AnalyzeImageUseCase
│   │   ├── PresentationModule.kt # VisionViewModel
│   │   ├── PlatformModule.kt    # expect fun platformModule(): Module
│   │   └── SharedModules.kt     # List of all modules
│   ├── navigation/              # NavRoute (sealed), AppNavHost
│   ├── feature/                 # Feature modules (3-layer)
│   │   └── vision/              # 🌟 MAIN FEATURE — 40% COMPLETE
│   │       ├── data/
│   │       │   ├── datasource/  # ImageAnalyzerDataSource (interface expect)
│   │       │   └── repository/  # VisionRepository interface + VisionRepositoryImpl
│   │       ├── domain/
│   │       │   ├── model/       # VisionResult
│   │       │   ├── repository/  # VisionRepository
│   │       │   └── usecase/     # AnalyzeImageUseCase
│   │       └── presentation/
│   │           ├── viewmodel/   # VisionState, VisionEvent, VisionEffect, VisionViewModel
│   │           └── screen/      # VisionScreen (expect fun)
│   └── platform/                # Platform.kt (expect/actual)
├── androidMain/kotlin/
│   ├── di/                      # PlatformModule.android.kt (MlKitImageAnalyzer)
│   └── feature/vision/
│       ├── data/datasource/     # MlKitImageAnalyzer (actual - ML Kit real)
│       └── presentation/screen/ # VisionScreen (actual - gallery, Coil, effects)
├── iosMain/kotlin/
│   ├── di/                      # PlatformModule.ios.kt (StubImageAnalyzerDataSource)
│   └── feature/vision/
│       └── presentation/screen/ # VisionScreen (actual - stub UI)
└── commonTest/kotlin/           # Unit tests (kotlinx.coroutines.test)
```

### Feature Layer Pattern (each feature)
```
feature/<name>/
├── domain/
│   ├── model/           # Domain models (data classes)
│   ├── repository/      # Repository interface
│   └── usecase/         # Use cases (suspend operator fun invoke)
├── data/
│   ├── datasource/      # DataSource interface
│   ├── service/         # Service implementation (Ktor, Firebase, etc.)
│   ├── mapper/          # DTO ↔ Domain (or direct Wire models)
│   └── repository/      # RepositoryImpl
└── presentation/
    ├── viewmodel/
    │   ├── XState.kt        # data class, immutable
    │   ├── XEvent.kt        # sealed class (OnClick, OnInputChanged...)
    │   ├── XEffect.kt       # sealed class (ShowToast, Navigate...)
    │   └── XViewModel.kt    # StateFlow state, SharedFlow effects, emitEvent()
    └── screen/
        └── XScreen.kt       # Composable, koinViewModel()
```

### Presentation Pattern (Catalog-style)
- **State:** `data class XState(...)` — single source of truth
- **Events:** `sealed class XEvent` — user actions
- **Effects:** `sealed class XEffect` — one-time side effects (navigation, toasts)
- **ViewModel:**
  - `private val _state = MutableStateFlow(XState())` → `val state = _state.asStateFlow()`
  - `private val _effect = MutableSharedFlow<XEffect>()` → `val effect = _effect.asSharedFlow()`
  - `fun emitEvent(event: XEvent) = viewModelScope.launch { when(event) { ... } }`
  - Calls **use cases**, never repositories directly
- **Screen:** `Composable` with `koinViewModel()`, collects state, sends events

### Dependency Injection (Koin)
- Modules defined in `shared/src/commonMain/kotlin/org/ucb/appp1/di/`
- `sharedModules()` returns: `platformModule()`, `dataModule`, `domainModule`, `presentationModule`
- Feature-specific modules added to the list
- Android `platformModule()` actual provides `AppDatabase` via Room builder
- iOS `platformModule()` actual provides stubs

### Error Handling
- Use `Result<T>` everywhere, chain with `.onSuccess { }` / `.onFailure { }`
- **Never** write manual `catch (e: Exception)` — detekt flags `TooGenericExceptionCaught`
- Use `safeCall { }` from `core/util/SafeCall.kt`:
  ```kotlin
  inline fun <T> safeCall(block: () -> T): Result<T> =
      try { Result.success(block()) }
      catch (e: CancellationException) { throw e }
      catch (e: Exception) { Result.failure(e) }
  ```
- `safeCall` rethrows `CancellationException` to not break coroutine cancellation

### Navigation
- `NavRoute` = `@Serializable sealed class` with data objects/classes
- `AppNavHost` uses type-safe `composable<NavRoute.X>`
- Start destination set in `AppNavHost` (currently `NavRoute.Crossref`)

### Expect/Actual Classes (with `-Xexpect-actual-classes`)
- `AppDatabaseConstructor` (Room)
- `LogScheduler` (WorkManager)
- `Platform` (device info)
- Flag in `shared/build.gradle.kts`: `freeCompilerArgs.add("-Xexpect-actual-classes")`

---

## Key Features Implemented (40% — 13 Nov)

| Feature | Description | Key Tech |
|---------|-------------|----------|
| **Vision (ML Kit)** | Image Labeling + Text Recognition on-device | `com.google.mlkit:image-labeling:17.0.9`, `text-recognition:16.0.1` |
| **Gallery Picker** | Select image from system gallery | `ActivityResultContracts.GetContent()`, `rememberLauncherForActivityResult` |
| **Image Preview** | Coil 3 AsyncImage with placeholder/error | `io.coil-kt.coil3:coil-compose:3.6.3` |
| **Parallel ML Analysis** | Labeler + OCR run simultaneously | `Tasks.await()` on `Dispatchers.IO` |
| **expect/actual Pattern** | Platform-specific VisionScreen & DataSource | `@file:JvmName`, Koin `platformModule()` |
| **MVI Architecture** | State/Event/Effect + ViewModel + Compose | `StateFlow`, `SharedFlow`, `koinViewModel()` |
| **Design System** | External repo, Maven local (`ds-repo/`) | `edu.ucb.ds:shared:1.0.0-SNAPSHOT` |
| **Detekt** | Strict config, 0 issues required | `config/detekt/detekt.yml` |
| **Navigation** | Type-safe Compose Navigation | `NavRoute` sealed class, `AppNavHost` |

---

## Features Pending (100% — 16 Nov)

| Feature | Description | Key Tech |
|---------|-------------|----------|
| **Room 3 Database** | Historial de análisis (Entity, DAO, Repository) | `androidx.room3`, KSP, BundledSQLiteDriver |
| **CameraX Realtime** | Vista previa cámara + análisis frame a frame | `androidx.camera:camera-mlkit-vision` |
| **UI Polish** | Dark mode, High Contrast (4 paletas), Empty states | Design System tokens, `MyProjectTheme` |
| **iOS Vision Framework** | `MlKitImageAnalyzer.ios.kt` → Apple Vision | `VNRecognizeObjectsRequest`, `VNRecognizeTextRequest` |
| **Full Testing** | Unit (UseCases), UI (ComposeTestRule), Instrumented | `kotlinx.coroutines.test`, `androidx.compose.ui.test` |
| **User Manual** | PDF con capturas, diagrama arquitectura | Mermaid/PlantUML |

---

---

## Design System (External Repo)

- **Repo:** `../design-system` (MaiteAraucoPorrez/DesignSystem)
- **Published:** `ds-repo/` folder in this repo (Maven repo, versioned)
- **Usage:** `implementation("edu.ucb.ds:shared:1.0.0-SNAPSHOT")`
- **Sync:** In design-system run `./gradlew :shared:publishAllPublicationsToAppRepoRepository`, commit `ds-repo/`
- **Tokens:** Colors, Typography, Spacing → `MyProjectTheme`
- **Components:** PrimaryButton, SecondaryButton, AppTextField, AppCard, AppSurface, AppList, AppLabel, UserIcon
- **High Contrast:** Four palettes, `MyProjectTheme(highContrast = false)`, `dsColorsFor()`

---

## Important Files

### Gradle Configuration
- `gradle/libs.versions.toml` — all versions & libraries (ML Kit, Coil, Material Icons, Coroutines added)
- `shared/build.gradle.kts` — KMP config, ML Kit/Coil/Coroutines deps in androidMain, Detekt
- `androidApp/build.gradle.kts` — Android app, minimal deps (no google-services, no firebase)
- `settings.gradle.kts` — includes `ds-repo` Maven repository

### Core Utilities
- `shared/src/commonMain/kotlin/org/ucb/appp1/core/util/SafeCall.kt` — `safeCall { }`
- `shared/src/commonMain/kotlin/org/ucb/appp1/core/util/Time.kt` — `currentTimestamp()` expect/actual

### Vision Feature (40% Complete)
- `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/domain/model/VisionResult.kt`
- `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/domain/repository/VisionRepository.kt`
- `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/domain/usecase/AnalyzeImageUseCase.kt`
- `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/data/datasource/ImageAnalyzerDataSource.kt` (expect)
- `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/data/datasource/StubImageAnalyzerDataSource.kt`
- `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/data/repository/VisionRepositoryImpl.kt`
- `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/presentation/viewmodel/VisionState.kt`
- `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/presentation/viewmodel/VisionEvent.kt`
- `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/presentation/viewmodel/VisionEffect.kt`
- `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/presentation/viewmodel/VisionViewModel.kt`
- `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/presentation/screen/VisionScreen.kt` (expect)

### Android Actuals (Real Implementation)
- `shared/src/androidMain/kotlin/org/ucb/appp1/di/PlatformModule.android.kt` — provides MlKitImageAnalyzer
- `shared/src/androidMain/kotlin/org/ucb/appp1/feature/vision/data/datasource/MlKitImageAnalyzer.kt` — ML Kit real
- `shared/src/androidMain/kotlin/org/ucb/appp1/feature/vision/presentation/screen/VisionScreen.kt` — gallery, Coil, effects

### iOS Actuals (Stubs)
- `shared/src/iosMain/kotlin/org/ucb/appp1/di/PlatformModule.ios.kt` — provides Stub
- `shared/src/iosMain/kotlin/org/ucb/appp1/feature/vision/presentation/screen/VisionScreen.kt` — stub UI

### DI Modules
- `shared/src/commonMain/kotlin/org/ucb/appp1/di/SharedModules.kt`
- `shared/src/commonMain/kotlin/org/ucb/appp1/di/DataModule.kt`
- `shared/src/commonMain/kotlin/org/ucb/appp1/di/DomainModule.kt`
- `shared/src/commonMain/kotlin/org/ucb/appp1/di/PresentationModule.kt`
- `shared/src/androidMain/kotlin/org/ucb/appp1/di/PlatformModule.android.kt`
- `shared/src/iosMain/kotlin/org/ucb/appp1/di/PlatformModule.ios.kt`

### Navigation
- `shared/src/commonMain/kotlin/org/ucb/appp1/navigation/NavRoute.kt`
- `shared/src/commonMain/kotlin/org/ucb/appp1/navigation/AppNavHost.kt`

### App Entry
- `shared/src/commonMain/kotlin/org/ucb/appp1/App.kt`
- `androidApp/src/main/kotlin/org/ucb/appp1/MainActivity.kt`
- `androidApp/src/main/kotlin/org/ucb/appp1/MainApplication.kt`

---

## Testing

- **Framework:** `kotlin.test` + `kotlinx.coroutines.test` (`runTest`)
- **Location:** `shared/src/commonTest/kotlin/`
- **Pattern:** Fake repositories, test use cases in isolation
- **Run all:** `./gradlew :shared:allTests` (includes androidHostTest + commonTest)
- **Current tests:** Basic shared test dummy (features tests removed during cleanup)
- **To add for 100%:** VisionViewModelTest, AnalyzeImageUseCaseTest, MlKitImageAnalyzerTest (with fake Context), VisionRepositoryImplTest

---

## Local Development Setup

### Required Files (NOT in Git)
- `local.properties`:
  ```properties
  tmdb.apiKey=YOUR_TMDB_KEY
  # loco.apiKey=YOUR_LOCO_EXPORT_KEY (Loco removed for 40% scope)
  ```
- `androidApp/google-services.json` — **NOT NEEDED for 40%** (Firebase removed). Keep for 100% if push notifications added.

### 40% Scope — What's Removed (Cleaned Up)
| Feature | Files Removed |
|---------|---------------|
| Room 3 | Database.kt, *Database.android.kt, *Database.ios.kt, schemas/ |
| WorkManager | LogScheduler.kt, LogScheduler.android.kt, LogUploadWorker.kt |
| Firebase (RTDB, Config, Firestore, Messaging) | All firebase deps, modules, services |
| gRPC/Wire | LogsClientFactory, proto files, wire plugin |
| Loco/Localise.biz | loco.gradle.kts, downloadLocoStrings task |
| User Information | FindAliasUseCase, GithubRepository, UserInformationScreen |
| Announcements, Remote Config, Push, Logs modules | Entire DI modules + feature code |

### Testing on Windows
- iOS **does not compile** on Windows (requires macOS/Xcode)
- iOS targets exist in `shared/build.gradle.kts` but compile only on macOS
- For iOS testing: use Mac or CI/CD (GitHub Actions with macOS runner)

---

## Git Workflow

- One branch per feature: `feature/<name>`
- PR to `main`
- Commits: `feat(scope): Added ...` or `fix(scope): Fixed ...`
- **No** `Co-Authored-By: Claude` in commits
- **No** Claude signature in PRs
- `gh` not installed — PRs via web: `.../compare/main...<branch>?expand=1&title=...&body=...`

### .gitignore Includes
- `google-services.json`
- `local.properties`
- `CLAUDE.md` (keys outside repo)

---

## Common Issues & Fixes

| Issue | Solution |
|-------|----------|
| `expect fun` not found | Must be top-level, not inside a class |
| Room schema export fails | Check `room3 { schemaDirectory("$projectDir/schemas") }` |
| KSP iOS targets | Use `kspIosArm64`, `kspIosSimulatorArm64` (no `kspIosX64`) |
| Detekt `TooGenericExceptionCaught` | Use `safeCall { }` instead of manual try/catch |
| FunctionNaming on `@Composable` | Excluded in detekt.yml via `ignoreAnnotated: ['Composable']` |
| Max line length 120 | Configured in detekt.yml |
| Long paths on Windows | `git config --global core.longpaths true` or clone to short path |

---

## Pending / To Test (100% — 16 Nov)

- [ ] Room 3 Database: Entity, DAO, Repository, UseCases, HistoryViewModel, HistoryScreen
- [ ] CameraX Real-time: PreviewView + ML Kit analyzer + bounding boxes overlay
- [ ] Design System demo: Dark mode switch, High Contrast (4 palettes), Empty states
- [ ] iOS Vision Framework: `MlKitImageAnalyzer.ios.kt` with `VNRecognizeObjectsRequest` + `VNRecognizeTextRequest`
- [ ] Full Testing: VisionViewModelTest, AnalyzeImageUseCaseTest, UI tests (ComposeTestRule), Instrumented Room tests
- [ ] User Manual (PDF): Screenshots galería, análisis, historial, settings + Architecture diagram (Mermaid)
- [ ] Autoevaluación + Heteroevaluación ready for 20 Nov exposition
- [ ] Regenerate TMDB API key (previous one in public history)
- [ ] Pass `google-services.json` (if needed for 100%) and `tmdb.apiKey` to teacher privately