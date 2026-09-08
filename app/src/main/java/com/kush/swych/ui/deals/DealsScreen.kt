package com.kush.swych.ui.deals

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sell
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import kotlinx.coroutines.launch

@Composable
fun DealsScreen(navController: androidx.navigation.NavController, onNavigateToMainTab: (Int) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dealRepo = remember { DealRepository(context) }
    val authRepo = remember { AuthRepository(context) }
    val hapticManager = remember { HapticManager(context) }

    var allDeals by remember { mutableStateOf<List<Deal>?>(null) }
    var users by remember { mutableStateOf<Map<String, User>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }

    val currentUid = authRepo.currentUserUid

    fun loadDeals(forceRefresh: Boolean = false) {
        coroutineScope.launch {
            try {
                val res = dealRepo.getAllDeals(forceRefresh = forceRefresh)
                if (res.isSuccess) {
                    allDeals = res.getOrNull()
                }
                
                val userRes = authRepo.getAllUsers(forceRefresh = forceRefresh)
                users = userRes.getOrNull()?.associateBy { it.uid } ?: emptyMap()
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadDeals()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Deals",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            val currentDeals = allDeals?.filter { it.buyerId == currentUid || it.sellerId == currentUid }
            
            @OptIn(ExperimentalMaterial3Api::class)
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    loadDeals(forceRefresh = true)
                },
                modifier = Modifier.fillMaxSize()
            ) {
                if (isLoading && currentDeals == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (currentDeals == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Failed to load deals.")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (currentDeals.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "No deals yet! Make an offer or list an item.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(currentDeals, key = { it.id }) { deal ->
                                val isBuyer = deal.buyerId == currentUid
                                val otherUser = if (isBuyer) users[deal.sellerId] else users[deal.buyerId]
                                val sellerName = otherUser?.name ?: "Unknown"
                                
                                val otherUserDeals = allDeals?.filter { it.sellerId == otherUser?.uid } ?: emptyList()
                                val dealsMade = otherUserDeals.count { it.status == "SOLD" }
                                val dealsExpired = otherUserDeals.count { it.status == "REJECTED" }
                                
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
                                    sellerName = sellerName,
                                    sellerBlock = otherUser?.block ?: "Unknown",
                                    dealsMade = dealsMade,
                                    dealsExpired = dealsExpired,
                                    isOwnItem = !isBuyer,
                                    onClick = {},
                                    onDealClick = {},
                                    bottomActions = {
                                        if (isBuyer) {
                                            BuyerDealActions(
                                                deal = deal,
                                                sellerPhone = otherUser?.phone,
                                                hapticManager = hapticManager,
                                                onCancel = {
                                                    coroutineScope.launch {
                                                        val res = dealRepo.deleteDeal(deal.id, deal.itemId)
                                                        if (res.isSuccess) {
                                                            allDeals = allDeals?.filter { it.id != deal.id }
                                                        }
                                                    }
                                                },
                                                onAccept = {
                                                    coroutineScope.launch {
                                                        dealRepo.updateDealStatus(deal.id, deal.itemId, "SOLD")
                                                        loadDeals(forceRefresh = true)
                                                    }
                                                }
                                            )
                                        } else {
                                            SellerDealActions(
                                                deal = deal,
                                                buyerPhone = otherUser?.phone,
                                                hapticManager = hapticManager,
                                                onDelete = {
                                                    coroutineScope.launch {
                                                        dealRepo.updateDealStatus(deal.id, deal.itemId, "REJECTED")
                                                        loadDeals(forceRefresh = true)
                                                    }
                                                }
                                            )
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
}

@Composable
fun BuyerDealActions(deal: Deal, sellerPhone: String?, hapticManager: HapticManager, onCancel: () -> Unit, onAccept: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (deal.status == "PENDING") {
            Button(
                onClick = { hapticManager.triggerFeedback(); onAccept() },
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { hapticManager.triggerFeedback(); onCancel() },
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
            ) {
                Text("Cancel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        } else if (deal.status == "REJECTED") {
            Button(
                onClick = { hapticManager.triggerFeedback(); onCancel() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text(text = "Dismiss", fontSize = 11.sp)
            }
        } else if (deal.status == "SOLD") {
            if (sellerPhone != null) {
                com.kush.swych.ui.deals.ContactReveal(phone = sellerPhone, hapticManager = hapticManager)
            } else {
                Text(text = "Accepted", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SellerDealActions(deal: Deal, buyerPhone: String?, hapticManager: HapticManager, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (deal.status == "PENDING" || deal.status == "REJECTED") {
            Button(
                onClick = { hapticManager.triggerFeedback(); onDelete() },
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Delete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        } else if (deal.status == "SOLD") {
            if (buyerPhone != null) {
                com.kush.swych.ui.deals.ContactReveal(phone = buyerPhone, hapticManager = hapticManager)
            } else {
                Text(text = "Sold", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ContactReveal(phone: String?, hapticManager: HapticManager) {
    val context = LocalContext.current
    if (phone != null) {
        IconButton(
            onClick = {
                hapticManager.triggerFeedback()
                try {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phone")
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    // No dialer app available — safely ignore
                }
            },
            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)).size(36.dp)
        ) {
            Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
        }
    }
}
