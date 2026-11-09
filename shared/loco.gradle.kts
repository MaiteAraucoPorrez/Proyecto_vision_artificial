// =====================================================================
// Descarga los textos (strings.xml) desde Localise.biz (Loco) con Gradle.
//
// Uso:   ./gradlew :shared:downloadLocoStrings        (en Windows: .\gradlew.bat :shared:downloadLocoStrings)
//
// La API key NO va en el código. Se lee de:
//   1) local.properties  ->  loco.apiKey=TU_API_KEY      (este archivo NO se sube a Git)
//   2) variable de entorno LOCO_API_KEY
// =====================================================================
import java.net.HttpURLConnection
import java.net.URI
import java.util.Properties

// Idiomas que existen en el proyecto de Loco (el código debe ser igual al de Loco).
val locoLocales = listOf("en", "es", "fr", "ca")

// Idioma por defecto: va en la carpeta "values". Los demás van en "values-es", "values-fr", ...
val locoDefaultLocale = "en"

abstract class DownloadLocoStrings : DefaultTask() {

    @get:Input
    @get:Optional
    abstract val localProperties: Property<String>

    @get:Input
    @get:Optional
    abstract val envKey: Property<String>

    @get:Input
    abstract val locales: ListProperty<String>

    @get:Input
    abstract val defaultLocale: Property<String>

    @get:Internal
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun download() {
        val props = Properties()
        localProperties.orNull?.let { props.load(it.reader()) }
        val apiKey = props.getProperty("loco.apiKey")?.trim()?.takeIf { it.isNotEmpty() }
            ?: envKey.orNull?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw GradleException(
                "Falta la API key de Localise.biz. Agrega en local.properties:  loco.apiKey=TU_API_KEY"
            )

        locales.get().forEach { locale ->
            val url = URI("https://localise.biz/api/export/locale/$locale.xml").toURL()
            val connection = url.openConnection() as HttpURLConnection
            // Header recomendado por Loco (la key no queda en la URL ni en los logs).
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000

            val code = connection.responseCode
            if (code != 200) {
                throw GradleException(
                    "Loco respondió HTTP $code para el idioma '$locale'. " +
                        "Revisa la API key y que el idioma '$locale' exista en tu proyecto de Loco."
                )
            }

            val xml = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val folder = if (locale == defaultLocale.get()) "values" else "values-$locale"
            val dir = outputDir.get().asFile.resolve(folder).apply { mkdirs() }
            dir.resolve("strings.xml").writeText(xml, Charsets.UTF_8)
            logger.lifecycle("Loco: $locale -> $folder/strings.xml")
        }
    }
}

tasks.register<DownloadLocoStrings>("downloadLocoStrings") {
    group = "localization"
    description = "Descarga los strings.xml de cada idioma desde Localise.biz"

    localProperties.set(
        providers.fileContents(rootProject.layout.projectDirectory.file("local.properties")).asText
    )
    envKey.set(providers.environmentVariable("LOCO_API_KEY"))
    locales.set(locoLocales)
    defaultLocale.set(locoDefaultLocale)
    outputDir.set(layout.projectDirectory.dir("src/commonMain/composeResources"))

    // Hace llamadas de red: no tiene sentido cachearla ni marcarla como "up-to-date".
    doNotTrackState("Descarga textos desde Localise.biz")
}
