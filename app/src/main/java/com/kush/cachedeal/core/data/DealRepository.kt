package com.kush.cachedeal.core.data

import android.content.Context
import com.kush.cachedeal.core.database.CacheDealDatabase
import com.kush.cachedeal.core.model.Deal
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class DealRepository(private val context: Context) {
    private val dealDao = CacheDealDatabase.getDatabase(context).dealDao()

    fun getMyDealsFlow(userId: String): Flow<List<Deal>> {
        return dealDao.getMyDealsFlow(userId)
    }

    suspend fun getMyDeals(userId: String): List<Deal> {
        return dealDao.getMyDeals(userId)
    }

    suspend fun createDeal(deal: Deal): Result<Unit> {
        return try {
            val newDeal = if (deal.id.isEmpty()) {
                deal.copy(id = UUID.randomUUID().toString())
            } else deal
            dealDao.insertDeal(newDeal)
            // TODO: In Phase 2, insert into Supabase
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDealStatus(dealId: String, newStatus: String): Result<Unit> {
        return try {
            dealDao.updateStatus(dealId, newStatus)
            // TODO: In Phase 2, update Supabase
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

