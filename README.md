# appp1 — Visión Artificial y Procesamiento de Imágenes

Proyecto del **4to Elemento** — Programación de Dispositivos Móviles  
**Autora:** Gabriela Maite Arauco Porrez  
**Repo:** MaiteAraucoPorrez/ucbp1_project_base (branch: `feature/vision`)

---

## 🎯 Descripción

App Android/KMP que usa **ML Kit de Google** (on-device, sin internet) para:
- **Image Labeling** — detectar objetos/etiquetas en fotos
- **Text Recognition (OCR)** — extraer texto de imágenes
- Guardar historial de análisis en **Room Database**

Arquitectura: **Clean Architecture + MVI** (State / Event / Effect + ViewModel)  
DI: **Koin** | UI: **Compose Multiplatform** | Network: **Ktor** (si se necesita)

---

## 🚀 Comandos principales

```bash
# Build completo + detekt + tests (ejecutar ANTES de hacer push)
./gradlew :shared:check :androidApp:assembleDebug

# Solo detekt (debe pasar con 0 issues)
./gradlew :shared:detekt

# Tests unitarios
./gradlew :shared:testAndroid

# Compilar solo Android
./gradlew :androidApp:assembleDebug
```

---

## 📦 Estructura del proyecto

```
proyecto_4to_elem/
├── androidApp/                    # App Android
│   ├── src/main/kotlin/org/ucb/appp1/
│   │   ├── MainActivity.kt
│   │   └── MainApplication.kt
│   └── build.gradle.kts
│
├── shared/                        # Código compartido (KMP)
│   ├── src/
│   │   ├── commonMain/kotlin/org/ucb/appp1/
│   │   │   ├── core/
│   │   │   │   ├── navigation/    # NavRoute, AppNavHost
│   │   │   │   └── util/          # SafeCall, Time
│   │   │   ├── di/                # Koin modules
│   │   │   │   ├── DataModule.kt
│   │   │   │   ├── DomainModule.kt
│   │   │   │   ├── PresentationModule.kt
│   │   │   │   ├── PlatformModule.kt (expect)
│   │   │   │   └── SharedModules.kt
│   │   │   ├── feature/
│   │   │   │   └── vision/        # 🌟 Feature principal
│   │   │   │       ├── data/
│   │   │   │       │   ├── datasource/     # ImageAnalyzerDataSource (interface)
│   │   │   │       │   ├── repository/     # VisionRepositoryImpl
│   │   │   │       │   └── model/          # VisionResult (domain model)
│   │   │   │       ├── domain/
│   │   │   │       │   ├── model/          # VisionResult
│   │   │   │       │   ├── repository/     # VisionRepository (interface)
│   │   │   │       │   └── usecase/        # AnalyzeImageUseCase
│   │   │   │       └── presentation/
│   │   │   │           ├── screen/         # VisionScreen
│   │   │   │           └── viewmodel/      # VisionState, VisionEvent, VisionEffect, VisionViewModel
│   │   │   ├── Platform.kt      # expect/actual platform info
│   │   │   └── App.kt           # Entry point Compose
│   │   │
│   │   ├── androidMain/kotlin/  # Implementaciones Android
│   │   │   ├── di/PlatformModule.android.kt (actual)
│   │   │   └── feature/vision/data/datasource/  # MlKitImageAnalyzer (expect/actual)
│   │   │
│   │   ├── iosMain/kotlin/      # Stubs iOS (no se compila en Windows)
│   │   └── commonTest/kotlin/   # Tests unitarios
│   └── build.gradle.kts
│
├── iosApp/                        # App iOS (no compila en Windows)
├── ds-repo/                       # Design System Maven repo (versionado)
├── config/detekt/detekt.yml       # Configuración detekt
├── gradle/libs.versions.toml      # Version Catalog
└── settings.gradle.kts
```

---

## ✨ Features implementadas

| Feature | Descripción | Estado |
|---------|-------------|--------|
| **Vision (ML Kit)** | Image Labeling + Text Recognition | 🚧 En desarrollo |
| **Room Database** | Historial de análisis | ⏳ Pendiente |
| **Navigation** | Compose Navigation (NavRoute sealed class) | ✅ Listo |
| **DI (Koin)** | Modules por feature + core | ✅ Listo |
| **Detekt** | Lint estricto (0 issues) | ✅ Pasando |
| **Design System** | `edu.ucb.ds:shared:1.0.0-SNAPSHOT` desde `ds-repo/` | ✅ Integrado |

---

## 🏗️ Arquitectura del Feature `vision`

### Domain Layer
- `VisionResult` — modelo de negocio (labels, texts, timestamp)
- `VisionRepository` — interface del repositorio
- `AnalyzeImageUseCase` — caso de uso `suspend operator fun invoke(uri: String): Result<VisionResult>`

### Data Layer
- `ImageAnalyzerDataSource` — interface (expect en commonMain)
- `MlKitImageAnalyzer` — implementación real con ML Kit (androidMain)
- `VisionRepositoryImpl` — implementa `VisionRepository`

### Presentation Layer (MVI)
- `VisionState` — data class inmutable
- `VisionEvent` — sealed interface (OnSelectImageClick, OnImageSelected, OnAnalyzeClick, OnClearResult)
- `VisionEffect` — sealed interface (OpenImagePicker, ShowError)
- `VisionViewModel` — StateFlow + SharedFlow, `emitEvent()`
- `VisionScreen` — Composable con `koinViewModel()`

---

## 🔑 Configuración local (NO en Git)

Crear `local.properties` en la raíz:

```properties
# API key de TMDB (para features futuras de catálogo)
tmdb.apiKey=TU_API_KEY_TMDB

# API key de Localise.biz (para descargar strings)
loco.apiKey=TU_EXPORT_KEY_LOCO
```

> **Nota:** `google-services.json` va en `androidApp/` — pásalo al docente por privado.

---

## 🧪 Testing

```bash
# Tests del módulo shared (JVM)
./gradlew :shared:testAndroid

# Tests específicos
./gradlew :shared:testAndroid --tests "org.ucb.appp1.*"
```

---

## 📱 Desarrollo en Windows

- iOS **no compila** en Windows (requiere macOS/Xcode)
- Para probar iOS: usar Mac o CI/CD
- Los targets iOS existen en `shared/build.gradle.kts` pero se compilan solo en macOS

---

## 📝 Plan de entregas (4to Elemento)

| Fecha | Entrega | Qué incluye |
|-------|---------|-------------|
| **9 Nov** | 20% | Estructura base + UI placeholder + arquitectura MVI compilando |
| **13 Nov** | 40% | Galería + ML Kit Image Labeling funcionando |
| **16 Nov** | 100% | Cámara tiempo real + OCR + Room DB + UI pulida + Manual |
| **20 Nov** | Exposición | Defensa oral + autoevaluación + heteroevaluación |

Ver [plan_proyecto_vision_artificial.md](plan_proyecto_vision_artificial.md) (archivo local, no se sube a Git).

---

## 🔗 Recursos útiles

- [ML Kit Image Labeling](https://developers.google.com/ml-kit/vision/image-labeling)
- [ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition)
- [Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
- [Koin DI](https://insert-koin.io/docs/reference/koin-multiplatform/)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)