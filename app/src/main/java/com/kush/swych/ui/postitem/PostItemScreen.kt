package com.kush.swych.ui.postitem

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.kush.swych.core.data.ItemRepository
import com.kush.swych.core.designsystem.component.DealButton
import com.kush.swych.core.model.Category
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostItemScreen(navController: NavController, onNavigateHome: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val itemRepo = remember { ItemRepository(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // Validation errors
    var titleError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf(false) }

    // Image picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        if (uri != null) imageError = false
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("List an Item", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // — Photo Picker —
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (imageError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = 2.dp,
                            color = if (imageError) MaterialTheme.colorScheme.error
                            else if (selectedImageUri != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = if (imageError) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Tap to add a photo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (imageError) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (imageError) {
                                Text(
                                    "Photo is required",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // — Category Dropdown —
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.displayName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = categoryError,
                        supportingText = if (categoryError) {{ Text("Select a category") }} else null
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        Category.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                    categoryError = false
                                }
                            )
                        }
                    }
                }

                // — Title —
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    label = { Text("Item Title") },
                    placeholder = { Text("e.g. Barely used MTB cycle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = titleError,
                    supportingText = if (titleError) {{ Text("Title is required") }} else null
                )

                // — Description —
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Condition, reason for selling, etc.") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                // — Price —
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' }; priceError = false },
                    label = { Text("Price (₹)") },
                    placeholder = { Text("0") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    isError = priceError,
                    supportingText = if (priceError) {{ Text("Enter a valid price") }} else null
                )

                Spacer(Modifier.height(4.dp))

                // — Submit Button —
                DealButton(
                    text = if (isSubmitting) "Listing..." else "List Item 🚀",
                    isLoading = isSubmitting,
                    onClick = {
                        // Validate
                        titleError = title.isBlank()
                        priceError = price.isBlank() || price.toDoubleOrNull() == null
                        categoryError = selectedCategory == null
                        imageError = selectedImageUri == null

                        if (!titleError && !priceError && !categoryError && !imageError) {
                            isSubmitting = true
                            scope.launch {
                                val result = itemRepo.postItem(
                                    title = title.trim(),
                                    description = description.trim(),
                                    price = price.toDouble(),
                                    category = selectedCategory!!.displayName,
                                    photoUri = selectedImageUri.toString()
                                )
                                isSubmitting = false
                                if (result.isSuccess) {
                                    snackbarHostState.showSnackbar("✅ Item listed successfully!")
                                    kotlinx.coroutines.delay(800)
                                    onNavigateHome()
                                } else {
                                    snackbarHostState.showSnackbar(
                                        result.exceptionOrNull()?.message ?: "Failed to list item"
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
