package com.example.autoai.presentation.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Base64
import coil.size.Dimension
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale
import kotlin.math.roundToInt

object ImageUtils {

    /**
     * Downscales, JPEG-compresses, and Base64-encodes an image for Firestore storage.
     * 400px max dimension is more than enough for an in-app avatar.
     */
    fun compressAndEncodeToBase64(imageBytes: ByteArray): String? {
        return try {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            val ratio = 400.0f / maxOf(bitmap.width, bitmap.height)
            val width = Math.round(bitmap.width * ratio)
            val height = Math.round(bitmap.height * ratio)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

            val outputStream = ByteArrayOutputStream()
            // 70% JPEG quality cuts file size drastically with no visible loss.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)

            val compressedBytes = outputStream.toByteArray()
            Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    fun compressForAi(imageBytes: ByteArray, maxDimension: Int = 1024, quality: Int = 80): ByteArray? {
        return try {
            val rotation = rotationDegreesFromExif(imageBytes)
            val source = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            val ratio = maxDimension.toFloat() / maxOf(source.width, source.height)
            val needsScale = ratio < 1f

            if (!needsScale && rotation == 0) return imageBytes

            val scaled = if (needsScale) {
                source.scale((source.width * ratio).roundToInt(), (source.height * ratio).roundToInt())
            } else source
            val oriented = if (rotation != 0) scaled.rotated(rotation) else scaled

            val outputStream = ByteArrayOutputStream()
            oriented.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    private fun rotationDegreesFromExif(imageBytes: ByteArray): Int = try {
        val orientation = ByteArrayInputStream(imageBytes).use { stream ->
            ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        }
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    } catch (e: Exception) {
        0
    }

    private fun Bitmap.rotated(degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    /**
     * Decodes a Firestore-stored Base64 string back into bytes so Coil can render it.
     */
    fun decodeBase64ToByteArray(base64String: String): ByteArray? {
        return try {
            Base64.decode(base64String, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }
}