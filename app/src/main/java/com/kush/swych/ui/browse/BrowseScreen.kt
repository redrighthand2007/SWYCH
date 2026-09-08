package com.kush.swych.ui.browse

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kush.swych.core.data.AuthRepository
import com.kush.swych.core.data.DealRepository
import com.kush.swych.core.data.ItemRepository
import com.kush.swych.core.designsystem.component.ItemCard
import com.kush.swych.core.designsystem.component.shimmerEffect
import com.kush.swych.core.model.Category
import com.kush.swych.core.model.Item
import com.kush.swych.core.model.User
import com.kush.swych.ui.navigation.ItemDetailRoute
import kotlinx.coroutines.launch

private enum class SortOption(val label: String) {
    RECENT("Recents"),
    LOW_TO_HIGH("Low -> High")
}

private enum class LocationFilter(val label: String) {
    CAMPUS("Campus"),
    HOSTEL("Hostel")
}

@Composable
fun BrowseContent(
    navController: NavController,
    category: String,
    onCategoryChange: (String) -> Unit,
    sortName: String,
    onSortChange: (String) -> Unit,
    locationName: String,
    onLocationChange: (String) -> Unit
) {
    val context = LocalContext.current
    val itemRepo = remember { ItemRepository(context) }
    val authRepo = remember { AuthRepository(context) }
    val dealRepo = remember { DealRepository(context) }
    
    var items by remember { mutableStateOf<List<Item>?>(ItemRepository.cachedItems) }
    var users by remember { mutableStateOf<Map<String, User>>(emptyMap()) }
    var allDeals by remember { mutableStateOf<List<com.kush.swych.core.model.Deal>>(emptyList()) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var appliedItemIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var fetchError by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val selectedSort = try { SortOption.valueOf(sortName) } catch (_: Exception) { SortOption.RECENT }
    val locationFilter = try { LocationFilter.valueOf(locationName) } catch (_: Exception) { LocationFilter.CAMPUS }

    // Centralized data loading function
    fun loadAllData(forceRefresh: Boolean = false) {
        scope.launch {
            try {
                val userResult = authRepo.getAllUsers(forceRefresh = forceRefresh)
                if (userResult.isSuccess) {
                    users = userResult.getOrNull()?.associateBy { it.uid } ?: emptyMap()
                }
                
                val currentUserResult = authRepo.getCurrentUserProfile()
                if (currentUserResult.isSuccess) {
                    currentUser = currentUserResult.getOrNull()
                }

                val dealsResult = dealRepo.getAllDeals(forceRefresh = forceRefresh)
                if (dealsResult.isSuccess) {
                    allDeals = dealsResult.getOrNull() ?: emptyList()
                    val myUid = currentUser?.uid
                    if (myUid != null) {
                        appliedItemIds = allDeals.filter { it.buyerId == myUid }.map { it.itemId }.toSet()
                    }
                }
                
                val result = itemRepo.getAllItems(forceRefresh = forceRefresh)
                if (result.isFailure) {
                    fetchError = result.exceptionOrNull()?.message ?: "Unknown error"
                } else {
                    fetchError = null
                }
                items = result.getOrNull() ?: items ?: emptyList()
            } finally {
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (items == null || items?.isEmpty() == true) {
            loadAllData()
        } else {
            // Still load fresh data in background but don't show loading state
            loadAllData()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        
        // Filters (All, Campus/Hostel, Sort)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // All Button
                val isAllSelected = category.isBlank() || category.equals("All", ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onCategoryChange("All") }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "All",
                        color = if (isAllSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Location Filter
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    LocationFilter.values().forEach { filter ->
                        val isSelected = locationFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onLocationChange(filter.name) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter.label,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Sort Filter
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SortOption.values().forEach { filter ->
                    val isSelected = selectedSort == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { onSortChange(filter.name) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter.label,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid with Pull-to-Refresh
        @OptIn(ExperimentalMaterial3Api::class)
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                loadAllData(forceRefresh = true)
            },
            modifier = Modifier.fillMaxSize()
        ) {
            val currentItems = items
            if (currentItems == null) {
                // Initial loading — shimmer
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(6) {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp)).shimmerEffect())
                    }
                }
            } else {
                val selectedCat = if (category.isBlank()) "All" else category
                var filteredItems = (if (selectedCat == "All") currentItems else currentItems.filter { it.category.equals(selectedCat, ignoreCase = true) }).filter { it.status != "SOLD" }
                
                // Location filtering
                if (locationFilter == LocationFilter.HOSTEL && currentUser != null) {
                    filteredItems = filteredItems.filter { item ->
                        val itemSellerBlock = users[item.sellerId]?.block ?: ""
                        itemSellerBlock == currentUser?.block
                    }
                }

                // Sorting
                filteredItems = when (selectedSort) {
                    SortOption.RECENT -> filteredItems.reversed()
                    SortOption.LOW_TO_HIGH -> filteredItems.sortedBy { it.price }
                }

                if (filteredItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = fetchError ?: "No items found.",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 120.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = filteredItems,
                            key = { it.id }
                        ) { item ->
                            val sellerBlock = users[item.sellerId]?.block ?: "Unknown"
                            val sellerName = users[item.sellerId]?.name ?: "Unknown"
                            val sellerDeals = allDeals.filter { it.sellerId == item.sellerId }
                            val dealsMade = sellerDeals.count { it.status == "SOLD" }
                            val dealsExpired = sellerDeals.count { it.status == "REJECTED" }
                            
                            ItemCard(
                                item = item,
                                sellerName = sellerName,
                                sellerBlock = sellerBlock,
                                dealsMade = dealsMade,
                                dealsExpired = dealsExpired,
                                isOwnItem = item.sellerId == currentUser?.uid,
                                isApplied = appliedItemIds.contains(item.id),
                                onClick = { navController.navigate(ItemDetailRoute(item.id)) },
                                onDealClick = {
                                    scope.launch {
                                        val result = dealRepo.createDeal(
                                            itemId = item.id,
                                            itemTitle = item.title,
                                            itemPhotoUrl = item.photoUrl ?: "",
                                            sellerId = item.sellerId,
                                            agreedPrice = item.price
                                        )
                                        if (result.isSuccess) {
                                            appliedItemIds = appliedItemIds + item.id
                                            // Refresh all data to reflect changes
                                            loadAllData(forceRefresh = true)
                                        }
                                    }
                                },
                                onRemoveClick = {
                                    scope.launch {
                                        val res = itemRepo.deleteItem(item.id)
                                        if (res.isSuccess) {
                                            // Refresh all data since deals may have been cleaned up too
                                            loadAllData(forceRefresh = true)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}




