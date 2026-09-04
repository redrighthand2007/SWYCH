package com.kush.swych.core.data

import android.content.Context
import androidx.core.content.edit
import com.kush.swych.core.model.User
import com.kush.swych.core.network.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest
import java.util.UUID

class AuthRepository(private val context: Context) {

    companion object {
        var cachedUsers: List<User>? = null
    }

    suspend fun registerUser(
        name: String,
        block: String,
        phone: String,
        email: String,
        password: String 
    ): Result<Unit> {
        return try {
            val user = User(
                uid = "user_",
                name = name,
                block = block,
                phone = phone,
                email = email
            )
            
            SupabaseManager.client.postgrest["users"].insert(user)
            
            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            prefs.edit { putString("current_uid", user.uid) }
            
            cachedUsers = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUserProfile(): Result<User> {
        return try {
            val uid = currentUserUid ?: return Result.failure(Exception("Not logged in"))
            val cached = cachedUsers?.find { it.uid == uid }
            if (cached != null) return Result.success(cached)

            val user = SupabaseManager.client.postgrest["users"]
                .select { filter { eq("uid", uid) } }
                .decodeSingle<User>()
            Result.success(user)
        } catch(e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            val user = SupabaseManager.client.postgrest["users"]
                .select { filter { eq("email", email) } }
                .decodeSingleOrNull<User>()
            
            if (user != null) {
                val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                prefs.edit { putString("current_uid", user.uid) }
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    val currentUserUid: String?
        get() {
            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            return prefs.getString("current_uid", null)
        }

    suspend fun getAllUsers(forceRefresh: Boolean = false): Result<List<User>> {
        if (!forceRefresh && cachedUsers != null) {
            return Result.success(cachedUsers!!)
        }
        return try {
            val users = SupabaseManager.client.postgrest["users"]
                .select()
                .decodeList<User>()
            cachedUsers = users
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        prefs.edit { clear() }
        cachedUsers = null
    }
}
