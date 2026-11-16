package org.ucb.appp1.feature.vision.data.ai

import android.content.Context
import android.net.Uri
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.ucb.appp1.feature.vision.data.ImageDecoding
import org.ucb.appp1.feature.vision.domain.repository.ImageDescriptionRepository

/**
 * Describe una imagen con la API de Gemini (IA generativa, en la nube).
 *
 * La API key se lee de `local.properties` (`gemini.apiKey`) y NO se sube a Git.
 * Si falta la key o no hay internet, lanza un error con un mensaje claro para el usuario.
 */
class GeminiImageDescriptionRepository(
    private val context: Context,
    private val apiKey: String
) : ImageDescriptionRepository {

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
        parse(post(body.toString()))
    }

    private fun post(json: String): String {
        val connection = URL("$ENDPOINT/$MODEL:generateContent").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("x-goog-api-key", apiKey)
            connection.outputStream.use { it.write(json.toByteArray()) }

            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                error(errorMessage(code))
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            throw IllegalStateException("No se pudo conectar con Gemini. Revisa tu conexión a internet.", e)
        } finally {
            connection.disconnect()
        }
    }

    private fun errorMessage(code: Int): String = when (code) {
        HTTP_BAD_REQUEST, HTTP_FORBIDDEN -> "Gemini rechazó la solicitud (revisa que la API key sea válida)."
        HTTP_NOT_FOUND -> "El modelo de Gemini no está disponible: hay que actualizar la constante MODEL."
        HTTP_TOO_MANY ->"Se alcanzó el límite de uso de Gemini. Intenta en un minuto."
        else -> "Gemini respondió con un error ($code)."
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
        const val MODEL = "gemini-2.5-flash"
        const val TIMEOUT_MS = 30_000
        const val HTTP_BAD_REQUEST = 400
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
        const val HTTP_TOO_MANY = 429
        const val PROMPT =
            "Describe esta imagen en español en 2 o 3 oraciones. Menciona los objetos principales y, " +
                "si hay texto legible, indícalo."
    }
}
