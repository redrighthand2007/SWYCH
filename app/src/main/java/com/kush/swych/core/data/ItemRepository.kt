package com.kush.swych.core.data

import android.content.Context
import androidx.core.net.toUri
import com.kush.swych.core.model.Item
import com.kush.swych.core.network.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import java.util.UUID

class ItemRepository(@Suppress("unused") private val context: Context) {

    companion object {
        @Volatile
        var cachedItems: List<Item>? = null
    }

    private val db get() = SupabaseManager.client.postgrest

    suspend fun postItem(
        title: String,
        description: String,
        price: Double,
        category: String,
        photoUri: String
    ): Result<Unit> = runCatching {
        val sellerId = SupabaseManager.currentUid ?: throw Exception("Not logged in")

        // Upload to Supabase Storage
        var photoUrl = photoUri
        if (photoUri.startsWith("file://")) {
            val path = photoUri.toUri().path
            if (path != null) {
                val file = java.io.File(path)
                val bucket = SupabaseManager.client.storage["items"]
                bucket.upload(file.name, file.readBytes()) { upsert = true }
                photoUrl = bucket.publicUrl(file.name)
            }
        }

        val item = Item(
            id = "item_" + UUID.randomUUID().toString().substring(0, 8),
            sellerId = sellerId,
            title = title,
            description = description,
            price = price,
            category = category,
            photoUrl = photoUrl
        )
        db["items"].insert(item)
        cachedItems = null
    }

    /** Newest first. */
    suspend fun getAllItems(forceRefresh: Boolean = false): Result<List<Item>> {
        val cached = cachedItems
        if (!forceRefresh && cached != null) return Result.success(cached)
        return runCatching {
            db["items"].select {
                order("created_at", Order.DESCENDING)
            }.decodeList<Item>().also { cachedItems = it }
        }
    }

    /** Always fetched fresh, so the status shown before making an offer is current. */
    suspend fun getItemById(itemId: String): Result<Item> = runCatching {
        db["items"].select { filter { eq("id", itemId) } }.decodeSingle<Item>()
    }

    suspend fun getMyItems(): Result<List<Item>> = runCatching {
        val sellerId = SupabaseManager.currentUid ?: throw Exception("Not logged in")
        db["items"].select {
            filter { eq("seller_id", sellerId) }
            order("created_at", Order.DESCENDING)
        }.decodeList<Item>()
    }

    /** Edits an open listing. Fails once an offer has been accepted. */
    suspend fun updateItem(itemId: String, title: String, description: String, price: Double): Result<Unit> = runCatching {
        val updated = db["items"].update({
            set("title", title)
            set("description", description)
            set("price", price)
        }) {
            select()
            filter { eq("id", itemId) }
        }.decodeList<Item>()
        if (updated.isEmpty()) throw Exception("This listing can't be edited while a deal is in progress.")
        cachedItems = null
    }

    /** Removes an open listing; its pending offers are removed with it. */
    suspend fun deleteItem(itemId: String): Result<Unit> = runCatching {
        val deleted = db["items"].delete {
            select()
            filter { eq("id", itemId) }
        }.decodeList<Item>()
        if (deleted.isEmpty()) throw Exception("This listing can't be removed while a deal is in progress.")
        cachedItems = null
    }
}
