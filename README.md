# appp1 — Visión Artificial y Procesamiento de Imágenes

Proyecto del **4to Elemento** — Programación de Dispositivos Móviles  
**Autora:** Gabriela Maite Arauco Porrez  
**Repo:** MaiteAraucoPorrez/Proyecto_vision_artificial (branch: `feature/visionscreen-gallery`)  
**Última actualización:** 2026-11-13 — **40% COMPLETADO** ✅

---

## 🎯 Descripción

App Android/KMP que usa **ML Kit de Google** (on-device, sin internet) para:
- **Image Labeling** — detectar objetos/etiquetas en fotos (17.0.9)
- **Text Recognition (OCR)** — extraer texto latino de imágenes (16.0.1)
- Guardar historial de análisis en **Room Database** (100%)

Arquitectura: **Clean Architecture + MVI** (State / Event / Effect + ViewModel)  
DI: **Koin** | UI: **Compose Multiplatform** | Image Loading: **Coil 3** | ML: **ML Kit**

---

## 🚀 Comandos principales

```bash
# Build completo + detekt + tests (ejecutar ANTES de hacer push)
./gradlew :shared:check :androidApp:assembleDebug --no-configuration-cache

# Solo detekt (debe pasar con 0 issues)
./gradlew :shared:detekt --no-configuration-cache

# Tests unitarios (todos los targets)
./gradlew :shared:allTests --no-configuration-cache

# Compilar solo Android
./gradlew :androidApp:assembleDebug --no-configuration-cache

# Instalar en emulador/dispositivo
./gradlew :androidApp:installDebug --no-configuration-cache
```

---

## 📦 Estructura del proyecto (40% — Actual)

```
proyecto_4to_elem/
├── androidApp/                    # App Android
│   ├── src/main/kotlin/org/ucb/appp1/
│   │   ├── MainActivity.kt        # setContent { App() }
│   │   └── MainApplication.kt     # initKoinAndroid(this)
│   └── build.gradle.kts
│
├── shared/                        # Código compartido (KMP)
│   ├── src/
│   │   ├── commonMain/kotlin/org/ucb/appp1/
│   │   │   ├── core/
│   │   │   │   └── util/          # SafeCall, Time (currentTimestamp expect)
│   │   │   ├── di/                # Koin modules
│   │   │   │   ├── DataModule.kt
│   │   │   │   ├── DomainModule.kt
│   │   │   │   ├── PresentationModule.kt
│   │   │   │   ├── PlatformModule.kt (expect)
│   │   │   │   └── SharedModules.kt
│   │   │   ├── feature/
│   │   │   │   └── vision/        # 🌟 Feature principal — 40% DONE
│   │   │   │       ├── data/
│   │   │   │       │   ├── datasource/     # ImageAnalyzerDataSource (interface expect)
│   │   │   │       │   └── repository/     # VisionRepository interface + Impl
│   │   │   │       ├── domain/
│   │   │   │       │   ├── model/          # VisionResult
│   │   │   │       │   ├── repository/     # VisionRepository
│   │   │   │       │   └── usecase/        # AnalyzeImageUseCase
│   │   │   │       └── presentation/
│   │   │   │           ├── screen/         # VisionScreen (expect fun)
│   │   │   │           └── viewmodel/      # VisionState/Event/Effect/ViewModel
│   │   │   ├── navigation/      # NavRoute (sealed), AppNavHost
│   │   │   └── App.kt           # Entry point Compose
│   │   │
│   │   ├── androidMain/kotlin/  # 🎯 Implementaciones Android REALES
│   │   │   ├── di/PlatformModule.android.kt — provides MlKitImageAnalyzer
│   │   │   └── feature/vision/
│   │   │       ├── data/datasource/MlKitImageAnalyzer.kt — ML Kit REAL
│   │   │       └── presentation/screen/VisionScreen.kt — Gallery + Coil + Effects
│   │   │
│   │   ├── iosMain/kotlin/      # Stubs iOS (no compila en Windows)
│   │   │   ├── di/PlatformModule.ios.kt — provides Stub
│   │   │   └── feature/vision/presentation/screen/VisionScreen.kt — Stub UI
│   │   └── commonTest/kotlin/   # Tests unitarios
│   └── build.gradle.kts
│
├── iosApp/                        # App iOS (no compila en Windows)
├── ds-repo/                       # Design System Maven repo (versionado en Git)
├── config/detekt/detekt.yml       # Configuración detekt (estricta, 0 issues)
├── gradle/libs.versions.toml      # Version Catalog
└── settings.gradle.kts
```

---

## ✨ Features implementadas (40% — 13 Nov)

| Feature | Descripción | Estado |
|---------|-------------|--------|
| **Vision — Gallery Picker** | Selector de imágenes nativo (`ActivityResultContracts.GetContent`) | ✅ **DONE** |
| **Vision — Image Preview** | Coil 3 `AsyncImage` con placeholder/error, 300dp rounded | ✅ **DONE** |
| **Vision — ML Kit Labeling** | Image Labeling 17.0.9 on-device, objetos detectados | ✅ **DONE** |
| **Vision — ML Kit OCR** | Text Recognition 16.0.1 latino on-device, bloques de texto | ✅ **DONE** |
| **Vision — Parallel Analysis** | Labeler + OCR simultáneo con `Tasks.await()` en `Dispatchers.IO` | ✅ **DONE** |
| **Vision — MVI + expect/actual** | State/Event/Effect + ViewModel + platform-specific Screen/DataSource | ✅ **DONE** |
| **Room Database** | Historial de análisis (Entity, DAO, Repository, Screen) | ⏳ **100%** |
| **CameraX Realtime** | Vista previa cámara + análisis frame a frame + bounding boxes | ⏳ **100%** |
| **Navigation** | Compose Navigation (NavRoute sealed class, 3 destinos) | ✅ **DONE** |
| **DI (Koin)** | Modules por capa + platformModule (Android/iOS) | ✅ **DONE** |
| **Detekt** | Lint estricto (0 issues, FunctionNaming excluye @Composable) | ✅ **PASSING** |
| **Design System** | `edu.ucb.ds:shared:1.0.0-SNAPSHOT` desde `ds-repo/` Maven local | ✅ **INTEGRATED** |

---

## 🏗️ Arquitectura del Feature `vision` (40%)

### Domain Layer (`commonMain`)
- `VisionResult` — data class inmutable (imageUri, labels:List<String>, texts:List<String>, timestamp:Long)
- `VisionRepository` — interface `suspend fun analyzeImage(imageUri: String): VisionResult`
- `AnalyzeImageUseCase` — `operator fun invoke(uri: String): Result<VisionResult>` (runCatching)

### Data Layer
- **commonMain:** `ImageAnalyzerDataSource` (interface expect), `StubImageAnalyzerDataSource` (fake data), `VisionRepositoryImpl` (delega a DataSource)
- **androidMain:** `MlKitImageAnalyzer` — **REAL ML Kit** con `ImageLabeler` + `TextRecognizer` en paralelo
- **iosMain:** `StubImageAnalyzerDataSource` — stub para compilar

### Presentation Layer — MVI (`commonMain`)
- `VisionState` — `data class(isLoading, error, selectedImageUri, result)` inmutable
- `VisionEvent` — `sealed interface`: `OnSelectImageClick`, `OnImageSelected(uri)`, `OnAnalyzeClick`, `OnClearResult`
- `VisionEffect` — `sealed interface`: `OpenImagePicker`, `ShowError(message)` (one-shot via SharedFlow)
- `VisionViewModel` — `StateFlow<VisionState>` + `SharedFlow<VisionEffect>`, `emitEvent()` delega a UseCase
- **androidMain:** `VisionScreen` — Gallery launcher, Coil preview, `LaunchedEffect` colecta effects
- **iosMain:** `VisionScreen` — Stub UI con botones básicos

---

## 🔑 Configuración local (NO en Git)

Crear `local.properties` en la raíz:

```properties
# API key de TMDB (para features futuras de catálogo — 100%)
tmdb.apiKey=TU_API_KEY_TMDB

# google-services.json NO NECESARIO para 40% (Firebase removido)
```

> **Nota:** Para 100%, `google-services.json` va en `androidApp/` — pásalo al docente por privado si se reactiva FCM.

---

## 🧪 Testing & Verificación

```bash
# Verificación COMPLETA (DEBE PASAR antes de push)
./gradlew :shared:check :androidApp:assembleDebug --no-configuration-cache

# Solo detekt (0 issues requeridos)
./gradlew :shared:detekt --no-configuration-cache

# Tests unitarios (todos targets)
./gradlew :shared:allTests --no-configuration-cache

# Compilar solo Android main
./gradlew :shared:compileAndroidMain --no-configuration-cache

# Instalar y probar en emulador/dispositivo
./gradlew :androidApp:installDebug --no-configuration-cache
```

### Qué debería pasar al hacer **Run** (androidApp):
1. Pantalla "Visión Artificial" + subtítulo + botón **"Seleccionar Imagen de Galería"**
2. Toca botón → Se abre **selector nativo de galería/archivos**
3. Elige imagen → **Preview 300dp** con esquinas redondeadas + botón **X** (limpiar) + botón **"Analizar Imagen"**
4. Toca **"Analizar Imagen"** → Loading indicator + botón disabled
5. **1-3 seg** → 2 Cards con elevation:
   - **"Objetos detectados (N)"** → lista labels (ej: Person, Car, Dog, Tree)
   - **"Texto detectado (M bloques)"** → lista textos OCR (ej: STOP, EXIT, señales)
6. Toca **X** o **"Limpiar"** → Vuelve a estado inicial

---

## 📱 Desarrollo en Windows

- iOS **no compila** en Windows (requiere macOS/Xcode)
- Para probar iOS: usar Mac o CI/CD (GitHub Actions macOS runner)
- Targets iOS (`iosArm64`, `iosSimulatorArm64`) existen en `shared/build.gradle.kts` pero solo compilan en macOS

---

## 📝 Plan de entregas (4to Elemento)

| Fecha | Entrega | Qué incluye | Estado |
|-------|---------|-------------|--------|
| **9 Nov** | 20% | Estructura base + UI placeholder + arquitectura MVI compilando | ✅ **DONE** |
| **13 Nov** | 40% | Galería + ML Kit Image Labeling + OCR funcionando | ✅ **DONE** |
| **16 Nov** | 100% | Room historial + CameraX realtime + UI pulida (dark/high-contrast) + iOS Vision + Manual | 🎯 **NEXT** |
| **20 Nov** | Exposición | Defensa oral + autoevaluación + heteroevaluación | ⏳ **PENDING** |

---

## 🔗 Recursos útiles

- [ML Kit Image Labeling](https://developers.google.com/ml-kit/vision/image-labeling)
- [ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition)
- [Coil 3 Image Loading](https://coil-kt.github.io/coil/compose/)
- [Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
- [Koin DI Multiplatform](https://insert-koin.io/docs/reference/koin-multiplatform/)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Activity Result APIs](https://developer.android.com/training/basics/intents/result)