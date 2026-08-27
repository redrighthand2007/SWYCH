package com.kush.cachedeal.core.network

import android.content.Context
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CloudinaryManager {
    private const val CLOUDINARY_URL = "cloudinary://212533658242963:n4E4lyTyHHT5COaZQwTdpd0qyVE@cachedeal"

    private val cloudinary by lazy {
        Cloudinary(CLOUDINARY_URL)
    }

    suspend fun uploadImage(context: Context, imageUri: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // In a real app, you would pass an InputStream or File. 
                // Using URL/String path here for demonstration.
                val uploadResult = cloudinary.uploader().upload(imageUri, ObjectUtils.emptyMap())
                val url = uploadResult["secure_url"] as String
                Result.success(url)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
