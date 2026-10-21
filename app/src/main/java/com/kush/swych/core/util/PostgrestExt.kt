package com.kush.swych.core.util

import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonPrimitive

/** Reads an RPC that returns a single text value, which may be null. */
fun PostgrestResult.decodeNullableString(): String? {
    val element = Json.parseToJsonElement(data)
    return if (element is JsonNull) null else element.jsonPrimitive.content
}
