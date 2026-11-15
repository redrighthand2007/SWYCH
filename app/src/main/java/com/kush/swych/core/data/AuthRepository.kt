package com.kush.swych.core.data

import android.content.Context
import androidx.core.content.edit
import com.kush.swych.BuildConfig
import com.kush.swych.core.model.User
import com.kush.swych.core.notifications.DealNotifier
import com.kush.swych.core.util.decodeNullableString
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Singleton
class SupabaseAuthRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val client: SupabaseClient,
    private val itemRepository: ItemRepository
) : AuthRepository {

    private companion object {
        // Phone and email are only readable through get_my_profile / get_deal_contact.
        val PUBLIC_USER_COLUMNS = Columns.list("uid", "name", "block", "green_dots", "red_dots", "vit_verified")
    }

    private val auth get() = client.auth
    private val http by lazy { HttpClient(CIO) }
    private val db get() = client.postgrest
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var cachedUsers: List<User>? = null

    override val sessionState: StateFlow<SessionState> = auth.sessionStatus
        .map { it.toSessionState() }
        .stateIn(scope, SharingStarted.Eagerly, auth.sessionStatus.value.toSessionState())

    override val currentUserUid: String?
        get() = auth.currentUserOrNull()?.id

    override suspend fun awaitInitialized() = auth.awaitInitialization()

    /**
     * Creates the auth account. The profile row is created by the on_auth_user_created
     * trigger from the metadata below, in the same transaction, so a failed profile
     * (for example a duplicate phone number) also fails the sign-up.
     */
    override suspend fun registerUser(
        name: String,
        block: String,
        phone: String,
        email: String,
        password: String
    ): Result<SignUpOutcome> = runCatching {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("name", name)
                put("block", block)
                put("phone", phone)
            }
        }
        cachedUsers = null
        // Sign-up only returns a session when email confirmation is off; otherwise the
        // student has to open the link in their inbox before they can log in
        if (auth.currentSessionOrNull() != null) SignUpOutcome.SignedIn else SignUpOutcome.ConfirmEmail
    }

    override suspend fun getCurrentUserProfile(): Result<User> = runCatching {
        db.rpc("get_my_profile").decodeList<User>().firstOrNull()
            ?: throw Exception("Profile not found. Please log out and sign up again.")
    }

    override suspend fun loginUser(phoneOrEmail: String, password: String): Result<Unit> = runCatching {
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

    override suspend fun getUsers(ids: Collection<String>, forceRefresh: Boolean): Map<String, User> {
        var users = fetchUsers(forceRefresh).associateBy { it.uid }
        if (ids.any { it !in users }) {
            users = fetchUsers(forceRefresh = true).associateBy { it.uid }.ifEmpty { users }
        }
        return users
    }

    private suspend fun fetchUsers(forceRefresh: Boolean): List<User> {
        val cached = cachedUsers
        if (!forceRefresh && cached != null) return cached
        return runCatching { db["users"].select(PUBLIC_USER_COLUMNS).decodeList<User>() }
            .onSuccess { cachedUsers = it }
            .getOrElse { cached.orEmpty() }
    }

    override suspend fun requestVitVerification(vitEmail: String): Result<Unit> = runCatching {
        val accessToken = auth.currentSessionOrNull()?.accessToken ?: throw Exception("Please log in again.")
        val response = http.post("${BuildConfig.SUPABASE_URL}/functions/v1/request-vit-verification") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            header("apikey", BuildConfig.SUPABASE_KEY)
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("email", vitEmail.trim().lowercase()) }.toString())
        }
        if (!response.status.isSuccess()) {
            // The function returns {"error": "<message for the user>"}
            val message = runCatching {
                Json.parseToJsonElement(response.bodyAsText()).jsonObject["error"]?.jsonPrimitive?.content
            }.getOrNull()
            throw Exception(message ?: "Couldn't send the verification email. Please try again.")
        }
    }

    /** Permanently deletes the account, its listings and deals, then signs out locally. */
    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        db.rpc("delete_my_account")
        logout()
    }

    override suspend fun logout() {
        try {
            auth.signOut()
        } catch (_: Exception) {
            // Still clear local state if the network call fails
        }
        // Older versions kept the signed-in user here
        context.getSharedPreferences("auth", Context.MODE_PRIVATE).edit { clear() }
        // Next account on this phone starts with a clean slate, without old deal alerts
        DealNotifier.clear(context)
        cachedUsers = null
        itemRepository.clearCache()
    }

    private fun SessionStatus.toSessionState() = when (this) {
        is SessionStatus.Initializing -> SessionState.Loading
        is SessionStatus.Authenticated -> SessionState.SignedIn
        // A failed refresh (e.g. offline) keeps the saved session; the user stays signed in
        is SessionStatus.RefreshFailure -> SessionState.SignedIn
        is SessionStatus.NotAuthenticated -> SessionState.SignedOut
    }
}
