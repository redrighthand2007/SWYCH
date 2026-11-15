package com.kush.swych.ui.deals

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kush.swych.core.data.AuthRepository
import com.kush.swych.core.data.DealRepository
import com.kush.swych.core.data.ItemRepository
import com.kush.swych.core.model.Deal
import com.kush.swych.core.model.Item
import com.kush.swych.core.model.User
import com.kush.swych.core.util.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/** The Deals tab: the user's offers as a buyer, and their listings and incoming offers as a seller. */
@HiltViewModel
class DealsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val itemRepository: ItemRepository,
    private val dealRepository: DealRepository
) : ViewModel() {

    /** Null until the first load finishes. */
    var deals by mutableStateOf<List<Deal>?>(null)
        private set
    var myItems by mutableStateOf<List<Item>>(emptyList())
        private set
    var users by mutableStateOf<Map<String, User>>(emptyMap())
        private set
    var loadError by mutableStateOf<String?>(null)
        private set
    var isRefreshing by mutableStateOf(false)
        private set

    /** 0 = Buy (my offers), 1 = Sell (my listings and the offers on them). */
    var selectedTab by mutableIntStateOf(0)

    /** How both tabs are ordered. */
    var sortMode by mutableStateOf(DealSort.LATEST)

    /** Deals with a request in flight; their buttons are disabled so nothing is sent twice. */
    var busyIds by mutableStateOf<Set<String>>(emptySet())
        private set

    /** One-off message for a snackbar; the screen clears it after showing. */
    var message by mutableStateOf<String?>(null)

    /** Phone number to open in the dialer; the screen clears it after opening. */
    var numberToDial by mutableStateOf<String?>(null)

    val currentUid: String? get() = authRepository.currentUserUid

    /** Always fetches fresh data, so an offer behind the red badge is visible right away. */
    fun load(showRefreshing: Boolean = false) {
        if (showRefreshing) isRefreshing = true
        viewModelScope.launch {
            dealRepository.getMyDeals()
                .onSuccess { deals = it; loadError = null }
                .onFailure { loadError = it.userMessage() }
            itemRepository.getMyItems().onSuccess { myItems = it }
            users = authRepository.getUsers(deals.orEmpty().flatMap { listOf(it.buyerId, it.sellerId) }.toSet())
            isRefreshing = false
        }
    }

    fun acceptDeal(id: String) = runAction(id, "Offer accepted. Call the buyer to meet up.") { dealRepository.acceptDeal(id) }
    fun rejectDeal(id: String) = runAction(id, "Offer rejected.") { dealRepository.rejectDeal(id) }
    fun withdrawOffer(id: String) = runAction(id, "Offer withdrawn.") { dealRepository.cancelDeal(id) }
    fun confirmHandover(id: String) = runAction(id, "Marked as done.") { dealRepository.confirmDeal(id) }
    fun cancelDeal(id: String) = runAction(id, "Deal cancelled.") { dealRepository.cancelDeal(id) }
    fun dismissDeal(id: String) = runAction(id, null) { dealRepository.dismissDeal(id) }
    fun removeListing(id: String) = runAction(id, "Listing removed.") { itemRepository.deleteItem(id) }

    fun callOtherSide(deal: Deal) {
        viewModelScope.launch {
            val phone = dealRepository.getContactPhone(deal.id).getOrNull()
            if (phone.isNullOrBlank()) {
                message = "Phone number isn't available for this deal."
            } else {
                numberToDial = phone
            }
        }
    }

    private fun runAction(id: String, successMessage: String?, action: suspend () -> Result<Unit>) {
        if (id in busyIds) return
        busyIds = busyIds + id
        viewModelScope.launch {
            val result = action()
            busyIds = busyIds - id
            result.onSuccess { if (successMessage != null) message = successMessage }
                .onFailure { message = it.userMessage() }
            load()
        }
    }
}

enum class DealSort(val label: String) {
    /** Newest offer or listing first. */
    LATEST("Latest"),
    /** Grouped by product name, A to Z; newest first within a product. */
    PRODUCT("Product")
}
