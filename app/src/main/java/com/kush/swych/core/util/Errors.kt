package com.kush.swych.core.util

import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException

/** Turns Supabase and network errors into a message that can be shown to the user. */
fun Throwable.userMessage(): String {
    if (this is HttpRequestException) return "No internet connection. Please try again."
    val raw = if (this is RestException) "$error ${description.orEmpty()}" else message.orEmpty()
    return when {
        "deals_one_live_offer" in raw -> "You already have an offer on this item."
        "users_phone_unique" in raw || "users_email_unique" in raw ||
            "Database error saving new user" in raw -> "This phone number or email is already registered."
        "User already registered" in raw -> "An account with this email already exists."
        "Invalid login credentials" in raw -> "Wrong email/phone or password."
        "Email not confirmed" in raw -> "Please confirm your email from the link we sent, then log in."
        "row-level security" in raw -> "This item is no longer available."
        "JWT" in raw || "not authenticated" in raw.lowercase() -> "Your session has expired. Please log in again."
        this is RestException -> error
        else -> message ?: "Something went wrong. Please try again."
    }
}
