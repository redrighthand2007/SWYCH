package com.kush.swych.ui.itemdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kush.swych.core.data.AuthRepository
import com.kush.swych.core.data.DealRepository
import com.kush.swych.core.data.ItemRepository
import com.kush.swych.core.model.Item
import com.kush.swych.core.util.userMessage
import com.kush.swych.ui.navigation.ItemDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val itemRepository: ItemRepository,
    private val dealRepository: DealRepository
) : ViewModel() {

    private val itemId = savedStateHandle.toRoute<ItemDetailRoute>().itemId

    var item by mutableStateOf<Item?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set

    /** The buyer already has a pending or accepted offer on this item. */
    var offerSubmitted by mutableStateOf(false)
        private set
    var offerAmount by mutableStateOf("")
        private set
    var isSubmittingOffer by mutableStateOf(false)
        private set

    /** Editing or removing the user's own listing. */
    var isWorking by mutableStateOf(false)
        private set

    /** Set once the listing is removed; the screen then goes back. */
    var isRemoved by mutableStateOf(false)
        private set

    /** One-off message for a snackbar; the screen clears it after showing. */
    var message by mutableStateOf<String?>(null)

    val currentUid: String? get() = authRepository.currentUserUid

    init {
        load()
    }

    // Item is always fetched fresh; an existing live offer hides the offer form
    private fun load() {
        viewModelScope.launch {
            item = itemRepository.getItemById(itemId).getOrNull()
            val uid = currentUid
            offerSubmitted = dealRepository.getMyDeals().getOrNull().orEmpty()
                .any { it.itemId == itemId && it.buyerId == uid && it.isLive }
            isLoading = false
        }
    }

    fun onOfferAmountChange(value: String) {
        offerAmount = value.filter { it.isDigit() || it == '.' }
    }

    fun submitOffer() {
        val current = item ?: return
        if (isSubmittingOffer) return
        val price = offerAmount.toDoubleOrNull()
        if (price == null || price <= 0) {
            message = "Enter a valid offer amount"
            return
        }
        isSubmittingOffer = true
        viewModelScope.launch {
            dealRepository.createDeal(
                itemId = current.id,
                itemTitle = current.title,
                itemPhotoUrl = current.photoUrl ?: "",
                sellerId = current.sellerId,
                agreedPrice = price
            )
                .onSuccess { offerSubmitted = true }
                .onFailure { message = it.userMessage() }
            isSubmittingOffer = false
        }
    }

    fun updateListing(title: String, description: String, price: Double) {
        val current = item ?: return
        isWorking = true
        viewModelScope.launch {
            itemRepository.updateItem(current.id, title, description, price)
                .onSuccess {
                    item = itemRepository.getItemById(current.id).getOrNull() ?: item
                    message = "Listing updated."
                }
                .onFailure { message = it.userMessage() }
            isWorking = false
        }
    }

    fun removeListing() {
        val current = item ?: return
        isWorking = true
        viewModelScope.launch {
            itemRepository.deleteItem(current.id)
                .onSuccess { isRemoved = true }
                .onFailure { message = it.userMessage() }
            isWorking = false
        }
    }
}
