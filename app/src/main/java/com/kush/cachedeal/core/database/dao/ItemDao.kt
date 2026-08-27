package com.kush.cachedeal.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kush.cachedeal.core.model.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY createdAt DESC")
    suspend fun getAllItems(): List<Item>

    @Query("SELECT * FROM items ORDER BY createdAt DESC")
    fun getAllItemsFlow(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getItem(id: String): Item?

    @Query("SELECT * FROM items WHERE sellerId = :sellerId ORDER BY createdAt DESC")
    suspend fun getItemsBySeller(sellerId: String): List<Item>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<Item>): List<Long>

    @Query("UPDATE items SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String): Int

    @Query("DELETE FROM items")
    suspend fun clearAll(): Int
}

