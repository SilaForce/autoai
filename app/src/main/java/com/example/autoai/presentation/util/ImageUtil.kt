package com.example.autoai.presentation.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import coil.size.Dimension
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale
import kotlin.math.roundToInt

object ImageUtils {

    /**
     * Smanjuje rezoluciju slike, kompresuje je u JPEG i pretvara u Base64 String
     * spreman za snimanje u Firestore.
     */
    fun compressAndEncodeToBase64(imageBytes: ByteArray): String? {
        return try {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            // Smanjujemo sliku na 400x400 (sasvim dovoljno za profilnu)
            val ratio = 400.0f / maxOf(bitmap.width, bitmap.height)
            val width = Math.round(bitmap.width * ratio)
            val height = Math.round(bitmap.height * ratio)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

            val outputStream = ByteArrayOutputStream()
            // Kompresija na 70% kvaliteta smanjuje veličinu fajla drastično, bez vidljivog gubitka
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)

            val compressedBytes = outputStream.toByteArray()
            Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    fun compressForAi(imageBytes: ByteArray, maxDimension: Int = 1024, quality: Int = 80): ByteArray? {
        return try {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.size)
            val ratio = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
            if (ratio >= 1f) return imageBytes

            val width = (bitmap.width * ratio).roundToInt()
            val height = (bitmap.height * ratio).roundToInt()
            val scaledBitmap = bitmap.scale(width, height)

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG,quality,outputStream)
            outputStream.toByteArray()
        } catch (e: Exception){
          null
        }
    }

    /**
     * Pretvara Base64 String iz baze nazad u ByteArray kako bi ga Coil mogao prikazati.
     */
    fun decodeBase64ToByteArray(base64String: String): ByteArray? {
        return try {
            Base64.decode(base64String, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }
}