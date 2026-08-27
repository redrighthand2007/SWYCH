package com.kush.cachedeal.core.database

import android.content.Context
import com.kush.cachedeal.core.model.Deal
import com.kush.cachedeal.core.model.Item
import com.kush.cachedeal.core.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseSeeder {
    fun seedDatabase(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = CacheDealDatabase.getDatabase(context)
            
            // Only seed if empty
            if (db.userDao().getUser("user_kush") == null) {
                // Seed User
                val user = User(
                    uid = "user_kush",
                    phone = "9876543210",
                    email = "kush@vitstudent.ac.in",
                    name = "Kush",
                    block = "MH-B",
                    greenDots = 5,
                    redDots = 0
                )
                db.userDao().insertUser(user)

                // Seed Items
                val items = listOf(
                    Item(
                        id = "item_1",
                        sellerId = "user_2",
                        sellerName = "Aarav",
                        sellerBlock = "MH-D",
                        sellerGreenDots = 12,
                        category = "Study Material",
                        title = "Engineering Drawing Kit (Full)",
                        description = "Used for 1 semester, excellent condition. Includes drafter and sheets.",
                        price = 450.0,
                        photoUrl = "https://images.unsplash.com/photo-1628151015968-3a4429e9ef04?w=400&q=80",
                        status = "open"
                    ),
                    Item(
                        id = "item_2",
                        sellerId = "user_kush",
                        sellerName = "Kush",
                        sellerBlock = "MH-B",
                        category = "Electronics",
                        title = "Casio fx-991EX Scientific Calc",
                        description = "Barely used, working perfectly.",
                        price = 800.0,
                        photoUrl = "https://images.unsplash.com/photo-1594980596870-8aa52a78d8cd?w=400&q=80",
                        status = "open"
                    )
                )
                db.itemDao().insertItems(items)

                // Seed Deals
                val deals = listOf(
                    Deal(
                        id = "deal_1",
                        itemId = "item_2",
                        itemTitle = "Casio fx-991EX Scientific Calc",
                        itemPhotoUrl = "https://images.unsplash.com/photo-1594980596870-8aa52a78d8cd?w=400&q=80",
                        sellerId = "user_kush",
                        sellerName = "Kush",
                        sellerPhone = "9876543210",
                        buyerId = "user_3",
                        buyerName = "Rohan",
                        buyerPhone = "9988776655",
                        finalPrice = 800.0,
                        status = "locked"
                    )
                )
                db.dealDao().insertDeals(deals)
            }
        }
    }
}
