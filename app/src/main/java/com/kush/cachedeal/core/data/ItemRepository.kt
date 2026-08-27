package com.kush.cachedeal.core.data

import android.content.Context
import com.kush.cachedeal.core.database.CacheDealDatabase
import com.kush.cachedeal.core.model.Category
import com.kush.cachedeal.core.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID

class ItemRepository(private val context: Context) {
    private val itemDao = CacheDealDatabase.getDatabase(context).itemDao()

    private fun getSampleItems(): List<Item> {
        return Category.entries.map { category ->
            val (title, description) = when (category) {
                Category.EATABLES -> "Balaji Wafer Pack" to "Sample item"
                Category.WEARABLES -> "Campus Hoodie" to "Sample item"
                Category.CYCLES -> "Campus Bicycle" to "Sample item"
                Category.CALCULATORS -> "Scientific Calculator" to "Sample item"
                Category.LAB_COATS -> "White Lab Coat" to "Sample item"
                Category.SUBSCRIPTIONS -> "Streaming Subscription" to "Sample item"
                Category.STUDY_NOTES -> "Handwritten Notes" to "Sample item"
                Category.GAME_ACCOUNTS -> "Gaming Account" to "Sample item"
            }
            Item(
                id = "sample_${category.name.lowercase()}",
                sellerId = "sample",
                sellerName = "CacheDeal",
                sellerBlock = "",
                category = category.displayName,
                title = title,
                description = description,
                price = 0.0,
                photoUrl = "android.resource://com.kush.cachedeal/drawable/sample_${category.name.lowercase()}",
                status = "sample"
            )
        }
    }

    fun getAllItemsFlow(): Flow<List<Item>> {
        // Return Room flow combined with samples
        return itemDao.getAllItemsFlow().map { roomItems ->
            getSampleItems() + roomItems
        }
    }

    suspend fun getAllItems(): List<Item> {
        return getSampleItems() + itemDao.getAllItems()
    }

    suspend fun getMyItems(userId: String): List<Item> {
        return itemDao.getItemsBySeller(userId)
    }

    suspend fun getItemById(itemId: String): Item? {
        val sample = getSampleItems().find { it.id == itemId }
        if (sample != null) return sample
        return itemDao.getItem(itemId)
    }

    suspend fun postItem(item: Item): Result<Unit> {
        return try {
            val newItem = if (item.id.isEmpty()) {
                item.copy(id = UUID.randomUUID().toString())
            } else {
                item
            }
            itemDao.insertItem(newItem)
            // TODO: In Phase 2, also insert into Supabase Postgrest
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateItemStatus(itemId: String, newStatus: String): Result<Unit> {
        return try {
            itemDao.updateStatus(itemId, newStatus)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

