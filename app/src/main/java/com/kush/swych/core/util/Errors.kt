package com.kush.swych.core.util

import android.util.Log
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException

private const val TAG = "Swych"
private const val GENERIC = "Something went wrong. Please try again."

// Wording in raw database / server errors that must never reach the user
private val TECHNICAL_MARKERS = listOf(
    "violates", "constraint", "relation ", "column ", "permission denied", "syntax",
    "null value", "invalid input", "function ", "schema", "postgres", "pgrst", "jwt", "sql"
)

/**
 * Turns Supabase and network errors into a message that can be shown to the user.
 * Known cases get friendly wording; messages written on purpose (by the app or by the deal
 * functions in Supabase) are passed through; anything else becomes a generic message and the
 * details are only logged.
 */
fun Throwable.userMessage(): String {
    if (this is HttpRequestException) return "No internet connection. Please try again."
    val raw = if (this is RestException) "$error ${description.orEmpty()}" else message.orEmpty()
    val lower = raw.lowercase()
    return when {
        "deals_one_live_offer" in raw -> "You already have an offer on this item."
        "users_phone_unique" in raw || "users_email_unique" in raw ||
            "Database error saving new user" in raw -> "This phone number or email is already registered."
        "User already registered" in raw -> "An account with this email already exists."
        "Invalid login credentials" in raw -> "Wrong email/phone or password."
        "Email not confirmed" in raw -> "Please confirm your email from the link we sent, then log in."
        "row-level security" in raw -> "This item is no longer available."
        "jwt" in lower || "not authenticated" in lower -> "Your session has expired. Please log in again."
        else -> {
            // Our own messages are plain sentences; raw server errors are logged, not shown
            val readable = if (this is RestException) error else message
            if (readable.isNullOrBlank() || TECHNICAL_MARKERS.any { it in readable.lowercase() } ||
                (this !is RestException && this::class != Exception::class)
            ) {
                Log.w(TAG, "Unexpected error", this)
                GENERIC
            } else {
                readable
            }
        }
    }
}
