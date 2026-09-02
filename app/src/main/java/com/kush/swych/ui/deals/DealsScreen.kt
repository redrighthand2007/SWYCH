package com.kush.swych.ui.deals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.launch
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
import com.kush.swych.core.network.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest

@Composable
fun DealsScreen(navController: NavController, onNavigateToMainTab: (Int) -> Unit) {
    val context = LocalContext.current
    val dealRepo = remember { DealRepository(context) }
    val authRepo = remember { AuthRepository(context) }
    val currentUid = authRepo.currentUserUid
    val coroutineScope = rememberCoroutineScope()

    var deals by remember { mutableStateOf<List<Deal>?>(null) }

    LaunchedEffect(Unit) {
        val result = dealRepo.getMyDeals()
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

        when (val currentDeals = deals) {
            null -> {
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
                val buyingDeals = currentDeals.filter { it.buyerId == currentUid }
                val sellingDeals = currentDeals.filter { it.sellerId == currentUid }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp)
                ) {
                    // Section 1: Buying (Locked In)
                    item {
                        Text(
                            text = "Buying (Locked In)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    if (buyingDeals.isEmpty()) {
                        item {
                            Text(
                                text = "No buying deals yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                        }
                    } else {
                        items(buyingDeals) { deal ->
                            DealCard(
                                deal = deal,
                                buttonText = "Cancel Deal",
                                onActionClick = {
                                    coroutineScope.launch {
                                        try {
                                            SupabaseManager.client.postgrest["deals"].delete {
                                                filter { eq("id", deal.id) }
                                            }
                                            deals = deals?.filter { it.id != deal.id }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        item { Spacer(modifier = Modifier.height(12.dp)) }
                    }

                    // Section 2: Selling (My Listings)
                    item {
                        Text(
                            text = "Selling (My Listings)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    if (sellingDeals.isEmpty()) {
                        item {
                            Text(
                                text = "No selling deals yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                        }
                    } else {
                        items(sellingDeals) { deal ->
                            DealCard(
                                deal = deal,
                                buttonText = "Delete Deal",
                                onActionClick = {
                                    coroutineScope.launch {
                                        try {
                                            SupabaseManager.client.postgrest["deals"].delete {
                                                filter { eq("id", deal.id) }
                                            }
                                            deals = deals?.filter { it.id != deal.id }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DealCard(
    deal: Deal,
    buttonText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        Text("📦", fontSize = 32.sp)
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
                        text = "Offer: $%.2f".format(deal.finalPrice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))

                    val statusColor = when (deal.status) {
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

            Spacer(Modifier.height(12.dp))
            
            Button(
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = buttonText, color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}





