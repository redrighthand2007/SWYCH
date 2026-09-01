package com.kush.swych.ui.browse

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kush.swych.core.data.AuthRepository
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
    initialCategory: String = ""
) {
    val context = LocalContext.current
    val itemRepo = remember { ItemRepository(context) }
    val authRepo = remember { AuthRepository(context) }
    
    // Initialize with cached items to avoid null state when popping back stack, which would reset scroll state
    var items by remember { mutableStateOf<List<Item>?>(ItemRepository.cachedItems) }
    var users by remember { mutableStateOf<Map<String, User>>(AuthRepository.cachedUsers?.associateBy { it.uid } ?: emptyMap()) }
    
    val initialUid = authRepo.currentUserUid
    var currentUser by remember { mutableStateOf<User?>(if (initialUid != null) users[initialUid] else null) }
    
    // Saveable state for filters
    var selectedCategory by rememberSaveable { mutableStateOf(if (initialCategory.isBlank()) "All" else initialCategory) }
    var selectedSortName by rememberSaveable { mutableStateOf(SortOption.RECENT.name) }
    var locationFilterName by rememberSaveable { mutableStateOf(LocationFilter.CAMPUS.name) }
    
    val selectedSort = SortOption.valueOf(selectedSortName)
    val locationFilter = LocationFilter.valueOf(locationFilterName)

    // We store "applied" items in memory for prototype
    var appliedItemIds by remember { mutableStateOf(setOf<String>()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val currentUid = authRepo.currentUserUid
        if (currentUid != null) {
            val userResult = authRepo.getAllUsers()
            val userList = userResult.getOrNull() ?: emptyList()
            users = userList.associateBy { it.uid }
            currentUser = users[currentUid]
        }
        
        val result = itemRepo.getAllItems()
        items = result.getOrNull() ?: emptyList()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        
        // Header
        Text(
            text = "Browse Items",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        // Categories
        val categories = listOf("All") + Category.values().map { it.name }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                val bgColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                val textColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(bgColor)
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat.lowercase().replaceFirstChar { it.uppercase() },
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filters (Campus/Hostel and Sort)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Location Filter
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                LocationFilter.values().forEach { filter ->
                    val isSelected = locationFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { locationFilterName = filter.name }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
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

            // Sort Filter
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SortOption.values().forEach { filter ->
                    val isSelected = selectedSort == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedSortName = filter.name }
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

        Spacer(modifier = Modifier.height(16.dp))

        // Grid
        if (items == null) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(6) {
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(0.85f).clip(RoundedCornerShape(16.dp)).shimmerEffect())
                }
            }
        } else {
            var filteredItems = if (selectedCategory == "All") items!! else items!!.filter { it.category == selectedCategory }
            
            // Location filtering
            if (locationFilter == LocationFilter.HOSTEL && currentUser != null) {
                filteredItems = filteredItems.filter { item ->
                    val itemSellerBlock = users[item.sellerId]?.block ?: ""
                    itemSellerBlock == currentUser!!.block
                }
            }

            // Sorting
            filteredItems = when (selectedSort) {
                SortOption.RECENT -> filteredItems // Assume order fetched is recent
                SortOption.LOW_TO_HIGH -> filteredItems.sortedBy { it.price }
            }

            if (filteredItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No items found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = filteredItems,
                        key = { it.id } // Use item ID as key to help Compose preserve scroll position across recompositions!
                    ) { item ->
                        val sellerBlock = users[item.sellerId]?.block ?: "Unknown"
                        val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
                        ItemCard(
                            item = item,
                            sellerBlock = sellerBlock,
                            isOwnItem = item.sellerId == currentUser?.uid,
                            isApplied = appliedItemIds.contains(item.id),
                            onClick = { navController.navigate(ItemDetailRoute(item.id)) },
                            onDealClick = {
                                appliedItemIds = appliedItemIds + item.id
                            },
                            onRemoveClick = {
                                coroutineScope.launch {
                                    val res = itemRepo.deleteItem(item.id)
                                    if (res.isSuccess) {
                                        items = items?.filter { it.id != item.id }
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

