package com.kush.swych.core.data

import android.content.Context
import com.kush.swych.core.model.Deal
import com.kush.swych.core.network.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest
import java.util.UUID

class DealRepository(private val context: Context) {

    suspend fun createDeal(
        itemId: String,
        itemTitle: String,
        itemPhotoUrl: String,
        sellerId: String,
        agreedPrice: Double
    ): Result<Unit> {
        return try {
            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val buyerId = prefs.getString("current_uid", null) ?: return Result.failure(Exception("Not logged in"))
            
            val deal = Deal(
                id = "deal_${UUID.randomUUID().toString().take(8)}",
                itemId = itemId,
                buyerId = buyerId,
                sellerId = sellerId,
                itemTitle = itemTitle,
                itemPhotoUrl = itemPhotoUrl,
                finalPrice = agreedPrice
            )
            
            SupabaseManager.client.postgrest["deals"].insert(deal)
            
            // Also update item status
            SupabaseManager.client.postgrest["items"].update({
                set("status", "PENDING")
            }) {
                filter { eq("id", itemId) }
            }
            
            // Invalidate BOTH caches so all screens see fresh data
            ItemRepository.cachedItems = null
            cachedDeals = null
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDealStatus(dealId: String, itemId: String, newStatus: String): Result<Unit> {
        return try {
            SupabaseManager.client.postgrest["deals"].update({
                set("status", newStatus)
            }) {
                filter { eq("id", dealId) }
            }
            
            val itemStatus = if (newStatus == "REJECTED") "OPEN" else "SOLD"
            SupabaseManager.client.postgrest["items"].update({
                set("status", itemStatus)
            }) {
                filter { eq("id", itemId) }
            }
            
            // Invalidate BOTH caches
            ItemRepository.cachedItems = null
            cachedDeals = null
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a deal AND reset the item status back to OPEN.
     * Used when a buyer cancels their deal request.
     */
    suspend fun deleteDeal(dealId: String, itemId: String): Result<Unit> {
        return try {
            SupabaseManager.client.postgrest["deals"].delete {
                filter { eq("id", dealId) }
            }
            
            // Reset item back to OPEN so others can deal on it
            SupabaseManager.client.postgrest["items"].update({
                set("status", "OPEN")
            }) {
                filter { eq("id", itemId) }
            }
            
            // Invalidate BOTH caches
            ItemRepository.cachedItems = null
            cachedDeals = null
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyDeals(): Result<List<Deal>> {
        return try {
            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val uid = prefs.getString("current_uid", null) ?: return Result.failure(Exception("Not logged in"))
            
            val deals = SupabaseManager.client.postgrest["deals"]
                .select {
                    filter { 
                        or {
                            eq("buyer_id", uid)
                            eq("seller_id", uid)
                        }
                    }
                }
                .decodeList<Deal>()
                
            Result.success(deals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllDeals(forceRefresh: Boolean = false): Result<List<Deal>> {
        return try {
            if (!forceRefresh) {
                val cached = cachedDeals
                if (cached != null) return Result.success(cached)
            }

            val deals = SupabaseManager.client.postgrest["deals"].select().decodeList<Deal>()
            cachedDeals = deals
            Result.success(deals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        var cachedDeals: List<Deal>? = null
    }
}
