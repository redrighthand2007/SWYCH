package com.kush.swych.ui.postitem

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kush.swych.core.data.ItemRepository
import com.kush.swych.core.designsystem.component.DealButton
import com.kush.swych.core.model.Category
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

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
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var savedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // Validation errors
    var titleError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf(false) }

    // Camera picker
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            selectedImageBitmap = bitmap
            imageError = false
            // Save bitmap to cache dir to get a URI for later upload
            val file = File(context.cacheDir, "item_photo_{System.currentTimeMillis()}.jpg")
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
            savedImageUri = Uri.fromFile(file)
        }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Sell an Item",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    
                    // Photo Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (imageError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) 
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                2.dp,
                                if (imageError) MaterialTheme.colorScheme.error else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { cameraLauncher.launch(null) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageBitmap != null) {
                            Image(
                                bitmap = selectedImageBitmap!!.asImageBitmap(),
                                contentDescription = "Item Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f))
                            )
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Retake Photo",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Take Photo",
                                    tint = if (imageError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (imageError) "Photo is compulsory" else "Take a photo of your item",
                                    color = if (imageError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Item Name
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it; titleError = false },
                        label = { Text("Item Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = titleError,
                        singleLine = true
                    )

                    // Category
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            isError = categoryError
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            Category.values().forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryError = false
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Price
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it; priceError = false },
                        label = { Text("Price (?)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = priceError,
                        singleLine = true
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Little Description (Optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DealButton(
                        text = "List Item",
                        onClick = {
                            if (title.isBlank()) titleError = true
                            if (price.isBlank() || price.toDoubleOrNull() == null) priceError = true
                            if (selectedCategory == null) categoryError = true
                            if (savedImageUri == null) imageError = true

                            if (!titleError && !priceError && !categoryError && !imageError) {
                                isSubmitting = true
                                scope.launch {
                                    val result = itemRepo.postItem(
                                        title = title,
                                        description = description,
                                        price = price.toDouble(),
                                        category = selectedCategory!!.name,
                                        photoUri = savedImageUri.toString()
                                    )
                                    isSubmitting = false
                                    result.onSuccess {
                                        onNavigateHome()
                                    }.onFailure { e ->
                                        snackbarHostState.showSnackbar("Failed: " + e.message)
                                    }
                                }
                            }
                        },
                        isLoading = isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

