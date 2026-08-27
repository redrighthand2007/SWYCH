package com.kush.cachedeal.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kush.cachedeal.core.model.Deal
import kotlinx.coroutines.flow.Flow

@Dao
interface DealDao {
    @Query("SELECT * FROM deals WHERE buyerId = :uid OR sellerId = :uid ORDER BY lockedAt DESC")
    suspend fun getMyDeals(uid: String): List<Deal>

    @Query("SELECT * FROM deals WHERE buyerId = :uid OR sellerId = :uid ORDER BY lockedAt DESC")
    fun getMyDealsFlow(uid: String): Flow<List<Deal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeal(deal: Deal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeals(deals: List<Deal>): List<Long>

    @Query("UPDATE deals SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String): Int

    @Query("DELETE FROM deals")
    suspend fun clearAll(): Int
}

