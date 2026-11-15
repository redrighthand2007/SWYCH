package com.kush.swych.core.network

import com.kush.swych.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseManager {
    val client: SupabaseClient by lazy {
        // Set in local.properties (see README); the publishable key is safe in the app
        // because every table is protected by row level security.
        check(BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_KEY.isNotBlank()) {
            "Supabase is not configured: add supabase.url and supabase.key to local.properties"
        }
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }

    /** Id of the signed-in user, taken from the Supabase session. */
    val currentUid: String?
        get() = client.auth.currentUserOrNull()?.id
}
