package com.kush.swych.ui.postitem

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.kush.swych.core.data.ItemRepository
import com.kush.swych.core.designsystem.component.DealButton
import com.kush.swych.core.model.Category
import com.kush.swych.core.util.ImageUtils
import com.kush.swych.core.util.userMessage
import kotlinx.coroutines.launch
import java.io.File

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
    // Downscaled JPEG ready to upload, shown as the preview
    var photoFile by remember { mutableStateOf<File?>(null) }
    var isPreparingPhoto by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // Validation errors
    var titleError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf(false) }

    fun usePhoto(source: Uri) {
        isPreparingPhoto = true
        scope.launch {
            val prepared = ImageUtils.prepareForUpload(context, source)
            isPreparingPhoto = false
            if (prepared != null) {
                photoFile = prepared
                imageError = false
            } else {
                snackbarHostState.showSnackbar("Couldn't read that photo. Try another one.")
            }
        }
    }

    // Camera writes a full-size photo into a file we own
    var cameraTarget by remember { mutableStateOf<Pair<File, Uri>?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val target = cameraTarget
        cameraTarget = null
        if (saved && target != null) usePhoto(target.second) else target?.first?.delete()
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) usePhoto(uri)
    }

    fun openCamera() {
        val target = ImageUtils.newCameraTarget(context)
        cameraTarget = target
        try {
            cameraLauncher.launch(target.second)
        } catch (_: ActivityNotFoundException) {
            cameraTarget = null
            scope.launch { snackbarHostState.showSnackbar("No camera app found. Choose a photo from your gallery instead.") }
        }
    }

    fun openGallery() {
        try {
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } catch (_: ActivityNotFoundException) {
            scope.launch { snackbarHostState.showSnackbar("No gallery app found.") }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Bottom padding keeps the List Item button clear of the floating nav bar
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .heightIn(min = 180.dp)
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
                    .clickable(enabled = !isPreparingPhoto) { openCamera() },
                contentAlignment = Alignment.Center
            ) {
                val preview = photoFile
                if (isPreparingPhoto) {
                    CircularProgressIndicator()
                } else if (preview != null) {
                    AsyncImage(
                        model = preview,
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

            TextButton(
                onClick = { openGallery() },
                enabled = !isPreparingPhoto,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Choose from gallery")
            }

            // Item Name
            OutlinedTextField(
                value = title,
                onValueChange = { newValue ->
                    title = newValue.trimStart()
                    titleError = false
                },
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
                    value = selectedCategory?.displayName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = categoryError
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
                onValueChange = { price = it.filter { c -> c.isDigit() }.take(6); priceError = false },
                label = { Text("Price (₹)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = priceError,
                supportingText = if (priceError) { { Text("Enter a price above ₹0") } } else null,
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
                    val priceValue = price.toDoubleOrNull()
                    val category = selectedCategory
                    val photo = photoFile
                    titleError = title.isBlank()
                    priceError = priceValue == null || priceValue <= 0
                    categoryError = category == null
                    imageError = photo == null

                    if (!titleError && !priceError && category != null && photo != null && priceValue != null) {
                        isSubmitting = true
                        scope.launch {
                            val result = itemRepo.postItem(
                                title = title.trim(),
                                description = description.trim(),
                                price = priceValue,
                                category = category.name,
                                photoUri = Uri.fromFile(photo).toString()
                            )
                            isSubmitting = false
                            result.onSuccess {
                                title = ""
                                description = ""
                                price = ""
                                selectedCategory = null
                                photoFile = null
                                ImageUtils.clearCachedPhotos(context)
                                snackbarHostState.showSnackbar("Item listed successfully!")
                            }.onFailure { e ->
                                snackbarHostState.showSnackbar(e.userMessage())
                            }
                        }
                    }
                },
                isLoading = isSubmitting,
                enabled = !isPreparingPhoto,
                modifier = Modifier.fillMaxWidth()
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .padding(horizontal = 24.dp),
            snackbar = { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(50)
                )
            }
        )
    }
}
