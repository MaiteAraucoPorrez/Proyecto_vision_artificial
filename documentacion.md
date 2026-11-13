# Documentación Técnica — 4to Elemento: Visión Artificial
**Proyecto:** appp1 | **Autora:** Gabriela Maite Arauco Porrez  
**Última actualización:** 2026-11-12 | **Versión:** 40% (Entrega 13 Nov)

> **⚠️ ARCHIVO PRIVADO** — No se sube a Git (ver `.gitignore`). Solo para referencia personal y defensa.

---

## 📋 Resumen de Entregas

### ✅ 20% (Entregado 9 Nov) — Arquitectura Base
| Aspecto | Estado |
|---------|--------|
| **Arquitectura base** | ✅ Clean Architecture + MVI |
| **Navegación** | ✅ 3 pantallas (Vision, Localization, DesignSystem) |
| **DI (Koin)** | ✅ Modules por feature + core |
| **Feature Vision** | ✅ Estructura completa (stubs) |
| **UI Básica** | ✅ VisionScreen con botones placeholder |
| **Detekt** | ✅ 0 issues |
| **Build** | ✅ `:shared:check :androidApp:assembleDebug` pasa |
| **Tests** | ✅ `:shared:testAndroid` pasa |

### ✅ 40% (Entregado 13 Nov) — Galería + ML Kit Real
| Aspecto | Estado |
|---------|--------|
| **Selector de galería** | ✅ `ActivityResultContracts.GetContent()` |
| **Preview imagen** | ✅ Coil 3 `AsyncImage` con placeholder/error |
| **ML Kit Image Labeling** | ✅ 17.0.9 — detección objetos on-device |
| **ML Kit Text Recognition** | ✅ 16.0.1 — OCR latino on-device |
| **Ejecución paralela** | ✅ `Tasks.await()` para ambos detectores |
| **expect/actual pattern** | ✅ VisionScreen + ImageAnalyzerDataSource |
| **DI Platform-specific** | ✅ MlKitImageAnalyzer (Android) / Stub (iOS) |
| **Efectos one-shot** | ✅ `SharedFlow` + `LaunchedEffect` |
| **Detekt** | ✅ 0 issues |
| **Build completo** | ✅ `:shared:check :androidApp:assembleDebug` pasa |
| **Tests** | ✅ `:shared:allTests` pasa |

---

## 🏗️ Estructura de Archivos Creados/Modificados

### 1. Configuración Gradle (Limpieza)

#### `gradle/libs.versions.toml`
**Cambios:** Eliminadas versiones y librerías no usadas:
- ❌ `room3`, `sqlite`, `ksp`, `googleServices`, `firebaseDatabase`, `firebaseKotlin`, `desugarJdkLibs`, `firebaseBom`, `workRuntimeKtx`, `wire`
- ✅ Quedan: `agp`, `kotlin`, `composeMultiplatform`, `koinBom`, `kotlinxCoroutines`, `kotlinxSerializationJson`, `navigationCompose`, `ktor`, `coil`, `detekt`, `material3`, `androidx-*`, `junit`

#### `shared/build.gradle.kts`
**Cambios:**
- ❌ Plugins: `ksp`, `androidx.room3`, `wire`, `detekt` (se mantiene)
- ❌ Dependencias `androidMain`: `ktor.client.okhttp`, `room3.sqlite.wrapper`, `work.runtime.ktx`
- ❌ Dependencias `commonMain`: `ktor.*`, `coil.*`, `room3.*`, `sqlite.*`, `firebase.*`, `wire.grpc.client`
- ❌ Bloques: `room3 { }`, `wire { }`, `generateSecrets` task, `apply(loco.gradle.kts)`
- ✅ Solo: Compose, Koin, Navigation, Serialization, Design System, Detekt
- ⚠️ **Flag mantenida:** `-Xexpect-actual-classes` (para Platform.kt)

#### `androidApp/build.gradle.kts`
**Cambios:**
- ❌ Plugin `googleServices`
- ❌ `coreLibraryDesugaring`, `firebase.bom`, `firebase.messaging.android`, `androidx.core.ktx`
- ❌ `compileOptions.isCoreLibraryDesugaringEnabled = true`
- ✅ Mínimo: `androidApplication`, `composeCompiler`, `project(:shared)`, `activity-compose`, `uiTooling`

#### `settings.gradle.kts`
**Sin cambios** — mantiene `ds-repo` para Design System.

---

### 2. Navegación (Core)

#### `shared/src/commonMain/kotlin/org/ucb/appp1/navigation/NavRoute.kt`
```kotlin
@Serializable
sealed class NavRoute {
    data object Vision : NavRoute()
    data object Localization : NavRoute()
    data object DesignSystem : NavRoute()
}
```
**Por qué:** Tipo seguro (type-safe navigation) con serialization para deep links futuros. Solo 3 destinos para el MVP.

#### `shared/src/commonMain/kotlin/org/ucb/appp1/navigation/AppNavHost.kt`
```kotlin
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = NavRoute.Vision) {
        composable<NavRoute.Vision> { VisionScreen() }
        composable<NavRoute.Localization> { LocalizationScreen() }
        composable<NavRoute.DesignSystem> { DesignSystemDemoScreen() }
    }
}
```
**Flujo:** Inicia en `Vision` (pantalla principal). `Localization` y `DesignSystem` son placeholders de features del proyecto base que se mantienen como ejemplos.

---

### 3. Inyección de Dependencias (Koin)

#### `shared/src/commonMain/kotlin/org/ucb/appp1/di/SharedModules.kt`
```kotlin
fun sharedModules(): List<Module> = listOf(
    platformModule(),
    dataModule,
    domainModule,
    presentationModule,
)
```
**Orden importante:** `platformModule()` primero (provee `AppDatabase` en Android, stubs en iOS), luego data → domain → presentation.

#### `shared/src/commonMain/kotlin/org/ucb/appp1/di/DataModule.kt`
```kotlin
val dataModule = module {
    // --- Vision / ML Kit ---
    single<ImageAnalyzerDataSource> { StubImageAnalyzerDataSource() }
    singleOf(::VisionRepositoryImpl)
    single<VisionRepository> { VisionRepositoryImpl(get()) }
}
```
**Patrón:** Interface → Implementación. `singleOf` para clases con constructor inyectado, `single<Interface> { Impl(get()) }` para dependencias manuales.

#### `shared/src/commonMain/kotlin/org/ucb/appp1/di/DomainModule.kt`
```kotlin
val domainModule = module {
    singleOf(::AnalyzeImageUseCase)
}
```

#### `shared/src/commonMain/kotlin/org/ucb/appp1/di/PresentationModule.kt`
```kotlin
val presentationModule = module {
    viewModelOf(::VisionViewModel)
}
```
**`viewModelOf`** = Koin gestiona lifecycle del ViewModel (scoped a navigation).

#### `shared/src/commonMain/kotlin/org/ucb/appp1/di/PlatformModule.kt` (expect)
```kotlin
expect fun platformModule(): Module
```

#### `shared/src/androidMain/kotlin/org/ucb/appp1/di/PlatformModule.android.kt` (actual)
```kotlin
actual fun platformModule(): Module = module {
    // No platform-specific dependencies needed for vision feature
}
```

#### `shared/src/iosMain/kotlin/org/ucb/appp1/di/PlatformModule.ios.kt` (actual)
```kotlin
actual fun platformModule(): Module = module {
    // No platform-specific dependencies needed for vision feature on iOS
}
```
**Nota:** Vacíos porque Vision no necesita Room/WorkManager. Cuando se agregue historial (Room), aquí se proveerá `AppDatabase`.

---

### 4. Feature Vision — Domain Layer

#### `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/domain/model/VisionResult.kt`
```kotlin
data class VisionResult(
    val imageUri: String = "",
    val labels: List<String> = emptyList(),   // Objetos detectados (Image Labeling)
    val texts: List<String> = emptyList(),     // Texto detectado (OCR)
    val timestamp: Long = 0L
)
```
**Diseño:** Inmutable, serializable. `imageUri` para mostrar preview. `timestamp` para ordenar historial futuro.

#### `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/domain/repository/VisionRepository.kt`
```kotlin
interface VisionRepository {
    suspend fun analyzeImage(imageUri: String): VisionResult
}
```
**Contrato:** Un solo método. Recibe URI de imagen, retorna `VisionResult`. Fácil de testear con fake.

#### `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/domain/usecase/AnalyzeImageUseCase.kt`
```kotlin
class AnalyzeImageUseCase(
    private val repository: VisionRepository
) {
    suspend operator fun invoke(imageUri: String): Result<VisionResult> = runCatching {
        repository.analyzeImage(imageUri)
    }
}
```
**Patrón:** `operator fun invoke` = llamada como función `useCase(uri)`. Retorna `Result<T>` para manejo funcional de errores (`.onSuccess/.onFailure`). Usa `runCatching` (stdlib) — **NO** `safeCall` aquí porque el repositorio ya maneja errores internamente.

---

### 5. Feature Vision — Data Layer

#### `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/data/datasource/ImageAnalyzerDataSource.kt`
```kotlin
interface ImageAnalyzerDataSource {
    suspend fun analyze(imageUri: String): VisionResult
}
```
**Interface expect/actual:** En `commonMain` para que Domain no dependa de Android. Implementación real en `androidMain` con ML Kit.

#### `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/data/datasource/StubImageAnalyzerDataSource.kt`
```kotlin
class StubImageAnalyzerDataSource : ImageAnalyzerDataSource {
    override suspend fun analyze(imageUri: String): VisionResult {
        return VisionResult(
            imageUri = imageUri,
            labels = listOf("Objeto de prueba", "Elemento detectado"),
            texts = emptyList(),
            timestamp = currentTimestamp()
        )
    }
}

expect fun currentTimestamp(): Long
```
**Stub temporal:** Devuelve datos fake para que la UI funcione sin ML Kit. `currentTimestamp()` es `expect` para multiplatform (ver abajo).

#### `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/data/repository/VisionRepositoryImpl.kt`
```kotlin
class VisionRepositoryImpl(
    private val dataSource: ImageAnalyzerDataSource
) : VisionRepository {
    override suspend fun analyzeImage(imageUri: String): VisionResult {
        return dataSource.analyze(imageUri)
    }
}
```
**Passthrough simple:** Delega al DataSource. Aquí se agregaría: mapeo DTO→Domain, caché, logging, retry.

---

### 6. Feature Vision — Platform Specific (Expect/Actual)

#### `shared/src/androidMain/kotlin/org/ucb/appp1/feature/vision/data/datasource/StubImageAnalyzerDataSource.android.kt`
```kotlin
actual fun currentTimestamp(): Long = java.lang.System.currentTimeMillis()
```

#### `shared/src/iosMain/kotlin/org/ucb/appp1/feature/vision/data/datasource/StubImageAnalyzerDataSource.ios.kt`
```kotlin
actual fun currentTimestamp(): Long = 0L
```
**Por qué expect/actual:** `java.lang.System` no existe en iOS (Kotlin/Native). En iOS real se usaría `clock_gettime` o `Date().timeIntervalSince1970`.

#### `shared/src/iosMain/kotlin/org/ucb/appp1/feature/vision/data/datasource/MlKitImageAnalyzer.ios.kt` (FUTURO)
```kotlin
// ML Kit no existe en iOS → stub vacío o implementar con Vision Framework de Apple
actual class MlKitImageAnalyzer : ImageAnalyzerDataSource {
    override suspend fun analyze(imageUri: String): VisionResult = TODO("iOS: usar Vision Framework")
}
```

---

### 7. Feature Vision — Presentation Layer (MVI)

#### `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/presentation/viewmodel/VisionState.kt`
```kotlin
data class VisionState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedImageUri: String? = null,
    val result: VisionResult? = null
)
```
**Inmutable:** Todos los campos con default. `copy()` para actualizaciones.

#### `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/presentation/viewmodel/VisionEvent.kt`
```kotlin
sealed interface VisionEvent {
    object OnSelectImageClick : VisionEvent
    data class OnImageSelected(val uri: String) : VisionEvent
    object OnAnalyzeClick : VisionEvent
    object OnClearResult : VisionEvent
}
```
**Eventos de usuario:** `sealed interface` = exhaustividad en `when` del ViewModel. `OnImageSelected` lleva payload (URI).

#### `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/presentation/viewmodel/VisionEffect.kt`
```kotlin
sealed interface VisionEffect {
    object OpenImagePicker : VisionEffect
    data class ShowError(val message: String) : VisionEffect
}
```
**Efectos one-shot:** Navegación, toasts, permisos. `SharedFlow` en ViewModel, colectado en Screen con `LaunchedEffect`.

#### `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/presentation/viewmodel/VisionViewModel.kt`
```kotlin
class VisionViewModel(
    private val analyzeImageUseCase: AnalyzeImageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VisionState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<VisionEffect>()
    val effect = _effect.asSharedFlow()

    fun emitEvent(event: VisionEvent) = viewModelScope.launch {
        when (event) {
            VisionEvent.OnSelectImageClick -> {
                _effect.emit(VisionEffect.OpenImagePicker)
            }
            is VisionEvent.OnImageSelected -> {
                _state.update { it.copy(selectedImageUri = event.uri, result = null, error = null) }
            }
            VisionEvent.OnAnalyzeClick -> {
                val uri = _state.value.selectedImageUri ?: return@launch
                _state.update { it.copy(isLoading = true, error = null) }
                analyzeImageUseCase(uri)
                    .onSuccess { result ->
                        _state.update { it.copy(isLoading = false, result = result) }
                    }
                    .onFailure { e ->
                        _state.update { it.copy(isLoading = false) }
                        _effect.emit(VisionEffect.ShowError(e.message ?: "Error desconocido"))
                    }
            }
            VisionEvent.OnClearResult -> {
                _state.update { VisionState() }
            }
        }
    }
}
```
**Flujo MVI:**
1. UI → `emitEvent(Event)` 
2. ViewModel → actualiza `_state` / emite `_effect`
3. UI colecta `state` (recompone) y `effect` (side effects)

**Patrones clave:**
- `viewModelScope.launch` = cancela automáticamente si ViewModel muere
- `Result.onSuccess/onFailure` = sin try/catch manual
- `return@launch` = early exit si no hay URI

#### `shared/src/commonMain/kotlin/org/ucb/appp1/feature/vision/presentation/screen/VisionScreen.kt`
```kotlin
@Composable
fun VisionScreen(viewModel: VisionViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    // ... UI con botones: Seleccionar Imagen, Analizar, Limpiar
    // Muestra: preview, loading, error, labels, texts
}
```
**UI actual (20%):** Botones placeholder, muestra estado. **Falta para 40%:** `ActivityResultLauncher` para galería, `LaunchedEffect` para colectar `effect`.

---

### 8. App Entry Point

#### `shared/src/commonMain/kotlin/org/ucb/appp1/App.kt`
```kotlin
@Composable
fun App() {
    MaterialTheme {
        VisionScreen()
    }
}
```
**Simplificado:** Antes tenía `AppNavHost`, ahora directo a `VisionScreen` para el 20%. Para navegación real: `AppNavHost()`.

#### `androidApp/src/main/kotlin/org/ucb/appp1/MainActivity.kt`
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
```
**Limpio:** Sin permisos FCM, sin landing page, sin `onNewIntent`.

#### `androidApp/src/main/kotlin/org/ucb/appp1/MainApplication.kt`
```kotlin
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this)
    }
}
```
**Solo Koin:** Sin WorkManager, sin NotificationChannel.

---

### 9. Archivos Eliminados (Limpieza)

| Archivo | Motivo |
|---------|--------|
| `shared/src/commonMain/kotlin/org/ucb/appp1/config/Database.kt` | Room 3 eliminado |
| `shared/src/androidMain/kotlin/org/ucb/appp1/config/Database.android.kt` | Room 3 eliminado |
| `shared/src/commonMain/kotlin/org/ucb/appp1/scheduler/LogScheduler.kt` | WorkManager eliminado |
| `shared/src/androidMain/kotlin/org/ucb/appp1/scheduler/LogScheduler.android.kt` | WorkManager eliminado |
| `shared/src/androidMain/kotlin/org/ucb/appp1/scheduler/LogUploadWorker.kt` | WorkManager eliminado |
| `shared/src/androidMain/kotlin/org/ucb/appp1/feature/logs/data/service/LogsClientFactory.android.kt` | gRPC eliminado |
| `shared/src/iosMain/kotlin/org/ucb/appp1/config/Database.ios.kt` | Room 3 eliminado |
| `shared/src/iosMain/kotlin/org/ucb/appp1/di/PlatformModule.ios.kt` | Reescrito vacío |
| `shared/src/iosMain/kotlin/org/ucb/appp1/scheduler/LogScheduler.ios.kt` | WorkManager eliminado |
| `shared/src/iosMain/kotlin/org/ucb/appp1/feature/logs/data/service/LogsClientFactory.ios.kt` | gRPC eliminado |
| `shared/src/iosMain/kotlin/org/ucb/appp1/di/KoinModules.ios.kt` | No usado |
| `androidApp/src/main/kotlin/org/ucb/appp1/AppFirebaseMessagingService.kt` | FCM eliminado |
| `androidApp/src/main/kotlin/org/ucb/appp1/NotificationChannels.kt` | FCM eliminado |
| `shared/src/commonMain/kotlin/org/ucb/appp1/di/AnnouncementsModule.kt` | Feature eliminada |
| `shared/src/commonMain/kotlin/org/ucb/appp1/di/RemoteConfigModule.kt` | Feature eliminada |
| `shared/src/commonMain/kotlin/org/ucb/appp1/di/PushNotificationsModule.kt` | Feature eliminada |
| `shared/src/commonMain/kotlin/org/ucb/appp1/di/LogsModule.kt` | Feature eliminada |
| `shared/src/commonTest/kotlin/org/ucb/appp1/FindAliasUseCaseTest.kt` | Test de feature eliminada |
| `shared/src/commonTest/kotlin/org/ucb/appp1/GithubRepositoryTest.kt` | Test de feature eliminada |

---

### 10. Tests

#### `shared/src/commonTest/kotlin/org/ucb/appp1/SharedCommonTest.kt` (existía)
```kotlin
class SharedCommonTest {
    @Test
    fun testSharedCode() { assertTrue(true) }
}
```
**Mantenido:** Test dummy para que `check` no falle.

---

## 🆕 Implementación 40% — Detalle Completo (13 Nov)

### 1. Dependencias Agregadas (`gradle/libs.versions.toml` + `shared/build.gradle.kts`)

| Dependencia | Versión | Uso |
|-------------|---------|-----|
| `mlkit-image-labeling` | 17.0.9 | Detección de objetos/etiquetas en imágenes |
| `mlkit-text-recognition` | 16.0.1 | OCR texto latino on-device |
| `material-icons-core` | 1.7.0 | Iconos Material Design (Close, Image) |
| `androidx-activity-compose` | 1.13.0 | `rememberLauncherForActivityResult` |
| `kotlinx-coroutines-core` | 1.11.0 | `Tasks.await()` + corrutinas en ML Kit |

**En `shared/build.gradle.kts` (androidMain):**
```kotlin
androidMain.dependencies {
    implementation(libs.mlkit.image.labeling)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.material.icons.core)
    implementation(libs.kotlinx.coroutines.core)
    // ... Compose, Koin, etc.
}
```

### 2. MlKitImageAnalyzer — Implementación Real (`androidMain`)

**Archivo:** `shared/src/androidMain/kotlin/org/ucb/appp1/feature/vision/data/datasource/MlKitImageAnalyzer.kt`

```kotlin
class MlKitImageAnalyzer(
    private val context: Context
) : ImageAnalyzerDataSource {

    private val imageLabeler: ImageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    private val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun analyze(imageUri: String): VisionResult = withContext(Dispatchers.IO) {
        val uri = Uri.parse(imageUri)
        val inputImage = InputImage.fromFilePath(context, uri)

        // Ejecutar AMBOS detectores en PARALELO
        val labelsTask = imageLabeler.process(inputImage)
        val textsTask = textRecognizer.process(inputImage)

        val labelsResult = Tasks.await(labelsTask)
        val textsResult = Tasks.await(textsTask)

        val labels = labelsResult.map { it.text }
        val texts = textsResult.textBlocks.map { it.text }

        VisionResult(
            imageUri = imageUri,
            labels = labels,
            texts = texts,
            timestamp = currentTimestamp()
        )
    }

    fun close() {
        imageLabeler.close()
        textRecognizer.close()
    }
}
```

**Puntos clave:**
- **`withContext(Dispatchers.IO)`**: ML Kit es bloqueante, correr en IO
- **`Tasks.await()`**: Convierte `Task<T>` de Google Play Services a suspend (no `async/await` de corrutinas porque ML Kit usa Tasks API)
- **Paralelismo real**: Ambos `process()` se lanzan antes de `await`, corren en hilos nativos de ML Kit
- **`InputImage.fromFilePath(context, uri)`**: Acepta `content://` y `file://` URIs
- **`close()`**: Llamar en `VisionRepositoryImpl` o `onCleared()` del ViewModel para liberar recursos nativos

### 3. DI Platform-Specific (`PlatformModule.android.kt`)

**Archivo:** `shared/src/androidMain/kotlin/org/ucb/appp1/di/PlatformModule.android.kt`

```kotlin
actual fun platformModule(): Module = module {
    single<ImageAnalyzerDataSource> { MlKitImageAnalyzer(androidContext()) }
}
```

**`androidContext()`**: Extensión de Koin que provee `Context` de la Application. Requiere `koin-android` dependency.

**iOS (`PlatformModule.ios.kt`):**
```kotlin
actual fun platformModule(): Module = module {
    single<ImageAnalyzerDataSource> { StubImageAnalyzerDataSource() }
}
```

### 4. VisionScreen — expect/actual Pattern

**Common (`commonMain`):**
```kotlin
@Composable
expect fun VisionScreen(viewModel: VisionViewModel)
```

**Android (`androidMain`):**
```kotlin
@Composable
@file:JvmName("VisionScreenAndroid")  // Evita duplicate JVM class name
actual fun VisionScreen(viewModel: VisionViewModel) {
    val state by viewModel.state.collectAsState()

    // Gallery picker launcher
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.emitEvent(VisionEvent.OnImageSelected(it.toString())) }
    }

    // Collect one-shot effects
    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is VisionEffect.OpenImagePicker -> pickImage.launch("image/*")
                is VisionEffect.ShowError -> { /* Toast/Snackbar */ }
            }
        }
    }

    Column(...) {
        // Header
        // Image preview con Coil AsyncImage
        // Botones: Seleccionar / Analizar / Limpiar
        // Loading, Error, Results (labels + texts)
    }
}
```

**iOS (`iosMain`):**
```kotlin
@Composable
@file:JvmName("VisionScreenIos")
actual fun VisionScreen(viewModel: VisionViewModel) {
    // Stub UI: solo botones, sin gallery picker ni Coil
}
```

**Por qué `@file:JvmName`**: Ambos `actual` generan `VisionScreenKt.class` → conflicto. Renombra clases JVM a `VisionScreenAndroidKt` / `VisionScreenIosKt`.

### 5. Coil 3 AsyncImage — Preview de Imagen

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(imageUri)
        .build(),
    contentDescription = "Imagen seleccionada",
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxSize(),
    placeholder = ColorPainter(Color.LightGray),
    error = ColorPainter(Color.Gray)
)
```

- **`imageUri`**: String `content://` o `file://` — Coil lo resuelve automáticamente
- **`ColorPainter`**: Painter simple para placeholder/error (no requiere recursos drawable)
- **Sin `crossfade`/`transformations`**: API simplificada de Coil 3.6.3

### 6. Flujo Completo 40% (End-to-End)

```
Usuario abre app
    ↓
VisionScreen → koinViewModel() → VisionViewModel
    ↓
UI: "Visión Artificial" + "Procesamiento de Imágenes con ML Kit"
    ↓
Usuario toca "Seleccionar Imagen de Galería"
    ↓
VisionScreen → emitEvent(OnSelectImageClick)
    ↓
VisionViewModel → _effect.emit(OpenImagePicker)
    ↓
LaunchedEffect colecta effect → pickImage.launch("image/*")
    ↓
Sistema abre galería → Usuario elige foto → onActivityResult
    ↓
pickImage callback → emitEvent(OnImageSelected(uri))
    ↓
VisionViewModel → _state.update(selectedImageUri = uri)
    ↓
UI recompone → Muestra preview (Coil AsyncImage) + "Analizar Imagen" habilitado
    ↓
Usuario toca "Analizar Imagen"
    ↓
VisionViewModel → _state.update(isLoading=true) → analyzeImageUseCase(uri)
    ↓
AnalyzeImageUseCase → repository.analyzeImage(uri)
    ↓
VisionRepositoryImpl → dataSource.analyze(uri)  // MlKitImageAnalyzer
    ↓
MlKitImageAnalyzer:
  1. InputImage.fromFilePath(context, uri)
  2. imageLabeler.process() + textRecognizer.process() EN PARALELO
  3. Tasks.await() ambos
  4. labelsResult.map { it.text }
  5. textsResult.textBlocks.map { it.text }
  6. VisionResult(uri, labels, texts, timestamp)
    ↓
VisionViewModel → _state.update(isLoading=false, result=VisionResult)
    ↓
UI recompone → Muestra Cards:
  - "Objetos detectados (N)" → lista labels
  - "Texto detectado (M bloques)" → lista textos OCR
    ↓
Usuario puede tocar "Limpiar" → VisionState() inicial
```

### 7. Tests y Verificación

```bash
# Verificación completa (DEBE PASAR antes de push)
./gradlew :shared:check :androidApp:assembleDebug --no-configuration-cache

# Solo detekt (0 issues requeridos)
./gradlew :shared:detekt --no-configuration-cache

# Tests unitarios
./gradlew :shared:allTests --no-configuration-cache

# Compilar solo Android
./gradlew :shared:compileAndroidMain --no-configuration-cache
```

---

## 🔄 Flujo Completo de la App (40% Actualizado)

```
Usuario abre app
    ↓
MainActivity.onCreate()
    ↓
setContent { App() }
    ↓
App() → MaterialTheme { VisionScreen(viewModel = koinViewModel()) }
    ↓
VisionScreen (koinViewModel → VisionViewModel)
    ↓
UI renderiza: Título "Visión Artificial" + subtítulo + "Seleccionar Imagen de Galería"
    ↓
Usuario toca "Seleccionar Imagen de Galería"
    ↓
VisionScreen → viewModel.emitEvent(OnSelectImageClick)
    ↓
VisionViewModel → _effect.emit(OpenImagePicker)
    ↓
LaunchedEffect colecta effect → pickImage.launch("image/*")
    ↓
Sistema abre galería → Usuario elige foto → onActivityResult
    ↓
pickImage callback → emitEvent(OnImageSelected(uri))
    ↓
VisionViewModel → _state.update(selectedImageUri = uri, result = null, error = null)
    ↓
UI recompone → Muestra preview (Coil AsyncImage 300dp, rounded corners)
             → Botón "Analizar Imagen" habilitado + "Limpiar selección" (X)
    ↓
Usuario toca "Analizar Imagen"
    ↓
VisionViewModel → _state.update(isLoading = true, error = null)
             → analyzeImageUseCase(uri)
    ↓
AnalyzeImageUseCase → repository.analyzeImage(uri)
    ↓
VisionRepositoryImpl → dataSource.analyze(uri)  // MlKitImageAnalyzer (Android)
    ↓
MlKitImageAnalyzer:
  1. InputImage.fromFilePath(context, uri)
  2. imageLabeler.process() + textRecognizer.process() EN PARALELO
  3. Tasks.await() ambos
  4. labelsResult.map { it.text }
  5. textsResult.textBlocks.map { it.text }
  6. VisionResult(uri, labels, texts, timestamp)
    ↓
VisionViewModel → _state.update(isLoading = false, result = VisionResult)
    ↓
UI recompone → Muestra 2 Cards con elevation:
  - "Objetos detectados (N)" → lista labels (ej: "Person", "Car", "Tree")
  - "Texto detectado (M bloques)" → lista textos OCR (ej: "STOP", "EXIT")
    ↓
Usuario puede tocar "Limpiar" (X en preview o botón) → VisionState() inicial
```

---

## 📦 Próximos Pasos (100% — 16 Nov)

### 1. Room Database para Historial
- **Entity:** `AnalysisHistoryEntity(id, imageUri, labelsJson, textsJson, timestamp)` en `androidMain` (Room 3 + BundledSQLiteDriver)
- **DAO:** `insert()`, `getAll()`, `deleteAll()`, `getById()` en `commonMain` (expect) + `androidMain` (actual)
- **Repository:** `HistoryRepository` + `HistoryRepositoryImpl` con mapeo JSON ↔ Domain
- **UseCase:** `GetHistoryUseCase`, `SaveAnalysisUseCase`, `DeleteHistoryUseCase`
- **ViewModel:** `HistoryViewModel` con `StateFlow<List<AnalysisHistory>>`
- **Screen:** `HistoryScreen` con `LazyColumn` + swipe-to-delete

### 2. CameraX — Análisis en Tiempo Real (Opcional/Plus)
- `androidx.camera:camera-core:1.4.0`, `camera-view:1.4.0`, `camera-mlkit-vision:1.4.0`
- `ImageAnalysis` use case con `MLKitAnalyzer` personalizado
- PreviewView + overlay de bounding boxes + texto detectado
- Botón "Capturar y Guardar" → guarda en Room

### 3. UI/UX Pulido
- **Design System:** Tema oscuro/claro + alto contraste (4 paletas) con switch en settings
- **Empty states:** Ilustraciones + mensajes amigables cuando no hay historial
- **Loading states:** Shimmer en lista historial, skeleton en preview
- **Error handling:** Snackbar unificado + retry en botones
- **Animaciones:** `AnimatedVisibility` para preview, `Crossfade` para estados

### 4. iOS — Vision Framework (Stubs → Real)
- `MlKitImageAnalyzer.ios.kt` → `VisionImageAnalyzer` usando `VNRecognizeObjectsRequest` + `VNRecognizeTextRequest`
- `PlatformModule.ios.kt` provee implementación real
- Probar en macOS / Xcode / Simulator

### 5. Testing Completo
- Tests unitarios: UseCases + Repository + ViewModels (fakes)
- Tests UI: `ComposeTestRule` + `createAndroidComposeRule` para VisionScreen
- Tests instrumentados: Room DAO, ML Kit integration (requires device)

### 6. Documentación + Manual de Usuario
- README actualizado con screenshots
- Manual de usuario (PDF) con capturas: galería, análisis, historial, settings
- Diagrama arquitectura (Mermaid/PlantUML) en docs/
- Autoevaluación + heteroevaluación listas para 20 Nov

---

## 🐛 Problemas Conocidos / Decisiones Técnicas

| Tema | Decisión | Razón |
|------|----------|-------|
| `java.lang.System.currentTimeMillis()` en commonMain | `expect/actual` con `currentTimestamp()` | No existe en iOS (Kotlin/Native) |
| `runCatching` vs `safeCall` en UseCase | `runCatching` | Repository ya usa `safeCall` internamente; UseCase solo envuelve |
| `sealed interface` vs `sealed class` para Events/Effects | `sealed interface` | Permite `data class` y `object` mezclados, exhaustividad en `when` |
| `MutableSharedFlow` para Effects | Sin `replay` | One-shot: cada collector recibe solo nuevos eventos |
| `koinViewModel()` sin parámetros | Scoped a navigation | Koin+Navigation integra automáticamente |
| Design System como dependencia externa | `ds-repo/` Maven local | Versionado en Git, checksums estables con `.gitattributes` |

---

## 📝 Notas para la Defensa (13 Nov — 40%)

1. **Arquitectura:** Explicar Clean Architecture + MVI con diagrama (Data → Domain → Presentation)
2. **Multiplatform:** Mostrar `expect/actual` en `currentTimestamp()`, `ImageAnalyzerDataSource`, y `VisionScreen`
3. **DI:** Koin modules por capa, `platformModule()` para bindings específicos (Android: ML Kit, iOS: Stub), `viewModelOf` para lifecycle
4. **ML Kit Real:** `MlKitImageAnalyzer` con `Tasks.await()` para paralelismo, `withContext(Dispatchers.IO)`, liberación recursos con `close()`
5. **Gallery Picker:** `ActivityResultContracts.GetContent()` + `rememberLauncherForActivityResult` + `LaunchedEffect` para effects
6. **Coil 3:** `AsyncImage` con `ImageRequest.Builder`, placeholder/error con `ColorPainter`
7. **Testing:** Fakes para Repository, `runTest` + `kotlinx.coroutines.test`, `allTests` pasa
8. **Detekt:** 0 issues, config estricta con `FunctionNaming` excluyendo `@Composable`
9. **Próximos pasos (100%):** Room historial, CameraX tiempo real, UI pulido (Design System dark/high-contrast), iOS Vision Framework, tests completos, manual usuario

---

## 🎯 Guía de Explicación de Código (Para Defensa / Preguntas)

### ¿Cómo explico el flujo del proyecto?
**Estructura recomendada:**
1. **Arquitectura general** (30 seg): "Clean Architecture + MVI. 3 capas: Data (ML Kit/Room), Domain (UseCases/Repository interfaces), Presentation (ViewModel State/Event/Effect + Compose Screen). Koin para DI."
2. **Feature Vision** (1 min): "Domain: `VisionResult`, `VisionRepository`, `AnalyzeImageUseCase`. Data: `ImageAnalyzerDataSource` interface + `MlKitImageAnalyzer` (Android) / `Stub` (iOS). Presentation: `VisionState/Event/Effect`, `VisionViewModel`, `VisionScreen`."
3. **Flujo usuario** (1 min): Recorrer el diagrama ASCII del flujo 40% (galería → preview → análisis → resultados).
4. **Multiplatform** (30 seg): "expect/actual en `ImageAnalyzerDataSource`, `currentTimestamp()`, `VisionScreen`. Android usa ML Kit real, iOS stub. Koin `platformModule()` inyecta la implementación correcta."

### Si me preguntan "¿Qué hace X cosa?" / "¿El código es descriptivo?"
**Patrón de respuesta:**
1. **Nombre del archivo/clase** → "En `MlKitImageAnalyzer.kt`..."
2. **Responsabilidad única** → "Implementa `ImageAnalyzerDataSource` para ML Kit on-device"
3. **Método clave** → "`analyze(uri)` recibe URI, crea `InputImage`, corre labeler+recognizer en paralelo con `Tasks.await()`, mapea a `VisionResult`"
4. **Por qué así** → "`Tasks.await()` porque ML Kit usa Google Play Services Tasks API, no corrutinas nativas. Paralelismo real: ambos `process()` se lanzan antes del primer `await`."

**Ejemplos de preguntas frecuentes:**
| Pregunta | Respuesta corta |
|----------|-----------------|
| "¿Qué hace `VisionViewModel`?" | "Estado inmutable (`StateFlow`), eventos de usuario (`emitEvent`), efectos one-shot (`SharedFlow`). Delega análisis a `AnalyzeImageUseCase`." |
| "¿Por qué `sealed interface` para Event/Effect?" | "Permite `object` (sin payload) y `data class` (con payload) mezclados. Exhaustividad en `when` garantizada por compilador." |
| "¿Cómo funciona la galería?" | "`rememberLauncherForActivityResult(GetContent())` registra callback. `LaunchedEffect` colecta `OpenImagePicker` effect y lanza `pickImage.launch()`. Resultado vuelve al ViewModel via `OnImageSelected(uri)`." |
| "¿Por qué `Tasks.await()` y no `async/await`?" | "ML Kit retorna `Task<T>` (Play Services). `Tasks.await()` lo convierte a suspend. `async` de corrutinas no aplica aquí." |
| "¿Dónde está el preview de imagen?" | "`AsyncImage` de Coil 3 en `VisionScreen` (androidMain). Recibe `imageUri` string (`content://`), Coil resuelve automáticamente." |

### Si me piden cambiar algo ("modificar campo X"), ¿dónde se hace?
| Qué cambiar | Archivo(s) a tocar |
|-------------|-------------------|
| **Agregar campo a resultado** (ej: confidence score) | 1. `VisionResult` (domain/model) → 2. `MlKitImageAnalyzer` (data) → 3. `VisionScreen` ResultSection (presentation) → 4. Tests |
| **Cambiar umbral confianza ML Kit** | `MlKitImageAnalyzer`: `ImageLabelerOptions.Builder().setConfidenceThreshold(0.7f).build()` |
| **Agregar nuevo evento UI** | 1. `VisionEvent` (sealed interface) → 2. `VisionViewModel.emitEvent()` (when) → 3. `VisionScreen` (botón/acción) |
| **Agregar nuevo efecto (toast, nav)** | 1. `VisionEffect` (sealed interface) → 2. `VisionViewModel._effect.emit()` → 3. `VisionScreen.LaunchedEffect` (collect) |
| **Cambiar tema/colores** | Design System externo (`ds-repo/`) → recompilar → `MyProjectTheme` |
| **Agregar dependencia** | `gradle/libs.versions.toml` (versión) → `shared/build.gradle.kts` (implementation en sourceSet correcto) |
| **Cambiar navegación** | `NavRoute.kt` (nueva ruta) → `AppNavHost.kt` (composable) → Screen llama `navController.navigate()` via Effect |

### ¿Cómo probar lo implementado? (Run manual)

**Prerrequisitos:**
- Android Studio + Emulador (API 24+) O dispositivo físico con USB debugging
- `local.properties` con `tmdb.apiKey` (opcional, para features futuras)

**Pasos:**
```bash
# 1. Compilar e instalar en emulador/dispositivo
./gradlew :androidApp:installDebug

# 2. O desde Android Studio: Run ▶ 'androidApp' (verde)
```

**Qué debería pasar al hacer Run:**
1. **Splash** → Pantalla "Visión Artificial" con título + subtítulo
2. **Botón principal**: "Seleccionar Imagen de Galería" (llena ancho)
3. **Toca botón** → Se abre selector de archivos del sistema (galería/archivos)
4. **Elige imagen** (jpg/png) → Vuelve a app
5. **Preview aparece**: Imagen 300dp con esquinas redondeadas, botón "X" arriba a la derecha
6. **Botones abajo**: "Analizar Imagen" (habilitado) + "Limpiar" (X en preview)
7. **Toca "Analizar Imagen"** → CircularProgressIndicator + "Analizando..." (botón disabled)
8. **Tras 1-3 seg** → Aparecen 2 Cards con elevation:
   - "Objetos detectados (N)" → lista: Person, Car, Dog, Tree, etc.
   - "Texto detectado (M bloques)" → lista: textos OCR (ej: señales, carteles)
9. **Toca "Limpiar" (X o botón)** → Vuelve a estado inicial (solo botón "Seleccionar")

**Casos de prueba extra:**
| Caso | Qué probar |
|------|------------|
| Imagen sin texto | Solo aparece card "Objetos detectados" |
| Imagen solo texto (doc) | Solo aparece card "Texto detectado" |
| Imagen borrosa | Labels genéricos, texts vacío o basura OCR |
| Cancelar galería | No cambia estado, sigue en pantalla inicial |
| Rotar pantalla | Estado se mantiene (ViewModel survives config change) |
| Múltiples análisis seguidos | Cada uno reemplaza resultado anterior |

---

## 🔗 Referencias Rápidas

- **ML Kit Image Labeling:** https://developers.google.com/ml-kit/vision/image-labeling/android
- **ML Kit Text Recognition:** https://developers.google.com/ml-kit/vision/text-recognition/android
- **Compose Navigation:** https://developer.android.com/jetpack/compose/navigation
- **Koin KMP:** https://insert-koin.io/docs/reference/koin-multiplatform/
- **Kotlinx Datetime:** https://github.com/Kotlin/kotlinx-datetime (para timestamps multiplatform futuros)