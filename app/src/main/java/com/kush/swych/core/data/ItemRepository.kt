package com.kush.swych.core.data

import androidx.core.net.toUri
import com.kush.swych.core.model.Item
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseItemRepository @Inject constructor(
    private val client: SupabaseClient
) : ItemRepository {

    private val db get() = client.postgrest
    private val currentUid: String? get() = client.auth.currentUserOrNull()?.id

    @Volatile
    private var cachedItems: List<Item>? = null

    override fun clearCache() {
        cachedItems = null
    }

    override suspend fun postItem(
        title: String,
        description: String,
        price: Double,
        category: String,
        photoUri: String
    ): Result<Unit> = runCatching {
        val sellerId = currentUid ?: throw Exception("Not logged in")

        // Upload to Supabase Storage
        val bucket = client.storage["items"]
        var photoUrl = photoUri
        var uploadedName: String? = null
        if (photoUri.startsWith("file://")) {
            val path = photoUri.toUri().path
            if (path != null) {
                val file = File(path)
                val name = "${sellerId}_${file.name}"
                bucket.upload(name, file.readBytes()) { upsert = true }
                uploadedName = name
                photoUrl = bucket.publicUrl(name)
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
        try {
            db["items"].insert(item)
        } catch (e: Exception) {
            // Don't leave an orphaned photo in storage when the listing itself fails
            if (uploadedName != null) runCatching { bucket.delete(uploadedName) }
            throw e
        }
        cachedItems = null
    }

    override suspend fun getAllItems(forceRefresh: Boolean): Result<List<Item>> {
        val cached = cachedItems
        if (!forceRefresh && cached != null) return Result.success(cached)
        return runCatching {
            db["items"].select {
                order("created_at", Order.DESCENDING)
            }.decodeList<Item>().also { cachedItems = it }
        }
    }

    /** Always fetched fresh, so the status shown before making an offer is current. */
    override suspend fun getItemById(itemId: String): Result<Item> = runCatching {
        db["items"].select { filter { eq("id", itemId) } }.decodeSingle<Item>()
    }

    override suspend fun getMyItems(): Result<List<Item>> = runCatching {
        val sellerId = currentUid ?: throw Exception("Not logged in")
        db["items"].select {
            filter { eq("seller_id", sellerId) }
            order("created_at", Order.DESCENDING)
        }.decodeList<Item>()
    }

    /** Edits an open listing. Fails once an offer has been accepted. */
    override suspend fun updateItem(itemId: String, title: String, description: String, price: Double): Result<Unit> = runCatching {
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
    override suspend fun deleteItem(itemId: String): Result<Unit> = runCatching {
        val deleted = db["items"].delete {
            select()
            filter { eq("id", itemId) }
        }.decodeList<Item>()
        if (deleted.isEmpty()) throw Exception("This listing can't be removed while a deal is in progress.")
        cachedItems = null
    }
}
