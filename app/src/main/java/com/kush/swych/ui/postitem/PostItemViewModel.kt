package com.kush.swych.ui.postitem

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kush.swych.core.data.ItemRepository
import com.kush.swych.core.model.Category
import com.kush.swych.core.util.ImageUtils
import com.kush.swych.core.util.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * New-listing form. Scoped to the main screen, so a half-written listing survives
 * switching tabs and rotating the phone.
 */
@HiltViewModel
class PostItemViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val itemRepository: ItemRepository
) : ViewModel() {

    var title by mutableStateOf("")
        private set
    var description by mutableStateOf("")
    var price by mutableStateOf("")
        private set
    var category by mutableStateOf<Category?>(null)
        private set

    /** Downscaled JPEG ready to upload, shown as the preview. */
    var photoFile by mutableStateOf<File?>(null)
        private set
    var isPreparingPhoto by mutableStateOf(false)
        private set
    var isSubmitting by mutableStateOf(false)
        private set

    var titleError by mutableStateOf(false)
        private set
    var priceError by mutableStateOf(false)
        private set
    var categoryError by mutableStateOf(false)
        private set
    var imageError by mutableStateOf(false)
        private set

    /** One-off message for a snackbar; the screen clears it after showing. */
    var message by mutableStateOf<String?>(null)

    fun onTitleChange(value: String) {
        title = value.trimStart()
        titleError = false
    }

    fun onPriceChange(value: String) {
        price = value.filter { it.isDigit() }.take(6)
        priceError = false
    }

    fun onCategoryChange(value: Category) {
        category = value
        categoryError = false
    }

    /** Downscales a camera or gallery photo before it becomes the listing photo. */
    fun usePhoto(source: Uri) {
        isPreparingPhoto = true
        viewModelScope.launch {
            val prepared = ImageUtils.prepareForUpload(context, source)
            isPreparingPhoto = false
            if (prepared != null) {
                photoFile = prepared
                imageError = false
            } else {
                message = "Couldn't read that photo. Try another one."
            }
        }
    }

    fun submit() {
        if (isSubmitting || isPreparingPhoto) return
        val priceValue = price.toDoubleOrNull()
        val selectedCategory = category
        val photo = photoFile
        titleError = title.isBlank()
        priceError = priceValue == null || priceValue <= 0
        categoryError = selectedCategory == null
        imageError = photo == null
        if (titleError || priceError || selectedCategory == null || photo == null || priceValue == null) return

        isSubmitting = true
        viewModelScope.launch {
            itemRepository.postItem(
                title = title.trim(),
                description = description.trim(),
                price = priceValue,
                category = selectedCategory.name,
                photoUri = Uri.fromFile(photo).toString()
            )
                .onSuccess {
                    title = ""
                    description = ""
                    price = ""
                    category = null
                    photoFile = null
                    ImageUtils.clearCachedPhotos(context)
                    message = "Item listed successfully!"
                }
                .onFailure { message = it.userMessage() }
            isSubmitting = false
        }
    }
}
