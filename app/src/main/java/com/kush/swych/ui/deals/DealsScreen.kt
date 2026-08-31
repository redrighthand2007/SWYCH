package com.kush.swych.ui.deals

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

@Composable
fun DealsScreen(navController: NavController, onNavigateToMainTab: (Int) -> Unit) {
    val context = LocalContext.current
    val dealRepo = remember { DealRepository(context) }
    val authRepo = remember { AuthRepository(context) }
    val currentUid = authRepo.currentUserUid

    var deals by remember { mutableStateOf<List<Deal>?>(null) }

    LaunchedEffect(Unit) {
        val result = dealRepo.getMyDeals()
        deals = result.getOrElse { emptyList() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            "My Deals",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Deals you are buying or selling",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        when {
            deals == null -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
            deals!!.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🤝", fontSize = 48.sp)
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
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(deals!!) { deal ->
                        DealCard(deal = deal, isSeller = deal.sellerId == currentUid)
                    }
                }
            }
        }
    }
}

@Composable
private fun DealCard(deal: Deal, isSeller: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            AsyncImage(
                model = deal.itemPhotoUrl.ifBlank { null },
                contentDescription = deal.itemTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(90.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        deal.itemTitle,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "₹${deal.finalPrice.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        if (isSeller) "You are selling" else "You are buying",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DealStatusChip(status = deal.status)
                }
            }
        }
    }
}

@Composable
private fun DealStatusChip(status: String) {
    val (bg, fg, label) = when (status.uppercase()) {
        "LOCKED" -> Triple(Color(0xFFF57F17).copy(alpha = 0.15f), Color(0xFFFFB300), "Locked 🔒")
        "COMPLETED" -> Triple(Color(0xFF1B5E20).copy(alpha = 0.15f), Color(0xFF4CAF50), "Completed ✅")
        "EXPIRED" -> Triple(Color.Gray.copy(alpha = 0.15f), Color.Gray, "Expired")
        else -> Triple(Color.Gray.copy(alpha = 0.15f), Color.Gray, status.replaceFirstChar { it.uppercase() })
    }
    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
