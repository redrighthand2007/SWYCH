package com.kush.swych.ui.browse

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kush.swych.core.designsystem.component.ItemCard
import com.kush.swych.core.designsystem.component.shimmerEffect
import com.kush.swych.core.model.Category
import com.kush.swych.ui.navigation.ItemDetailRoute

@Composable
fun BrowseContent(navController: NavController, viewModel: BrowseViewModel) {
    val context = LocalContext.current
    val items = viewModel.items
    val users = viewModel.users
    val myUid = viewModel.myUid
    val appliedItemIds = viewModel.appliedItemIds
    val sendingOfferIds = viewModel.sendingOfferIds
    val category = viewModel.category
    val selectedSort = viewModel.sort
    val locationFilter = viewModel.location

    // Refresh in the background each time the tab is shown; cached items stay on screen
    LaunchedEffect(Unit) { viewModel.load() }

    LaunchedEffect(viewModel.message) {
        viewModel.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.message = null
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {

        // --- 1. Categories pill ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            val cats = listOf("All") + Category.entries.map { it.name }
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(cats) { catName ->
                    val isSelected = if (catName == "All") category.isBlank() || category.equals("All", ignoreCase = true) else category.equals(catName, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { viewModel.selectCategory(if (catName == "All") "" else catName) }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val color by animateColorAsState(targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                        val displayCat = if (catName == "All") "All" else Category.fromName(catName)?.displayName ?: catName
                        Text(
                            text = displayCat,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = color
                        )
                    }
                }
            }
        }

        // --- 2. 50-50 Filters ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Location Segmented Control (50%)
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                val tabWidth = maxWidth / 2
                val selectedIndex = LocationFilter.entries.indexOf(locationFilter)
                val indicatorOffset by animateDpAsState(
                    targetValue = if (selectedIndex == 0) 0.dp else tabWidth,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                )

                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Row(modifier = Modifier.fillMaxSize()) {
                    LocationFilter.entries.forEachIndexed { index, filter ->
                        val isSelected = selectedIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) { viewModel.selectLocation(filter) },
                            contentAlignment = Alignment.Center
                        ) {
                            val color by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = filter.label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = color)
                        }
                    }
                }
            }

            // Sort Segmented Control (50%)
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                val tabWidth = maxWidth / 2
                val selectedIndex = SortOption.entries.indexOf(selectedSort)
                val indicatorOffset by animateDpAsState(
                    targetValue = if (selectedIndex == 0) 0.dp else tabWidth,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                )

                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Row(modifier = Modifier.fillMaxSize()) {
                    SortOption.entries.forEachIndexed { index, filter ->
                        val isSelected = selectedIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) { viewModel.selectSort(filter) },
                            contentAlignment = Alignment.Center
                        ) {
                            val color by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = filter.label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = color)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid with Pull-to-Refresh
        @OptIn(ExperimentalMaterial3Api::class)
        PullToRefreshBox(
            isRefreshing = viewModel.isRefreshing,
            onRefresh = { viewModel.load(forceRefresh = true) },
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
                val filteredItems = viewModel.visibleItems
                if (filteredItems.isEmpty()) {
                    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize().defaultMinSize(minHeight = 400.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = viewModel.fetchError ?: "No items found.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
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
                            val seller = users[item.sellerId]

                            ItemCard(
                                item = item,
                                sellerName = sellerName,
                                sellerVerified = users[item.sellerId]?.vitVerified == true,
                                sellerBlock = sellerBlock,
                                upvotes = seller?.upvotes ?: 0,
                                downvotes = seller?.downvotes ?: 0,
                                myVote = viewModel.myVotes[item.id] ?: 0,
                                onVote = if (item.sellerId == myUid) null else { tapped -> viewModel.vote(item, tapped) },
                                isOwnItem = item.sellerId == myUid,
                                isApplied = item.id in appliedItemIds || item.id in sendingOfferIds,
                                onClick = { navController.navigate(ItemDetailRoute(item.id)) },
                                onDealClick = { viewModel.pendingOffer = item }
                            )
                        }
                    }
                }
            }
        }
    }

    viewModel.pendingOffer?.let { item ->
        AlertDialog(
            onDismissRequest = { viewModel.pendingOffer = null },
            title = { Text("Send offer?") },
            text = { Text("Offer ₹%.0f for %s? The seller will see your offer and can accept or reject it.".format(item.price, item.title)) },
            confirmButton = {
                TextButton(onClick = { viewModel.sendOffer(item) }) { Text("Send offer") }
            },
            dismissButton = { TextButton(onClick = { viewModel.pendingOffer = null }) { Text("Cancel") } }
        )
    }
}
