package com.kush.swych.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    private const val MAX_SIDE = 1280
    private const val JPEG_QUALITY = 80
    private const val PHOTO_DIR = "photos"

    /** Empty file plus a content:// uri the camera app can write the full-size photo into. */
    fun newCameraTarget(context: Context): Pair<File, Uri> {
        val dir = File(context.cacheDir, PHOTO_DIR).apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return file to uri
    }

    /**
     * Downscales and re-encodes a camera or gallery image into a small JPEG in the cache,
     * rotated upright, ready to upload. Returns null if the image can't be read.
     */
    suspend fun prepareForUpload(context: Context, source: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

            var sample = 1
            while (bounds.outWidth / (sample * 2) >= MAX_SIDE || bounds.outHeight / (sample * 2) >= MAX_SIDE) sample *= 2
            val decoded = resolver.openInputStream(source)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return@withContext null

            val rotation = resolver.openInputStream(source)?.use {
                when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f

            val scale = minOf(1f, MAX_SIDE.toFloat() / maxOf(decoded.width, decoded.height))
            val matrix = Matrix().apply {
                postScale(scale, scale)
                postRotate(rotation)
            }
            val output = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)

            val dir = File(context.cacheDir, PHOTO_DIR).apply { mkdirs() }
            val file = File(dir, "item_photo_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { output.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
            if (output !== decoded) output.recycle()
            decoded.recycle()
            file
        } catch (_: Exception) {
            null
        }
    }

    /** Removes photos left in the cache by earlier captures and uploads. */
    fun clearCachedPhotos(context: Context) {
        File(context.cacheDir, PHOTO_DIR).listFiles()?.forEach { it.delete() }
    }
}
