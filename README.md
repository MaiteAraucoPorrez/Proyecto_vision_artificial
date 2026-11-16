# appp1 — Visión Artificial y Procesamiento de Imágenes

Proyecto del **4to Elemento** — Programación de Dispositivos Móviles
**Autora:** Gabriela Maite Arauco Porrez
**Repo:** MaiteAraucoPorrez/Proyecto_vision_artificial
**Estado:** 100% (16 Nov 2026)

---

## Descripción

App Android (Kotlin Multiplatform + Compose Multiplatform) que analiza imágenes **en el dispositivo, sin internet**, con modelos de *machine learning* pre-entrenados de **ML Kit** (Google):

| Función | Qué hace | Modelo |
|---|---|---|
| Imagen de galería | Objetos + texto (OCR por líneas) + **aviso de calidad** (borrosa/oscura) + descripción opcional con IA (Gemini) | Image Labeling + Text Recognition + análisis de nitidez |
| Cámara en tiempo real | Recuadros sobre objetos y texto mientras apuntas | Object Detection + Labeling + OCR |
| Escáner QR y códigos | Lee QR y códigos de barras en tiempo real **o desde una foto de la galería** | Barcode Scanning |
| Historial | Guarda análisis y códigos (SQLite, persiste al cerrar la app) | — |
| Ajustes | Tema claro/oscuro/sistema y alto contraste | Design System |

**Arquitectura:** Clean Architecture + MVI (State / Event / Effect) · **DI:** Koin · **UI:** Compose Multiplatform + Design System propio (`ds-repo/`) · **Imágenes:** Coil 3 · **Cámara:** CameraX

---

## Comandos

```bash
./gradlew :shared:detekt                 # análisis estático (0 issues)
./gradlew :shared:allTests               # tests unitarios + de UI
./gradlew :androidApp:assembleDebug      # compilar el APK
./gradlew :androidApp:installDebug       # instalar en el dispositivo/emulador conectado
./gradlew :shared:check :androidApp:assembleDebug   # todo junto, antes de hacer push
```

---

## Estructura

```
shared/src/
├── commonMain/org/ucb/appp1/
│   ├── App.kt                  tema del Design System + navegación
│   ├── core/                   ui (componentes), util (OcrTextCleaner, SafeCall, tiempo)
│   ├── di/                     módulos Koin
│   ├── navigation/             NavRoute (tipadas) + AppNavHost
│   └── feature/
│       ├── home/               pantalla de inicio
│       ├── vision/             galería (domain / data / presentation)
│       ├── camera/             cámara en tiempo real
│       ├── scanner/            escáner QR y códigos
│       ├── history/            historial (domain / data / presentation)
│       └── settings/           tema y alto contraste
├── androidMain/                ML Kit, CameraX, SQLite, pantallas nativas
├── iosMain/                    Apple Vision (sin verificar), pantallas informativas
├── commonTest/                 tests unitarios
└── androidHostTest/            tests de UI (Compose + Robolectric)
androidApp/                     MainActivity, MainApplication, manifest (permiso CAMERA)
ds-repo/                        Design System publicado como Maven local
```

Más detalle (decisiones técnicas, flujos, casos de prueba): `documentacion.md` (archivo privado).

---

## Cómo probar la app

1. Conecta el celular (depuración USB) y ejecuta `./gradlew :androidApp:installDebug`.
2. **Galería:** Inicio → *Imagen de galería* → elige una foto → *Analizar imagen*.
3. **Cámara:** Inicio → *Cámara en tiempo real* → acepta el permiso → apunta a objetos o texto.
4. **Escáner:** Inicio → *Escáner QR y códigos* → apunta a un QR; puedes abrir el enlace o copiarlo.
5. **Historial:** desliza un elemento para borrarlo; "Borrar todo" limpia la lista.
6. **Ajustes:** cambia el tema y activa el alto contraste.

---

## Limitaciones conocidas

- iOS: el código de Apple Vision está escrito pero no se pudo compilar en Windows; iOS no tiene galería, cámara ni escáner (muestra pantallas informativas) y su historial es solo en memoria.
- Los modelos on-device tienen límites: fotos borrosas, oscuras o con ángulos raros dan menos objetos o ninguno; las etiquetas son genéricas ("Planta", "Vehículo"), no específicas.
- La preferencia de tema no se guarda al cerrar la app.

---

## Desarrollo en Windows

iOS no compila en Windows (requiere macOS/Xcode). Los targets iOS existen en `shared/build.gradle.kts` pero solo compilan en macOS.

---

## Entregas

| Fecha | Entrega | Estado |
|---|---|---|
| 9 Nov | 20% — arquitectura base | ✅ |
| 13 Nov | 40% — galería + ML Kit | ✅ |
| 16 Nov | 100% — cámara, escáner, historial, tema, tests | ✅ |
| 20 Nov | Exposición oral, autoevaluación y heteroevaluación | pendiente |

---

## Recursos

- [ML Kit Image Labeling](https://developers.google.com/ml-kit/vision/image-labeling)
- [ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition)
- [ML Kit Object Detection](https://developers.google.com/ml-kit/vision/object-detection)
- [ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning)
- [CameraX](https://developer.android.com/media/camera/camerax)
- [Koin Multiplatform](https://insert-koin.io/docs/reference/koin-multiplatform/)

---

## Descripción con IA generativa (opcional, Gemini)

En *Imagen de galería* hay un botón **"Describir con IA (Gemini)"** que envía la foto a la API de Gemini y muestra una descripción en español. Es opcional: el resto de la app funciona sin internet.

Para activarlo, agrega tu API key (gratis en Google AI Studio) en `local.properties` (archivo que **no** se sube a Git) y vuelve a compilar:

```properties
gemini.apiKey=TU_API_KEY
```

Sin la key, el botón muestra un mensaje explicando qué falta. La key queda dentro del APK, así que esto es válido para una demo, no para publicar la app.

## Permiso de cámara

Se pide **una vez por sesión**, cuando entras a Cámara o Escáner (no al abrir la app). Si lo deniegas, no se vuelve a abrir el diálogo solo: verás un botón *Conceder permiso* o, si marcaste "no volver a preguntar", *Abrir ajustes*. Escanear un QR desde la galería no necesita el permiso.
