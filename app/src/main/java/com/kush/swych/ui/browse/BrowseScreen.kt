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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kush.swych.core.data.ItemRepository
import com.kush.swych.core.designsystem.component.ItemCard
import com.kush.swych.core.designsystem.component.shimmerEffect
import com.kush.swych.core.model.Category
import com.kush.swych.core.model.Item
import com.kush.swych.ui.navigation.ItemDetailRoute
import kotlinx.coroutines.launch

private enum class SortOption(val label: String) {
    RECENT("Recently Added"),
    LOW_TO_HIGH("Low to High"),
    HIGH_TO_LOW("High to Low")
}

@Composable
fun BrowseContent(
    navController: NavController,
    initialCategory: String = ""
) {
    val context = LocalContext.current
    val itemRepo = remember { ItemRepository(context) }
    var items by remember { mutableStateOf<List<Item>?>(null) }
    
    var selectedCategory by remember { mutableStateOf(if (initialCategory.isBlank()) "All" else initialCategory) }
    var selectedSort by remember { mutableStateOf(SortOption.RECENT) }
    
    // We store "applied" items in memory for prototype
    var appliedItemIds by remember { mutableStateOf(setOf<String>()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
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

        // Grid
        if (items == null) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(6) {
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(0.7f).clip(RoundedCornerShape(16.dp)).shimmerEffect())
                }
            }
        } else {
            var filteredItems = if (selectedCategory == "All") items!! else items!!.filter { it.category == selectedCategory }
            filteredItems = when (selectedSort) {
                SortOption.RECENT -> filteredItems // Assume fetched by recent
                SortOption.LOW_TO_HIGH -> filteredItems.sortedBy { it.price }
                SortOption.HIGH_TO_LOW -> filteredItems.sortedByDescending { it.price }
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
                    items(filteredItems) { item ->
                        ItemCard(
                            item = item,
                            isApplied = appliedItemIds.contains(item.id),
                            onClick = { navController.navigate(ItemDetailRoute(item.id)) },
                            onDealClick = {
                                appliedItemIds = appliedItemIds + item.id
                                // In real app: create deal row in Supabase
                            }
                        )
                    }
                }
            }
        }
    }
}
