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
            
            ItemRepository.cachedItems = null // invalidate cache
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDealStatus(dealId: String, itemId: String, newStatus: String): Result<Unit> {
        return try {
            // Update deal status
            SupabaseManager.client.postgrest["deals"].update({
                set("status", newStatus)
            }) {
                filter { eq("id", dealId) }
            }
            
            // If new status is REJECTED, item goes back to OPEN. If SOLD, item goes to SOLD.
            val itemStatus = if (newStatus == "REJECTED") "OPEN" else "SOLD"
            SupabaseManager.client.postgrest["items"].update({
                set("status", itemStatus)
            }) {
                filter { eq("id", itemId) }
            }
            
            ItemRepository.cachedItems = null // invalidate cache
            
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
                        // Simplified filter: buyer or seller. 
                        // In PostgREST, we'd use 'or' filter, but doing simple fetch for MVP
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
}
