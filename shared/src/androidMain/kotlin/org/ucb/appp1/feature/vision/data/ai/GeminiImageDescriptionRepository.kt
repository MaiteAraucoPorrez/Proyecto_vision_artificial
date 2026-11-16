package org.ucb.appp1.feature.vision.data.ai

import android.content.Context
import android.net.Uri
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.ucb.appp1.core.util.safeCall
import org.ucb.appp1.feature.vision.data.ImageDecoding
import org.ucb.appp1.feature.vision.domain.repository.ImageDescriptionRepository

/**
 * Describe una imagen con la API de Gemini (IA generativa, en la nube).
 *
 * La API key se lee de `local.properties` (`gemini.apiKey`) y NO se sube a Git.
 * Los nombres de modelo de Gemini cambian con el tiempo, así que en vez de fijar uno la app
 * **consulta a la API qué modelos tiene disponibles tu clave** y usa el Flash más reciente
 * que acepte imágenes (generateContent).
 */
class GeminiImageDescriptionRepository(
    private val context: Context,
    private val apiKey: String
) : ImageDescriptionRepository {

    @Volatile
    private var cachedModels: List<String>? = null

    override suspend fun describe(imageUri: String): String = withContext(Dispatchers.IO) {
        check(apiKey.isNotBlank()) {
            "Falta la API key de Gemini. Agrega gemini.apiKey en local.properties y vuelve a compilar."
        }
        val base64 = ImageDecoding.toBase64Jpeg(context, Uri.parse(imageUri))
            ?: error("No se pudo leer la imagen.")

        val body = JSONObject().put(
            "contents",
            JSONArray().put(
                JSONObject().put(
                    "parts",
                    JSONArray()
                        .put(JSONObject().put("text", PROMPT))
                        .put(
                            JSONObject().put(
                                "inline_data",
                                JSONObject().put("mime_type", "image/jpeg").put("data", base64)
                            )
                        )
                )
            )
        )
        callWithFallback(availableModels(), body.toString())
    }

    /** Modelos de la clave; si la consulta falla por saturación se usa una lista conocida (sin cachearla). */
    private fun availableModels(): List<String> {
        cachedModels?.let { return it }
        return try {
            discoverModels().also { cachedModels = it }
        } catch (e: GeminiHttpException) {
            if (e.code !in RETRYABLE) throw e
            FALLBACK_MODELS
        }
    }

    /** Pide a la API la lista de modelos de la clave y elige el Flash más reciente con generateContent. */
    private fun discoverModels(): List<String> {
        val models = JSONObject(request("$ENDPOINT?pageSize=$PAGE_SIZE", null)).optJSONArray("models")
        val flash = (0 until (models?.length() ?: 0)).mapNotNull { models?.optJSONObject(it) }
            .filter { m ->
                val methods = m.optJSONArray("supportedGenerationMethods")
                (0 until (methods?.length() ?: 0)).any { methods?.optString(it) == "generateContent" }
            }
            .map { it.optString("name").removePrefix("models/") }
            .filter { name -> name.contains("flash") && EXCLUDED.none { name.contains(it) } }
        val main = flash.filterNot { it.contains("lite") }.sortedDescending()
        val lite = flash.filter { it.contains("lite") }.sortedDescending()

        // Orden de prueba: primero los "lite" (rápidos y menos saturados), luego los demás Flash.
        val ordered = lite.filter { it.endsWith("latest") } + lite.filterNot { it.endsWith("latest") } +
            main.filter { it.endsWith("latest") } + main.filterNot { it.endsWith("latest") }
        check(ordered.isNotEmpty()) {
            "Tu API key no tiene modelos Gemini Flash disponibles. Revisa la clave en Google AI Studio."
        }
        return ordered
    }

    /** Si un modelo está saturado (503/429) espera un momento y prueba con el siguiente. */
    private suspend fun callWithFallback(models: List<String>, body: String): String {
        var failure: GeminiHttpException? = null
        // Recorre los modelos en ciclo con espera creciente: la saturación suele durar solo unos segundos.
        for (attempt in 0 until MAX_ATTEMPTS) {
            val model = models[attempt % models.size]
            try {
                return parse(request("$ENDPOINT/$model:generateContent", body))
            } catch (e: GeminiHttpException) {
                if (e.code !in RETRYABLE) throw e
                failure = e
                delay(RETRY_DELAY_MS * (attempt + 1))
            }
        }
        throw IllegalStateException(
            "Gemini sigue saturado por alto tráfico (error de Google, no de la app). " +
                "Espera unos minutos e inténtalo de nuevo.",
            failure
        )
    }

    private class GeminiHttpException(val code: Int, message: String) : IllegalStateException(message)

    private fun request(url: String, json: String?): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = if (json == null) "GET" else "POST"
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("x-goog-api-key", apiKey)
            if (json != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { it.write(json.toByteArray()) }
            }
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                val detail = safeCall { connection.errorStream?.bufferedReader()?.use { it.readText() } }
                    .getOrNull().orEmpty()
                throw GeminiHttpException(code, errorMessage(code, detail))
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            throw IllegalStateException(
                "No se pudo conectar con Gemini (${e.javaClass.simpleName}). Revisa tu conexión a internet.",
                e
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun errorMessage(code: Int, detail: String): String {
        val serverMessage = safeCall { JSONObject(detail).optJSONObject("error")?.optString("message") }
            .getOrNull().orEmpty().take(MAX_DETAIL)
        val base = when (code) {
            HTTP_BAD_REQUEST, HTTP_FORBIDDEN -> "Gemini rechazó la solicitud (revisa que la API key sea válida)."
            HTTP_NOT_FOUND -> "Gemini no encontró el recurso solicitado."
            HTTP_TOO_MANY -> "Se alcanzó el límite de uso de Gemini. Intenta en un minuto."
            else -> "Gemini respondió con un error ($code)."
        }
        return if (serverMessage.isEmpty()) base else "$base\n$serverMessage"
    }

    private fun parse(response: String): String {
        val parts = JSONObject(response)
            .optJSONArray("candidates")?.optJSONObject(0)
            ?.optJSONObject("content")?.optJSONArray("parts")
        val count = parts?.length() ?: 0
        val text = (0 until count).joinToString("") { parts?.optJSONObject(it)?.optString("text").orEmpty() }
        return text.trim().ifEmpty { error("Gemini no devolvió ninguna descripción.") }
    }

    private companion object {
        const val ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models"
        const val PAGE_SIZE = 200
        const val TIMEOUT_MS = 20_000
        const val MAX_DETAIL = 200
        const val HTTP_BAD_REQUEST = 400
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
        const val HTTP_TOO_MANY = 429
        const val HTTP_UNAVAILABLE = 503
        const val MAX_ATTEMPTS = 7
        const val RETRY_DELAY_MS = 2_000L
        val FALLBACK_MODELS = listOf("gemini-flash-lite-latest", "gemini-flash-latest")
        val RETRYABLE = setOf(HTTP_TOO_MANY, HTTP_UNAVAILABLE, HTTP_NOT_FOUND)
        val EXCLUDED = listOf("image", "tts", "live", "audio", "thinking", "exp", "robotics", "computer")
        const val PROMPT =
            "Describe esta imagen en español en 2 o 3 oraciones. Menciona los objetos principales y, " +
                "si hay texto legible, indícalo."
    }
}
