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
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
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
    var selectedTab by remember { mutableStateOf(0) }

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
        SegmentedControl(selectedTabIndex = selectedTab, onTabSelected = { selectedTab = it })
        
        Box(modifier = Modifier.fillMaxSize().weight(1f).padding(top = 8.dp)) {
            val currentDeals = allDeals?.filter { 
                if (selectedTab == 0) it.buyerId == currentUid else it.sellerId == currentUid
            }
            
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
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn(animationSpec = tween(300))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + fadeOut(animationSpec = tween(300)))
                            } else {
                                (slideInHorizontally(animationSpec = tween(300)) { width -> -width } + fadeIn(animationSpec = tween(300))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(300)) { width -> width } + fadeOut(animationSpec = tween(300)))
                            }
                        },
                        label = "deals_animation",
                        modifier = Modifier.fillMaxSize()
                    ) { tab ->
                        val animatedDeals = allDeals?.filter { 
                            if (tab == 0) it.buyerId == currentUid else it.sellerId == currentUid
                        } ?: emptyList()

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(1),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 120.dp, top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (animatedDeals.isEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = if (tab == 0) "You haven't made any offers yet." else "No one has made offers on your items yet.",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                items(animatedDeals, key = { it.id }) { deal ->
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
                                                    onDelete = {
                                                        coroutineScope.launch {
                                                            val res = dealRepo.deleteDeal(deal.id, deal.itemId)
                                                            if (res.isSuccess) {
                                                                allDeals = allDeals?.filter { it.id != deal.id }
                                                            }
                                                        }
                                                    }
                                                )
                                            } else {
                                                SellerDealActions(
                                                    deal = deal,
                                                    buyerPhone = otherUser?.phone,
                                                    hapticManager = hapticManager,
                                                    onAccept = {
                                                        coroutineScope.launch {
                                                            dealRepo.updateDealStatus(deal.id, deal.itemId, "SOLD")
                                                            loadDeals(forceRefresh = true)
                                                        }
                                                    },
                                                    onReject = {
                                                        coroutineScope.launch {
                                                            dealRepo.updateDealStatus(deal.id, deal.itemId, "REJECTED")
                                                            loadDeals(forceRefresh = true)
                                                        }
                                                    },
                                                    onDelete = {
                                                        coroutineScope.launch {
                                                            val res = dealRepo.deleteDeal(deal.id, deal.itemId)
                                                            if (res.isSuccess) {
                                                                allDeals = allDeals?.filter { it.id != deal.id }
                                                            }
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
}

@Composable
fun RowScope.BuyerDealActions(deal: Deal, sellerPhone: String?, hapticManager: HapticManager, onDelete: () -> Unit) {
    if (deal.status == "PENDING") {
        Button(
            onClick = { hapticManager.triggerFeedback(); onDelete() },
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(28.dp).weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
        ) {
            Text("Cancel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    } else if (deal.status == "REJECTED") {
        Button(
            onClick = { hapticManager.triggerFeedback(); onDelete() },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(28.dp).weight(1f)
        ) {
            Text(text = "Dismiss", fontSize = 11.sp)
        }
    } else if (deal.status == "SOLD") {
        if (sellerPhone != null) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                com.kush.swych.ui.deals.ContactReveal(phone = sellerPhone, hapticManager = hapticManager)
            }
        } else {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(text = "Accepted", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun RowScope.SellerDealActions(deal: Deal, buyerPhone: String?, hapticManager: HapticManager, onAccept: () -> Unit, onReject: () -> Unit, onDelete: () -> Unit) {
    if (deal.status == "PENDING") {
        Button(
            onClick = { hapticManager.triggerFeedback(); onAccept() },
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(28.dp).weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = { hapticManager.triggerFeedback(); onReject() },
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(28.dp).weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
        ) {
            Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    } else if (deal.status == "REJECTED") {
        Button(
            onClick = { hapticManager.triggerFeedback(); onDelete() },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(28.dp).weight(1f)
        ) {
            Text(text = "Delete", fontSize = 11.sp)
        }
    } else if (deal.status == "SOLD") {
        if (buyerPhone != null) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                com.kush.swych.ui.deals.ContactReveal(phone = buyerPhone, hapticManager = hapticManager)
            }
        } else {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(text = "Sold", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 11.sp)
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

@Composable
fun SegmentedControl(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Offers", "My Products")
    
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp)
    ) {
        val tabWidth = maxWidth / 2
        
        val indicatorOffset by animateDpAsState(
            targetValue = if (selectedTabIndex == 0) 0.dp else tabWidth,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
            label = "indicator_offset"
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
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    val color by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "tab_color_$index"
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = color
                    )
                }
            }
        }
    }
}
