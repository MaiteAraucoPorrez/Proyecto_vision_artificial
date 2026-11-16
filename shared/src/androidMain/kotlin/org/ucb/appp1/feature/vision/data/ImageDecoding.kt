package org.ucb.appp1.feature.vision.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.max
import org.ucb.appp1.feature.vision.domain.quality.ImageQuality
import org.ucb.appp1.feature.vision.domain.quality.ImageQualityEvaluator

/** Utilidades para leer imágenes de una URI reduciéndolas (evita cargar fotos de 12 MP completas). */
object ImageDecoding {

    private const val QUALITY_SIDE = 512
    private const val AI_SIDE = 1024
    private const val JPEG_QUALITY = 85
    private const val RED = 299
    private const val GREEN = 587
    private const val BLUE = 114
    private const val LUMA_DIVISOR = 1000

    fun decodeScaled(context: Context, uri: Uri, maxSide: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val longest = max(bounds.outWidth, bounds.outHeight)
        if (longest <= 0) return null

        var sample = 1
        while (longest / (sample * 2) >= maxSide) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }

    /** Calcula las métricas de calidad (nitidez, brillo, contraste) de la imagen. */
    fun evaluateQuality(context: Context, uri: Uri): ImageQuality? {
        val bitmap = decodeScaled(context, uri, QUALITY_SIDE) ?: return null
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        bitmap.recycle()
        val gray = IntArray(pixels.size) { i ->
            val p = pixels[i]
            (((p shr 16) and 0xFF) * RED + ((p shr 8) and 0xFF) * GREEN + (p and 0xFF) * BLUE) / LUMA_DIVISOR
        }
        return ImageQualityEvaluator.evaluate(gray, w, h)
    }

    /** JPEG en Base64 (máx. 1024 px) listo para enviar a la API de Gemini. */
    fun toBase64Jpeg(context: Context, uri: Uri): String? {
        val bitmap = decodeScaled(context, uri, AI_SIDE) ?: return null
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        bitmap.recycle()
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }
}
