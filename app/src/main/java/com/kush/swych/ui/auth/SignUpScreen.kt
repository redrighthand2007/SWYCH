package com.kush.swych.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.kush.swych.core.designsystem.component.DealButton
import com.kush.swych.core.designsystem.theme.SCREEN_FADE_MILLIS
import com.kush.swych.core.util.Constants
import com.kush.swych.ui.navigation.AuthRoute
import com.kush.swych.ui.navigation.LoginRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController, viewModel: SignUpViewModel = hiltViewModel()) {
    var name by viewModel::name
    var hostelBlock by viewModel::hostelBlock
    var email by viewModel::email
    var confirmPassword by viewModel::confirmPassword
    val phone = viewModel.phone
    val password = viewModel.password
    val showError = viewModel.showErrors
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val sentTo = viewModel.confirmationSentTo
        if (sentTo != null) {
            CheckYourInbox(
                email = sentTo,
                onGoToLogin = {
                    navController.navigate(LoginRoute) { popUpTo(AuthRoute) }
                },
                modifier = Modifier.padding(paddingValues)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(durationMillis = SCREEN_FADE_MILLIS, delayMillis = 0))
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && name.isBlank(),
                    supportingText = if (showError && name.isBlank()) {
                        { Text("Required field") }
                    } else null
                )
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(durationMillis = SCREEN_FADE_MILLIS, delayMillis = 50))
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = hostelBlock,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hostel Block") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        isError = showError && hostelBlock.isBlank(),
                        supportingText = if (showError && hostelBlock.isBlank()) {
                            { Text("Required field") }
                        } else null
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        Constants.HOSTEL_BLOCKS.forEach { block ->
                            DropdownMenuItem(
                                text = { Text(block) },
                                onClick = {
                                    hostelBlock = block
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(durationMillis = SCREEN_FADE_MILLIS, delayMillis = 100))
            ) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = viewModel::onPhoneChange,
                    label = { Text("Phone Number") },
                    prefix = { Text("+91") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && !viewModel.isPhoneValid,
                    supportingText = if (showError && !viewModel.isPhoneValid) {
                        { Text("Must be exactly 10 digits") }
                    } else null
                )
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(durationMillis = SCREEN_FADE_MILLIS, delayMillis = 150))
            ) {
                val isEmailValid = viewModel.isEmailValid
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && !isEmailValid,
                    supportingText = if (showError && email.isBlank()) {
                        { Text("Required field") }
                    } else if (showError && !isEmailValid) {
                        { Text("Enter a valid email address") }
                    } else {
                        { Text("Use your VIT email to get the verified V badge right away") }
                    }
                )
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(durationMillis = SCREEN_FADE_MILLIS, delayMillis = 200))
            ) {
                val hasLetterAndNumber = viewModel.passwordHasLetterAndNumber
                val isPasswordValid = viewModel.isPasswordValid

                OutlinedTextField(
                    value = password,
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password Visibility"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && !isPasswordValid,
                    supportingText = if (showError && password.length < 6) {
                        { Text("Must be at least 6 characters") }
                    } else if (showError && !hasLetterAndNumber) {
                        { Text("Must contain letters and numbers") }
                    } else null
                )
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(durationMillis = SCREEN_FADE_MILLIS, delayMillis = 250))
            ) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Confirm Password Visibility"
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && confirmPassword != password,
                    supportingText = if (showError && confirmPassword != password) {
                        { Text("Passwords do not match") }
                    } else null
                )
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(durationMillis = SCREEN_FADE_MILLIS, delayMillis = 300))
            ) {
                Column {
                    DealButton(
                        text = if (isLoading) "Creating Account..." else "Create Account",
                        // On success the nav host sees the new session and opens the main screen
                        onClick = viewModel::register,
                        modifier = Modifier.fillMaxWidth()
                    )

                    AnimatedVisibility(visible = errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/** Shown after sign-up when the student has to confirm their email before logging in. */
@Composable
private fun CheckYourInbox(email: String, onGoToLogin: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MarkEmailRead,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(20.dp))
        Text(
            "Check your inbox",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "We sent a confirmation link to\n$email\n\nOpen it to activate your account, then log in. " +
                "It can take a minute; check Spam if you don't see it.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        DealButton(text = "Go to Login", onClick = onGoToLogin)
    }
}
