package com.kush.swych.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kush.swych.core.data.AuthRepository
import com.kush.swych.core.model.User
import com.kush.swych.core.util.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Profile card, logout and account deletion. Both end the session, and the nav host
 * then returns to the welcome screen, so nothing here navigates.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var user by mutableStateOf<User?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var isDeleting by mutableStateOf(false)
        private set

    /** One-off message for a toast; the screen clears it after showing. */
    var message by mutableStateOf<String?>(null)

    // Reputation counts (green/red dots) are kept up to date by the deal functions in Supabase
    fun load() {
        viewModelScope.launch {
            authRepository.getCurrentUserProfile().onSuccess { user = it }
            isLoading = false
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    fun deleteAccount() {
        if (isDeleting) return
        isDeleting = true
        viewModelScope.launch {
            authRepository.deleteAccount().onFailure {
                isDeleting = false
                message = it.userMessage()
            }
        }
    }
}
