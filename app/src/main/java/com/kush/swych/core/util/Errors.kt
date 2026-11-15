package com.kush.swych.core.util

import android.util.Log
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import java.io.IOException
import java.nio.channels.UnresolvedAddressException

private const val TAG = "Swych"
private const val GENERIC = "Something went wrong on our side. Please try again in a minute."

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
    if (this is HttpRequestException || this is IOException || this is UnresolvedAddressException) {
        return "No internet connection. Check your Wi-Fi or mobile data and try again."
    }
    if (this is AuthRestException) authMessage(errorCode?.value)?.let { return it }
    if (this is RestException && statusCode == 429) return "Too many attempts. Please wait a few minutes and try again."
    val raw = if (this is RestException) "$error ${description.orEmpty()}" else message.orEmpty()
    val lower = raw.lowercase()
    return when {
        "deals_one_live_offer" in raw -> "You already have an offer on this item."
        "users_phone_unique" in raw -> PHONE_TAKEN
        "users_email_unique" in raw || "User already registered" in raw -> EMAIL_TAKEN
        "Database error saving new user" in raw -> PHONE_TAKEN
        "confirmation email" in lower || "sending" in lower && "email" in lower -> EMAIL_SEND_FAILED
        "Invalid login credentials" in raw -> WRONG_LOGIN
        "Email not confirmed" in raw -> EMAIL_NOT_CONFIRMED
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

private const val PHONE_TAKEN = "This phone number is already registered. Log in, or use a different number."
private const val EMAIL_TAKEN = "An account with this email already exists. Log in instead."
private const val WRONG_LOGIN = "Wrong email/phone or password."
private const val EMAIL_NOT_CONFIRMED = "Your email isn't confirmed yet. Open the link we emailed you, then log in."
private const val EMAIL_SEND_FAILED = "We couldn't send the confirmation email. Please try again in a few minutes."

/** Supabase Auth error codes mapped to plain sentences; null means "use the general rules". */
private fun authMessage(code: String?): String? = when (code) {
    "invalid_credentials" -> WRONG_LOGIN
    "email_not_confirmed" -> EMAIL_NOT_CONFIRMED
    "email_exists", "user_already_exists" -> EMAIL_TAKEN
    "phone_exists" -> PHONE_TAKEN
    "weak_password" -> "That password is too weak. Use at least 6 characters with letters and numbers."
    "same_password" -> "Your new password must be different from the old one."
    "email_address_invalid", "validation_failed" -> "That email address doesn't look right. Please check it."
    "over_email_send_rate_limit" -> "Too many emails sent. Please wait a few minutes before trying again."
    "over_request_rate_limit" -> "Too many attempts. Please wait a few minutes and try again."
    "user_banned" -> "This account has been blocked. Contact support from the Play Store listing."
    "user_not_found" -> "No account found with these details."
    "signup_disabled" -> "New sign-ups are paused right now. Please try again later."
    "session_expired", "session_not_found", "refresh_token_not_found", "refresh_token_already_used" ->
        "Your session has expired. Please log in again."
    "request_timeout" -> "The server took too long to answer. Please try again."
    else -> null
}
