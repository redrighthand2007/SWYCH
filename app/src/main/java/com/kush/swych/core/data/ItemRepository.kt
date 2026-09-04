package com.kush.swych.core.data

import android.content.Context
import androidx.core.net.toUri
import com.kush.swych.core.model.Item
import com.kush.swych.core.network.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import java.util.UUID

class ItemRepository(private val context: Context) {

    companion object {
        var cachedItems: List<Item>? = null
    }

    suspend fun postItem(
        title: String,
        description: String,
        price: Double,
        category: String,
        photoUri: String
    ): Result<Unit> {
        return try {
            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val sellerId = prefs.getString("current_uid", null) ?: return Result.failure(Exception("Not logged in"))
            
            // Upload to Supabase Storage
            var photoUrl = photoUri
            if (photoUri.startsWith("file://")) {
                val path = photoUri.toUri().path
                if (path != null) {
                    val file = java.io.File(path)
                    val fileName = file.name
                    val bucket = SupabaseManager.client.storage["items"]
                    bucket.upload(fileName, file.readBytes()) { upsert = true }
                    photoUrl = bucket.publicUrl(fileName)
                }
            }

            val item = Item(
                id = "item_" + java.util.UUID.randomUUID().toString().substring(0, 8),
                sellerId = sellerId,
                title = title,
                description = description,
                price = price,
                category = category,
                photoUrl = photoUrl
            )
            
            SupabaseManager.client.postgrest["items"].insert(item)
            cachedItems = null // Invalidate cache
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllItems(forceRefresh: Boolean = false): Result<List<Item>> {
        if (!forceRefresh && cachedItems != null) {
            return Result.success(cachedItems!!)
        }
        return try {
            val items = SupabaseManager.client.postgrest["items"]
                .select()
                .decodeList<Item>()
            cachedItems = items
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getItemById(itemId: String): Result<Item> {
        return try {
            val cached = cachedItems?.find { it.id == itemId }
            if (cached != null) return Result.success(cached)

            val item = SupabaseManager.client.postgrest["items"]
                .select { filter { eq("id", itemId) } }
                .decodeSingle<Item>()
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateItemStatus(itemId: String, status: String): Result<Unit> {
        return try {
            SupabaseManager.client.postgrest["items"]
                .update({ set("status", status) }) { filter { eq("id", itemId) } }
            cachedItems = null // Invalidate cache
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyItems(): Result<List<Item>> {
        return try {
            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val sellerId = prefs.getString("current_uid", null) ?: return Result.failure(Exception("Not logged in"))
            
            val items = SupabaseManager.client.postgrest["items"]
                .select {
                    filter { eq("seller_id", sellerId) }
                }
                .decodeList<Item>()
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun deleteItem(itemId: String): Result<Unit> {
        return try {
            SupabaseManager.client.postgrest["items"]
                .delete { filter { eq("id", itemId) } }
            cachedItems = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}







