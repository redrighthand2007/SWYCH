package com.kush.cachedeal.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kush.cachedeal.core.data.AuthRepository
import com.kush.cachedeal.core.designsystem.component.DealButton
import com.kush.cachedeal.core.util.Constants
import kotlinx.coroutines.launch
import com.kush.cachedeal.ui.navigation.MainRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var hostelBlock by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val authRepo = remember(context) { AuthRepository(context) }

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
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300, delayMillis = 0)
                ) + fadeIn(tween(300))
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
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300, delayMillis = 50)
                ) + fadeIn(tween(300))
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
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300, delayMillis = 100)
                ) + fadeIn(tween(300))
            ) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 10) phone = it },
                    label = { Text("Phone Number") },
                    prefix = { Text("+91") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && phone.length != 10,
                    supportingText = if (showError && phone.length != 10) {
                        { Text("Must be exactly 10 digits") }
                    } else null
                )
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300, delayMillis = 150)
                ) + fadeIn(tween(300))
            ) {
                val isEmailValid = email.isNotBlank() && email.endsWith("@vitstudent.ac.in")
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (@vitstudent.ac.in)") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && !isEmailValid,
                    supportingText = if (showError && email.isBlank()) {
                        { Text("Required field") }
                    } else if (showError && !email.endsWith("@vitstudent.ac.in")) {
                        { Text("Must be a @vitstudent.ac.in address") }
                    } else null
                )
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300, delayMillis = 200)
                ) + fadeIn(tween(300))
            ) {
                val hasLetterAndNumber = password.any { it.isLetter() } && password.any { it.isDigit() }
                val isPasswordValid = password.length in 6..9 && hasLetterAndNumber
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { if (it.length < 10) password = it },
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
                        { Text("Firebase requires min 6 characters") }
                    } else if (showError && !hasLetterAndNumber) {
                        { Text("Must contain letters and numbers") }
                    } else null
                )
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300, delayMillis = 250)
                ) + fadeIn(tween(300))
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
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300, delayMillis = 300)
                ) + fadeIn(tween(300))
            ) {
                Column {
                    DealButton(
                        text = if (isLoading) "Creating Account..." else "Create Account",
                        onClick = {
                            if (isLoading) return@DealButton
                            showError = true
                            val isNameValid = name.isNotBlank()
                            val isBlockValid = hostelBlock.isNotBlank()
                            val isPhoneValid = phone.length == 10
                            val isEmailValid = email.isNotBlank() && email.endsWith("@vitstudent.ac.in")
                            val hasLetterAndNumber = password.any { it.isLetter() } && password.any { it.isDigit() }
                            val isPasswordValid = password.length in 6..9 && hasLetterAndNumber
                            val isConfirmValid = password == confirmPassword

                            if (isNameValid && isBlockValid && isPhoneValid && isEmailValid && isPasswordValid && isConfirmValid) {
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    val result = authRepo.registerUser(name, hostelBlock, phone, email, password)
                                    isLoading = false
                                    if (result.isSuccess) {
                                        navController.navigate(MainRoute) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
                                    }
                                }
                            }
                        },
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




