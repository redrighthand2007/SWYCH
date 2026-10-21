package com.kush.swych.core.data

import android.content.Context
import com.kush.swych.core.model.Deal
import com.kush.swych.core.network.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import com.kush.swych.core.util.decodeNullableString
import kotlinx.serialization.json.put
import java.util.UUID

/**
 * Offers and deals. Creating an offer is a plain insert (checked by row level security);
 * every later status change runs in a Supabase function so it happens in one transaction.
 */
class DealRepository(@Suppress("unused") private val context: Context) {

    private val db get() = SupabaseManager.client.postgrest

    suspend fun createDeal(
        itemId: String,
        itemTitle: String,
        itemPhotoUrl: String,
        sellerId: String,
        agreedPrice: Double
    ): Result<Unit> = runCatching {
        val buyerId = SupabaseManager.currentUid ?: throw Exception("Not logged in")
        val deal = Deal(
            id = "deal_${UUID.randomUUID().toString().take(8)}",
            itemId = itemId,
            buyerId = buyerId,
            sellerId = sellerId,
            itemTitle = itemTitle,
            itemPhotoUrl = itemPhotoUrl,
            finalPrice = agreedPrice
        )
        db["deals"].insert(deal)
    }

    /** Seller accepts: deal ACCEPTED, item LOCKED, other offers REJECTED. */
    suspend fun acceptDeal(dealId: String): Result<Unit> = callDealFunction("accept_deal", dealId)

    suspend fun rejectDeal(dealId: String): Result<Unit> = callDealFunction("reject_deal", dealId)

    /** Buyer withdraws a pending offer, or either side backs out of an accepted deal. */
    suspend fun cancelDeal(dealId: String): Result<Unit> = callDealFunction("cancel_deal", dealId)

    /** Confirms the hand-over; the deal completes once both sides have confirmed. */
    suspend fun confirmDeal(dealId: String): Result<Unit> = callDealFunction("confirm_deal", dealId)

    /** Removes a rejected, cancelled or expired deal from the list. */
    suspend fun dismissDeal(dealId: String): Result<Unit> = runCatching {
        val deleted = db["deals"].delete {
            select()
            filter { eq("id", dealId) }
        }.decodeList<Deal>()
        if (deleted.isEmpty()) throw Exception("This deal can't be removed yet.")
    }

    /** Deals where the signed-in user is buyer or seller (row level security limits it to those). */
    suspend fun getMyDeals(expireStale: Boolean = true): Result<List<Deal>> = runCatching {
        // Closes accepted deals older than 3 days; failing here must not block the list
        if (expireStale) runCatching { db.rpc("expire_stale_deals") }
        db["deals"].select {
            order("timestamp", Order.DESCENDING)
        }.decodeList<Deal>()
    }

    /** The other side's phone number, available once the deal is accepted. */
    suspend fun getContactPhone(dealId: String): Result<String?> = runCatching {
        db.rpc("get_deal_contact", buildJsonObject { put("p_deal_id", dealId) }).decodeNullableString()
    }

    private suspend fun callDealFunction(name: String, dealId: String): Result<Unit> = runCatching {
        db.rpc(name, buildJsonObject { put("p_deal_id", dealId) })
        ItemRepository.cachedItems = null
    }
}
