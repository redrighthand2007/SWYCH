package com.kush.cachedeal.core.data

import android.content.Context
import com.kush.cachedeal.core.database.CacheDealDatabase
import com.kush.cachedeal.core.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AuthRepository(private val context: Context) {
    private val userDao = CacheDealDatabase.getDatabase(context).userDao()

    suspend fun registerUser(
        name: String,
        block: String,
        phone: String,
        email: String,
        password: String // We hash this locally or let Supabase handle it in Phase 2
    ): Result<Unit> {
        return try {
            val user = User(
                uid = "user_${UUID.randomUUID().toString().take(8)}",
                name = name,
                block = block,
                phone = phone,
                email = email,
                greenDots = 0,
                redDots = 0
            )
            userDao.insertUser(user)
            // Save mock current user to shared prefs
            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            prefs.edit().putString("current_uid", user.uid).apply()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, pass: String): Result<Unit> {
        // Mock login
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        prefs.edit().putString("current_uid", "user_kush").apply() // Fallback to seeded user
        return Result.success(Unit)
    }

    suspend fun getCurrentUserProfile(): User? {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val uid = prefs.getString("current_uid", "user_kush") ?: "user_kush"
        return userDao.getUser(uid)
    }

    fun getCurrentUserProfileFlow(): Flow<User?> {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val uid = prefs.getString("current_uid", "user_kush") ?: "user_kush"
        return userDao.getUserFlow(uid)
    }

    suspend fun signOut() {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        prefs.edit().remove("current_uid").apply()
    }
}

