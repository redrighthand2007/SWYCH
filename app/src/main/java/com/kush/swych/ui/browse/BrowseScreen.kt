package com.kush.swych.ui.browse

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.kush.swych.core.data.AuthRepository
import com.kush.swych.core.data.ItemRepository
import com.kush.swych.core.designsystem.component.CategoryChip
import com.kush.swych.core.designsystem.component.shimmerEffect
import com.kush.swych.core.model.Category
import com.kush.swych.core.model.Item
import com.kush.swych.ui.navigation.ItemDetailRoute

// ????????? Sort Options ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

private enum class SortOption(val label: String) {
    RECENT("Recently Added"),
    LOW_TO_HIGH("Low to High"),
    HIGH_TO_LOW("High to Low")
}

// ????????? BrowseContent (displayed inside MainScreen) ???????????????????????????????????????????????????????????????????????????????????????

@Composable
fun BrowseContent(
    navController: NavController,
    initialCategory: String = ""
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val authRepo = remember(context) { AuthRepository(context) }
    val contextItem = androidx.compose.ui.platform.LocalContext.current
    val itemRepo = remember(contextItem) { ItemRepository(contextItem) }

    var selectedCategory by remember { mutableStateOf<Category?>(
        if (initialCategory.isNotEmpty()) Category.entries.find { it.displayName == initialCategory } else null
    ) }
    var selectedSort by remember { mutableStateOf(SortOption.RECENT) }
    var nearMeEnabled by remember { mutableStateOf(false) }
    var allItems by remember { mutableStateOf<List<Item>?>(null) }
    var currentUserBlock by remember { mutableStateOf("") }

    // Update category when initialCategory changes from Home screen
    LaunchedEffect(initialCategory) {
        if (initialCategory.isNotEmpty()) {
            selectedCategory = Category.entries.find { it.displayName == initialCategory }
        }
    }

    LaunchedEffect(Unit) {
        val result = itemRepo.getAllItems()
        allItems = result.getOrNull() ?: emptyList()
        currentUserBlock = authRepo.getCurrentUserProfile().getOrNull()?.block ?: ""
    }

    // Filter and sort
    val displayItems = remember(selectedCategory, allItems, selectedSort, nearMeEnabled, currentUserBlock) {
        val items = allItems ?: return@remember null
        var filtered = if (selectedCategory == null) items
        else items.filter { it.category == selectedCategory!!.displayName }

        when (selectedSort) {
            SortOption.RECENT -> filtered
            SortOption.LOW_TO_HIGH -> filtered.sortedBy { it.price }
            SortOption.HIGH_TO_LOW -> filtered.sortedByDescending { it.price }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ?????? Category horizontal chips ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                CategoryChip(
                    label = "All",
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null }
                )
            }
            items(Category.entries.toList()) { category ->
                CategoryChip(
                    label = category.displayName,
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category }
                )
            }
        }

        // ?????? Campus / Hostel Block Toggle ??????????????????????????????????????????????????????????????????????????????????????????????????????
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            NearMeToggle(
                nearMeEnabled = nearMeEnabled,
                onToggle = { nearMeEnabled = it }
            )
        }

        // ?????? Sort Options ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(SortOption.entries.toList()) { option ->
                SortChip(
                    label = option.label,
                    selected = selectedSort == option,
                    onClick = { selectedSort = option }
                )
            }
        }

        // ?????? Item List ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (displayItems == null) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(6) {
                    ShimmerBrowseItemRow()
                }
            }
        } else if (displayItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📭", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No items found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = displayItems,
                    key = { it.id }
                ) { item ->
                    BrowseItemRow(
                        item = item,
                        onClick = {
                            navController.navigate(ItemDetailRoute(item.id))
                        }
                    )
                }
            }
        }
    }
}

// ????????? Item Row Card ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

@Composable
private fun BrowseItemRow(
    item: Item,
    onClick: () -> Unit
) {
    var hasApplied by androidx.compose.runtime.remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // 25% Photo
            AsyncImage(
                model = item.photoUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.25f)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )

            // 75% Info
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "???${item.price.toInt()}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Seller",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    // Status badge or Deal Action
                    if (item.status.lowercase() == "open") {
                        if (hasApplied) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Applied ???",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { hasApplied = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Deal",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    } else {
                        StatusBadge(status = item.status)
                    }
                }
            }
        }
    }
}

// ????????? Status Badge ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

@Composable
private fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        "open" -> Triple(
            Color(0xFF1B5E20).copy(alpha = 0.15f),
            Color(0xFF4CAF50),
            "Open"
        )
        "locked" -> Triple(
            Color(0xFFF57F17).copy(alpha = 0.15f),
            Color(0xFFFFB300),
            "Locked"
        )
        "sold" -> Triple(
            Color.Gray.copy(alpha = 0.15f),
            Color.Gray,
            "Sold"
        )
        else -> Triple(
            Color.Gray.copy(alpha = 0.15f),
            Color.Gray,
            status.replaceFirstChar { it.uppercase() }
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

// ????????? Near Me Toggle ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

@Composable
private fun NearMeToggle(
    nearMeEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val options = listOf("Campus", "Hostel Block")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                val isSelected = if (nearMeEnabled) index == 1 else index == 0

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                    else Color.Transparent,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "toggle_bg_$index"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "toggle_text_$index"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(46.dp))
                        .background(bgColor)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onToggle(index == 1) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = textColor
                    )
                }
            }
        }
    }
}

// ????????? Sort Chip ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

@Composable
private fun SortChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "sort_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "sort_content"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else Color.Transparent,
        label = "sort_border"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}

@Composable
fun ShimmerBrowseItemRow() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .shimmerEffect(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.25f)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Box(modifier = Modifier.height(16.dp).fillMaxWidth(0.7f).shimmerEffect().background(Color.Gray.copy(alpha = 0.3f)))
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.height(14.dp).fillMaxWidth(0.3f).shimmerEffect().background(Color.Gray.copy(alpha = 0.3f)))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.height(12.dp).fillMaxWidth(0.5f).shimmerEffect().background(Color.Gray.copy(alpha = 0.3f)))
                    }
                    Box(modifier = Modifier.height(24.dp).width(60.dp).clip(RoundedCornerShape(6.dp)).shimmerEffect().background(Color.Gray.copy(alpha = 0.3f)))
                }
            }
        }
    }
}
