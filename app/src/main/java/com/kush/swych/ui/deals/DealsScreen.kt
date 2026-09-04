package com.kush.swych.ui.deals

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Call
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.kush.swych.core.model.Item
import com.kush.swych.core.model.User
import com.kush.swych.core.network.SupabaseManager
import com.kush.swych.core.designsystem.component.ItemCard
import com.kush.swych.core.util.HapticManager
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@Composable
fun DealsScreen(navController: androidx.navigation.NavController, onNavigateToMainTab: (Int) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dealRepo = remember { DealRepository(context) }
    val authRepo = remember { AuthRepository(context) }
    val hapticManager = remember { HapticManager(context) }

    var deals by remember { mutableStateOf<List<Deal>?>(null) }
    var users by remember { mutableStateOf<Map<String, User>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    val currentUid = authRepo.currentUserUid

    fun loadDeals() {
        coroutineScope.launch {
            val res = dealRepo.getMyDeals()
            val userRes = authRepo.getAllUsers()
            
            deals = res.getOrNull()
            users = userRes.getOrNull()?.associateBy { it.uid } ?: emptyMap()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadDeals()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Deals",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )

        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
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

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Offers (Buying)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 0.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Offers",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { loadDeals() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                if (buyingDeals.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No offers sent yet.",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(buyingDeals, key = { it.id }) { deal ->
                        val seller = users[deal.sellerId]
                        val dummyItem = Item(
                            id = deal.itemId,
                            sellerId = deal.sellerId,
                            title = deal.itemTitle,
                            description = "",
                            price = deal.finalPrice,
                            category = "Deals",
                            status = deal.status,
                            photoUrl = deal.itemPhotoUrl
                        )
                        ItemCard(
                            item = dummyItem,
                            sellerBlock = seller?.block ?: "Unknown",
                            isOwnItem = false,
                            onClick = {},
                            onDealClick = {},
                            bottomActions = {
                                BuyerDealActions(deal = deal, sellerPhone = seller?.phone, hapticManager = hapticManager, onCancel = {
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
                                })
                            }
                        )
                    }
                }

                // Section 2: My Products (Selling)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "My Products",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                if (sellingDeals.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No incoming requests yet.",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(sellingDeals, key = { it.id }) { deal ->
                        val buyer = users[deal.buyerId]
                        val dummyItem = Item(
                            id = deal.itemId,
                            sellerId = deal.sellerId,
                            title = deal.itemTitle,
                            description = "",
                            price = deal.finalPrice,
                            category = "Deals",
                            status = deal.status,
                            photoUrl = deal.itemPhotoUrl
                        )
                        ItemCard(
                            item = dummyItem,
                            sellerBlock = buyer?.block ?: "Unknown",
                            isOwnItem = true,
                            onClick = {},
                            onDealClick = {},
                            bottomActions = {
                                SellerDealActions(
                                    deal = deal,
                                    buyerPhone = buyer?.phone,
                                    hapticManager = hapticManager,
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
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
fun BuyerDealActions(deal: Deal, sellerPhone: String?, hapticManager: HapticManager, onCancel: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "₹" + "%.0f".format(deal.finalPrice),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))

        if (deal.status == "PENDING") {
            Button(
                onClick = { hapticManager.triggerFeedback(); onCancel() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
            ) {
                Text(text = "Cancel", fontSize = 12.sp)
            }
        } else if (deal.status == "REJECTED") {
            Button(
                onClick = { hapticManager.triggerFeedback(); onCancel() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Text(text = "Dismiss", fontSize = 12.sp)
            }
        } else if (deal.status == "SOLD") {
            ContactReveal(phone = sellerPhone, hapticManager = hapticManager)
        }
    }
}

@Composable
fun SellerDealActions(deal: Deal, buyerPhone: String?, hapticManager: HapticManager, onAccept: () -> Unit, onReject: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "₹" + "%.0f".format(deal.finalPrice),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))

        if (deal.status == "PENDING") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { hapticManager.triggerFeedback(); onReject() },
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                ) {
                    Text("Reject", fontSize = 12.sp)
                }
                Button(
                    onClick = { hapticManager.triggerFeedback(); onAccept() },
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White)
                ) {
                    Text("Accept", fontSize = 12.sp)
                }
            }
        } else if (deal.status == "SOLD") {
            ContactReveal(phone = buyerPhone, hapticManager = hapticManager)
        } else if (deal.status == "REJECTED") {
             Text("Rejected", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ContactReveal(phone: String?, hapticManager: HapticManager) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Contact:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = phone ?: "No Phone",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (phone != null) {
            IconButton(
                onClick = {
                    hapticManager.triggerFeedback()
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phone")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)).size(36.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
            }
        }
    }
}
