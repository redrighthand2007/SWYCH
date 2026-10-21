package com.kush.swych.core.data

import android.content.Context
import androidx.core.content.edit
import com.kush.swych.core.model.User
import com.kush.swych.core.network.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import com.kush.swych.core.util.decodeNullableString
import kotlinx.serialization.json.put

class AuthRepository(private val context: Context) {

    companion object {
        @Volatile
        var cachedUsers: List<User>? = null

        // Phone and email are only readable through get_my_profile / get_deal_contact.
        private val PUBLIC_USER_COLUMNS = Columns.list("uid", "name", "block", "green_dots", "red_dots")
    }

    private val auth get() = SupabaseManager.client.auth
    private val db get() = SupabaseManager.client.postgrest

    /**
     * Creates the auth account. The profile row is created by the on_auth_user_created
     * trigger from the metadata below, in the same transaction, so a failed profile
     * (for example a duplicate phone number) also fails the sign-up.
     */
    suspend fun registerUser(
        name: String,
        block: String,
        phone: String,
        email: String,
        password: String
    ): Result<Unit> = runCatching {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("name", name)
                put("block", block)
                put("phone", phone)
            }
        }
        // Sign-up only returns a session when email confirmation is off
        if (auth.currentSessionOrNull() == null) {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }
        cachedUsers = null
    }

    suspend fun getCurrentUserProfile(): Result<User> = runCatching {
        db.rpc("get_my_profile").decodeList<User>().firstOrNull()
            ?: throw Exception("Profile not found. Please log out and sign up again.")
    }

    suspend fun loginUser(phoneOrEmail: String, password: String): Result<Unit> = runCatching {
        val input = phoneOrEmail.trim()
        val email = if (input.contains("@")) {
            input.lowercase()
        } else {
            val phone = input.filter { it.isDigit() }
            db.rpc("get_login_email", buildJsonObject { put("p_phone", phone) }).decodeNullableString()
                ?: throw Exception("No account found with this phone number.")
        }
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        cachedUsers = null
    }

    val currentUserUid: String?
        get() = SupabaseManager.currentUid

    suspend fun getAllUsers(forceRefresh: Boolean = false): Result<List<User>> {
        val cached = cachedUsers
        if (!forceRefresh && cached != null) return Result.success(cached)
        return runCatching {
            db["users"].select(PUBLIC_USER_COLUMNS).decodeList<User>().also { cachedUsers = it }
        }
    }

    /** Users by id, refetching when someone new (e.g. a user who just signed up) is missing. */
    suspend fun getUsers(ids: Collection<String>): Map<String, User> {
        var users = getAllUsers().getOrNull().orEmpty().associateBy { it.uid }
        if (ids.any { it !in users }) {
            users = getAllUsers(forceRefresh = true).getOrNull()?.associateBy { it.uid } ?: users
        }
        return users
    }

    suspend fun logout() {
        try {
            auth.signOut()
        } catch (_: Exception) {
            // Still clear local state if the network call fails
        }
        // Older versions kept the signed-in user here
        context.getSharedPreferences("auth", Context.MODE_PRIVATE).edit { clear() }
        cachedUsers = null
        ItemRepository.cachedItems = null
    }
}
