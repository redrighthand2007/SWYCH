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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.kush.swych.core.designsystem.component.DealButton
import com.kush.swych.core.model.Category
import com.kush.swych.core.util.ImageUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostItemScreen(viewModel: PostItemViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var description by viewModel::description
    val title = viewModel.title
    val price = viewModel.price
    val selectedCategory = viewModel.category
    val photoFile = viewModel.photoFile
    val isPreparingPhoto = viewModel.isPreparingPhoto
    val isSubmitting = viewModel.isSubmitting
    val titleError = viewModel.titleError
    val priceError = viewModel.priceError
    val categoryError = viewModel.categoryError
    val imageError = viewModel.imageError
    var categoryExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.message) {
        viewModel.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.message = null
        }
    }

    // Camera writes a full-size photo into a file we own. The path is saved so the
    // photo still arrives if Android closes the app while the camera is open.
    var cameraTargetPath by rememberSaveable { mutableStateOf<String?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val file = cameraTargetPath?.let { File(it) }
        cameraTargetPath = null
        if (file == null) return@rememberLauncherForActivityResult
        if (saved) viewModel.usePhoto(Uri.fromFile(file)) else file.delete()
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.usePhoto(uri)
    }

    fun openCamera() {
        val (file, uri) = ImageUtils.newCameraTarget(context)
        cameraTargetPath = file.absolutePath
        try {
            cameraLauncher.launch(uri)
        } catch (_: ActivityNotFoundException) {
            cameraTargetPath = null
            viewModel.message = "No camera app found. Choose a photo from your gallery instead."
        }
    }

    fun openGallery() {
        try {
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } catch (_: ActivityNotFoundException) {
            viewModel.message = "No gallery app found."
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
                onValueChange = viewModel::onTitleChange,
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
                                viewModel.onCategoryChange(cat)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Price
            OutlinedTextField(
                value = price,
                onValueChange = viewModel::onPriceChange,
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
                onClick = viewModel::submit,
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
