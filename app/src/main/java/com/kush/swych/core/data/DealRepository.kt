package com.kush.swych.core.data

import com.kush.swych.core.model.Deal
import com.kush.swych.core.util.decodeNullableString
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Singleton
class SupabaseDealRepository @Inject constructor(
    private val client: SupabaseClient,
    private val itemRepository: ItemRepository
) : DealRepository {

    private val db get() = client.postgrest

    override suspend fun createDeal(
        itemId: String,
        itemTitle: String,
        itemPhotoUrl: String,
        sellerId: String,
        agreedPrice: Double
    ): Result<Unit> = runCatching {
        val buyerId = client.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
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

    override suspend fun acceptDeal(dealId: String): Result<Unit> = callDealFunction("accept_deal", dealId)

    override suspend fun rejectDeal(dealId: String): Result<Unit> = callDealFunction("reject_deal", dealId)

    override suspend fun cancelDeal(dealId: String): Result<Unit> = callDealFunction("cancel_deal", dealId)

    override suspend fun confirmDeal(dealId: String): Result<Unit> = callDealFunction("confirm_deal", dealId)

    override suspend fun dismissDeal(dealId: String): Result<Unit> = runCatching {
        val deleted = db["deals"].delete {
            select()
            filter { eq("id", dealId) }
        }.decodeList<Deal>()
        if (deleted.isEmpty()) throw Exception("This deal can't be removed yet.")
    }

    // Row level security limits the result to the signed-in user's deals
    override suspend fun getMyDeals(expireStale: Boolean): Result<List<Deal>> = runCatching {
        // Closes accepted deals older than 3 hours; failing here must not block the list
        if (expireStale) runCatching { db.rpc("expire_stale_deals") }
        db["deals"].select {
            order("timestamp", Order.DESCENDING)
        }.decodeList<Deal>()
    }

    override suspend fun getContactPhone(dealId: String): Result<String?> = runCatching {
        db.rpc("get_deal_contact", buildJsonObject { put("p_deal_id", dealId) }).decodeNullableString()
    }

    private suspend fun callDealFunction(name: String, dealId: String): Result<Unit> = runCatching {
        db.rpc(name, buildJsonObject { put("p_deal_id", dealId) })
        itemRepository.clearCache()
    }
}
