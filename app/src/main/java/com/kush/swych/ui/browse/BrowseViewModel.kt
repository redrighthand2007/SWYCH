package com.kush.swych.ui.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kush.swych.core.data.AuthRepository
import com.kush.swych.core.data.DealRepository
import com.kush.swych.core.data.ItemRepository
import com.kush.swych.core.model.Item
import com.kush.swych.core.model.ItemStatus
import com.kush.swych.core.model.User
import com.kush.swych.core.util.nextVote
import com.kush.swych.core.util.userMessage
import com.kush.swych.core.util.withVoteChange
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

enum class SortOption(val label: String) {
    RECENT("Recents"),
    LOW_TO_HIGH("Low -> High")
}

enum class LocationFilter(val label: String) {
    CAMPUS("Campus"),
    HOSTEL("Hostel")
}

/**
 * Marketplace feed. Lives as long as the main screen, so switching tabs keeps the loaded
 * items, filters and scroll-worthy state instead of reloading from scratch.
 */
@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val itemRepository: ItemRepository,
    private val dealRepository: DealRepository
) : ViewModel() {

    /** Null until the first load finishes (the screen shows a shimmer). */
    var items by mutableStateOf<List<Item>?>(null)
        private set
    var users by mutableStateOf<Map<String, User>>(emptyMap())
        private set
    var currentUser by mutableStateOf<User?>(null)
        private set
    var appliedItemIds by mutableStateOf<Set<String>>(emptySet())
        private set
    var sendingOfferIds by mutableStateOf<Set<String>>(emptySet())
        private set
    var fetchError by mutableStateOf<String?>(null)
        private set
    var isRefreshing by mutableStateOf(false)
        private set

    /** Enum name of the selected category, or blank for all. */
    var category by mutableStateOf("")
        private set
    var sort by mutableStateOf(SortOption.RECENT)
        private set
    var location by mutableStateOf(LocationFilter.CAMPUS)
        private set

    /** Item id -> this user's vote (1 or -1). */
    var myVotes by mutableStateOf<Map<String, Int>>(emptyMap())
        private set
    private var votingIds = emptySet<String>()

    /** Item waiting for the "send offer?" confirmation. */
    var pendingOffer by mutableStateOf<Item?>(null)

    /** One-off message for a toast; the screen clears it after showing. */
    var message by mutableStateOf<String?>(null)

    val myUid: String? get() = authRepository.currentUserUid

    fun selectCategory(value: String) {
        category = value
    }

    fun selectSort(value: SortOption) {
        sort = value
    }

    fun selectLocation(value: LocationFilter) {
        location = value
    }

    /** Open items from other sellers, after the category, hostel and sort filters. */
    val visibleItems: List<Item>
        get() = filterItems(items.orEmpty(), category, location, sort, myUid, currentUser, users)

    fun load(forceRefresh: Boolean = false) {
        if (forceRefresh) isRefreshing = true
        viewModelScope.launch {
            authRepository.getCurrentUserProfile().onSuccess { currentUser = it }

            val uid = myUid
            dealRepository.getMyDeals().onSuccess { myDeals ->
                appliedItemIds = myDeals.filter { it.buyerId == uid && it.isLive }.map { it.itemId }.toSet()
            }

            itemRepository.getMyVotes().onSuccess { myVotes = it }

            val result = itemRepository.getAllItems(forceRefresh = forceRefresh)
            fetchError = result.exceptionOrNull()?.userMessage()
            items = result.getOrNull() ?: items ?: emptyList()

            // Refetches users when a seller is missing, e.g. someone who just signed up
            // Always fresh, so vote totals from other students show up
            users = authRepository.getUsers(items.orEmpty().map { it.sellerId }.toSet(), forceRefresh = true)
            isRefreshing = false
        }
    }

    /** Shows the vote straight away, and undoes it if the server refuses. */
    fun vote(item: Item, tapped: Int) {
        if (item.sellerId == myUid || item.id in votingIds) return
        val old = myVotes[item.id] ?: 0
        val new = nextVote(old, tapped)
        applyVote(item, old, new)
        votingIds = votingIds + item.id
        viewModelScope.launch {
            itemRepository.vote(item.id, new).onFailure {
                applyVote(item, new, old)
                message = it.userMessage()
            }
            votingIds = votingIds - item.id
        }
    }

    private fun applyVote(item: Item, old: Int, new: Int) {
        myVotes = if (new == 0) myVotes - item.id else myVotes + (item.id to new)
        users[item.sellerId]?.let { users = users + (item.sellerId to it.withVoteChange(old, new)) }
    }

    fun sendOffer(item: Item) {
        pendingOffer = null
        if (item.id in sendingOfferIds) return
        sendingOfferIds = sendingOfferIds + item.id
        viewModelScope.launch {
            dealRepository.createDeal(
                itemId = item.id,
                itemTitle = item.title,
                itemPhotoUrl = item.photoUrl ?: "",
                sellerId = item.sellerId,
                agreedPrice = item.price
            )
                .onSuccess { appliedItemIds = appliedItemIds + item.id }
                .onFailure { message = it.userMessage() }
            sendingOfferIds = sendingOfferIds - item.id
            load(forceRefresh = true)
        }
    }

    companion object {
        fun filterItems(
            items: List<Item>,
            category: String,
            location: LocationFilter,
            sort: SortOption,
            myUid: String?,
            currentUser: User?,
            users: Map<String, User>
        ): List<Item> {
            var result = items.filter { it.status == ItemStatus.OPEN && it.sellerId != myUid }
            if (category.isNotBlank()) {
                result = result.filter { it.category.equals(category, ignoreCase = true) }
            }
            if (location == LocationFilter.HOSTEL && currentUser != null) {
                result = result.filter { users[it.sellerId]?.block == currentUser.block }
            }
            return when (sort) {
                SortOption.RECENT -> result // already newest first
                SortOption.LOW_TO_HIGH -> result.sortedBy { it.price }
            }
        }
    }
}
