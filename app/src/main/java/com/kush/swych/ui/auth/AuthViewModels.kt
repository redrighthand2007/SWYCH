package com.kush.swych.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kush.swych.core.data.AuthRepository
import com.kush.swych.core.data.SignUpOutcome
import com.kush.swych.core.util.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Login form. A successful login changes the session state, and the nav host
 * moves to the main screen, so the form itself never navigates.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)

    fun login() {
        if (isLoading) return
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please enter both email and password."
            return
        }
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            authRepository.loginUser(email.trim(), password)
                .onFailure { errorMessage = it.userMessage() }
            isLoading = false
        }
    }
}

/** Sign-up form with its validation rules. */
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var name by mutableStateOf("")
    var hostelBlock by mutableStateOf("")
    var phone by mutableStateOf("")
        private set
    var email by mutableStateOf("")
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")

    /** Field errors are only shown after the first submit attempt. */
    var showErrors by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    /** Set when the account was created but the email link must be opened before logging in. */
    var confirmationSentTo by mutableStateOf<String?>(null)
        private set

    private val cleanEmail get() = email.trim().lowercase()

    val isNameValid get() = name.isNotBlank()
    val isBlockValid get() = hostelBlock.isNotBlank()
    val isPhoneValid get() = phone.length == 10
    // Any email can sign up; VIT emails also get the verified badge straight away
    val isEmailValid get() = cleanEmail.matches(EMAIL_REGEX)
    val passwordHasLetterAndNumber get() = password.any { it.isLetter() } && password.any { it.isDigit() }
    val isPasswordValid get() = password.length in 6..72 && passwordHasLetterAndNumber
    val isConfirmValid get() = password == confirmPassword

    fun onPhoneChange(value: String) {
        phone = value.filter { it.isDigit() }.take(10)
    }

    fun onPasswordChange(value: String) {
        if (value.length <= 72) password = value
    }

    fun register() {
        if (isLoading) return
        showErrors = true
        if (!(isNameValid && isBlockValid && isPhoneValid && isEmailValid && isPasswordValid && isConfirmValid)) return

        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            authRepository.registerUser(name.trim(), hostelBlock, phone, cleanEmail, password)
                .onSuccess { if (it == SignUpOutcome.ConfirmEmail) confirmationSentTo = cleanEmail }
                .onFailure { errorMessage = it.userMessage() }
            isLoading = false
        }
    }

    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
    }
}
