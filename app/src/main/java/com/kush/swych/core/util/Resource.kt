package com.kush.swych.core.util

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Exception? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}
