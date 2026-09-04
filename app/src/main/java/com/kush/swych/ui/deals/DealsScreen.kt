package com.kush.swych.ui.deals

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import coil3.compose.AsyncImage
import com.kush.swych.core.data.AuthRepository
import com.kush.swych.core.data.DealRepository
import com.kush.swych.core.model.Deal
import com.kush.swych.core.network.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@Composable
fun DealsScreen(navController: androidx.navigation.NavController, onNavigateToMainTab: (Int) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dealRepo = remember { DealRepository(context) }
    val authRepo = remember { AuthRepository(context) }

    var deals by remember { mutableStateOf<List<Deal>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val currentUid = authRepo.currentUserUid

    fun loadDeals() {
        coroutineScope.launch {
            val res = dealRepo.getMyDeals()
            deals = res.getOrNull()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadDeals()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val currentDeals = deals
        if (isLoading && currentDeals == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (currentDeals == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Failed to load deals.")
            }
        } else {
            val buyingDeals = currentDeals.filter { it.buyerId == currentUid }
            val sellingDeals = currentDeals.filter { it.sellerId == currentUid }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 24.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "My Offers (Buying)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { loadDeals() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
                if (buyingDeals.isEmpty()) {
                    item {
                        Text(
                            text = "No offers sent yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }
                } else {
                    items(buyingDeals) { deal ->
                        BuyerDealCard(
                            deal = deal,
                            onCancelClick = {
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

                // Section 2: Selling
                item {
                    Text(
                        text = "My Deal Requests (Selling)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                if (sellingDeals.isEmpty()) {
                    item {
                        Text(
                            text = "No incoming requests yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }
                } else {
                    items(sellingDeals) { deal ->
                        SellerDealCard(
                            deal = deal,
                            onAccept = {
                                coroutineScope.launch {
                                    dealRepo.updateDealStatus(deal.id, deal.itemId, "SOLD")
                                    loadDeals()
                                }
                            },
                            onReject = {
                                coroutineScope.launch {
                                    dealRepo.updateDealStatus(deal.id, deal.itemId, "REJECTED")
                                    loadDeals()
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

@Composable
fun BuyerDealCard(
    deal: Deal,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            DealHeader(deal)
            Spacer(Modifier.height(12.dp))
            
            if (deal.status == "PENDING") {
                Button(
                    onClick = onCancelClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                ) {
                    Text(text = "Cancel Offer")
                }
            } else if (deal.status == "REJECTED" || deal.status == "SOLD") {
                Button(
                    onClick = onCancelClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(text = "Dismiss")
                }
            }
        }
    }
}

@Composable
fun SellerDealCard(
    deal: Deal,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            DealHeader(deal)
            Spacer(Modifier.height(12.dp))
            
            if (deal.status == "PENDING") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reject")
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White)
                    ) {
                        Text("Accept")
                    }
                }
            }
        }
    }
}

@Composable
fun DealHeader(deal: Deal) {
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
                text = "Offer: ₹${deal.finalPrice.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))

            val statusColor = when (deal.status) {
                "SOLD" -> Color(0xFF4CAF50)
                "REJECTED" -> Color(0xFFF44336)
                "PENDING" -> Color(0xFFF57F17)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
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
