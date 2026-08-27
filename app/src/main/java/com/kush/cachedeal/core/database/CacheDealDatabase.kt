package com.kush.cachedeal.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kush.cachedeal.core.database.dao.DealDao
import com.kush.cachedeal.core.database.dao.ItemDao
import com.kush.cachedeal.core.database.dao.UserDao
import com.kush.cachedeal.core.model.Deal
import com.kush.cachedeal.core.model.Item
import com.kush.cachedeal.core.model.User

@Database(entities = [User::class, Item::class, Deal::class], version = 1, exportSchema = false)
abstract class CacheDealDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun itemDao(): ItemDao
    abstract fun dealDao(): DealDao

    companion object {
        @Volatile
        private var INSTANCE: CacheDealDatabase? = null

        fun getDatabase(context: Context): CacheDealDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CacheDealDatabase::class.java,
                    "cachedeal_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
