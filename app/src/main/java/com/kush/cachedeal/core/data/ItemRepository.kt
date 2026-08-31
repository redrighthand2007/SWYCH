package com.kush.cachedeal.core.data

import android.content.Context
import com.kush.cachedeal.core.model.Item
import com.kush.cachedeal.core.network.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class ItemRepository(private val context: Context) {

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
            
            // Upload to Cloudinary (will implement later if needed, passing blank for now or just using uri string)
            val photoUrl = photoUri // com.kush.cachedeal.core.network.CloudinaryManager.uploadImage(context, photoUri)

            val item = Item(
                id = "item_${UUID.randomUUID().toString().take(8)}",
                sellerId = sellerId,
                title = title,
                description = description,
                price = price,
                category = category,
                photoUrl = photoUrl
            )
            
            SupabaseManager.client.postgrest["items"].insert(item)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllItems(): Result<List<Item>> {
        return try {
            val items = SupabaseManager.client.postgrest["items"]
                .select()
                .decodeList<Item>()
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getItemById(itemId: String): Result<Item> {
        return try {
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
}

