package com.kush.swych.ui.deals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.kush.swych.core.data.AuthRepository
import com.kush.swych.core.data.DealRepository
import com.kush.swych.core.designsystem.component.shimmerEffect
import com.kush.swych.core.model.Deal

private enum class DealFilter(val label: String) {
    BUYING("Buying"),
    SELLING("Selling")
}

@Composable
fun DealsScreen(navController: NavController, onNavigateToMainTab: (Int) -> Unit) {
    val context = LocalContext.current
    val dealRepo = remember { DealRepository(context) }
    val authRepo = remember { AuthRepository(context) }
    val currentUid = authRepo.currentUserUid

    var deals by remember { mutableStateOf<List<Deal>?>(null) }
    var selectedFilter by remember { mutableStateOf(DealFilter.BUYING) }

    LaunchedEffect(Unit) {
        val result = dealRepo.getMyDeals()
        // Sort in recent manner
        deals = result.getOrElse { emptyList() }.reversed()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Deals",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        // Filter Toggle
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            DealFilter.values().forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
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

        Spacer(modifier = Modifier.height(16.dp))

        when {
            deals == null -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 0.dp)
                ) {
                    items(4) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .shimmerEffect()
                        )
                    }
                }
            }
            else -> {
                val filteredDeals = deals!!.filter { deal ->
                    if (selectedFilter == DealFilter.SELLING) deal.sellerId == currentUid
                    else deal.buyerId == currentUid || deal.sellerId != currentUid // fallback if buyerId logic needs adjust
                }

                if (filteredDeals.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("??", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("No deals yet", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Browse items and make your first offer!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 0.dp)
                    ) {
                        items(filteredDeals) { deal ->
                            DealCard(deal = deal, isSeller = selectedFilter == DealFilter.SELLING)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DealCard(deal: Deal, isSeller: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (deal.itemPhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model = deal.itemPhotoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("??", fontSize = 32.sp)
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deal.itemTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Offer: ?%.0f".format(deal.finalPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                
                val statusColor = when(deal.status) {
                    "ACCEPTED" -> Color(0xFF4CAF50) // Green
                    "REJECTED" -> Color(0xFFF44336) // Red
                    else -> MaterialTheme.colorScheme.onSurfaceVariant // Pending
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = deal.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

