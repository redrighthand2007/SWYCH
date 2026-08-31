package com.kush.swych.core.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseManager {
    private const val SUPABASE_URL = "https://brmngeqpfudsdpgjyimm.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_YM9MrcZObK_rYFLFFb5I5A_mu__BKxN"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}
