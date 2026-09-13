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
import androidx.compose.foundation.border
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

    var allItems by remember { mutableStateOf<List<Item>?>(null) }
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
                
                val itemRepo = com.kush.swych.core.data.ItemRepository(context)
                val itemRes = itemRepo.getAllItems(forceRefresh = forceRefresh)
                if (itemRes.isSuccess) {
                    allItems = itemRes.getOrNull()
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
            val hasData = allDeals != null && allItems != null
            
            @OptIn(ExperimentalMaterial3Api::class)
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    loadDeals(forceRefresh = true)
                },
                modifier = Modifier.fillMaxSize()
            ) {
                if (isLoading && !hasData) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (!hasData) {
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
                        
                        val displayList: List<Any> = if (tab == 0) {
                            allDeals?.filter { it.buyerId == currentUid } ?: emptyList()
                        } else {
                            val deals = allDeals?.filter { it.sellerId == currentUid } ?: emptyList()
                            val itemsWithDeals = deals.map { it.itemId }.toSet()
                            val listedItems = allItems?.filter { it.sellerId == currentUid && it.id !in itemsWithDeals } ?: emptyList()
                            
                            val sortedDeals = deals.sortedByDescending { it.status.uppercase() == "PENDING" }
                            sortedDeals + listedItems
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(1),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 120.dp, top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (displayList.isEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = if (tab == 0) "You haven't made any offers yet." else "You haven't listed any items yet.",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                items(displayList, key = { if (it is Deal) it.id else (it as Item).id }) { itemObj ->
                                    if (itemObj is Deal) {
                                        val deal = itemObj
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
                                        var showDropdown by remember { mutableStateOf(false) }
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
                                                    val status = deal.status.uppercase().trim()
                                                    val color = when (status) {
                                                        "SOLD", "ACCEPTED" -> Color(0xFF4CAF50)
                                                        "REJECTED" -> MaterialTheme.colorScheme.error
                                                        else -> MaterialTheme.colorScheme.primary
                                                    }
                                                    val text = when (status) {
                                                        "SOLD", "ACCEPTED" -> "Accepted"
                                                        "REJECTED" -> "Rejected"
                                                        "PENDING", "OPEN" -> "Applied"
                                                        else -> "Applied ($status)"
                                                    }
                                                    
                                                    Button(
                                                        onClick = {
                                                            if (status == "SOLD") {
                                                                showDropdown = !showDropdown
                                                            } else if (status == "REJECTED") {
                                                                coroutineScope.launch {
                                                                    val res = dealRepo.deleteDeal(deal.id, deal.itemId)
                                                                    if (res.isSuccess) {
                                                                        loadDeals(forceRefresh = true)
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier.weight(1f).height(28.dp),
                                                        contentPadding = PaddingValues(0.dp),
                                                        shape = RoundedCornerShape(50),
                                                        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.1f), contentColor = color)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                                            Text(
                                                                text = text,
                                                                color = color,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            if (status == "SOLD") {
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Icon(
                                                                    imageVector = Icons.Default.ArrowDropDown,
                                                                    contentDescription = null,
                                                                    tint = color,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            } else if (status == "REJECTED") {
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Icon(
                                                                    imageVector = Icons.Default.Close,
                                                                    contentDescription = "Dismiss",
                                                                    tint = color,
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                            }
                                                        }
                                                    }
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
                                                                    loadDeals(forceRefresh = true)
                                                                }
                                                            }
                                                        },
                                                        onToggleDropdown = {
                                                            showDropdown = !showDropdown
                                                        }
                                                    )
                                                }
                                            },
                                            dropdownContent = {
                                                AnimatedVisibility(
                                                    visible = showDropdown && deal.status.uppercase() == "SOLD",
                                                    enter = expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)),
                                                    exit = shrinkVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                                                ) {
                                                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                                                        Row(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(50))
                                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                                                .clickable {
                                                                    otherUser?.phone?.let {
                                                                        hapticManager.triggerFeedback()
                                                                        try {
                                                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                                                data = Uri.parse("tel:$it")
                                                                            }
                                                                            context.startActivity(intent)
                                                                        } catch (_: Exception) {}
                                                                    }
                                                                },
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(if (isBuyer) "Call Seller" else "Call Buyer", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    } else if (itemObj is Item) {
                                        val item = itemObj
                                        val myUser = users[currentUid]
                                        val myDeals = allDeals?.filter { it.sellerId == currentUid } ?: emptyList()
                                        val dealsMade = myDeals.count { it.status == "SOLD" }
                                        val dealsExpired = myDeals.count { it.status == "REJECTED" }
                                        
                                        ItemCard(
                                            item = item,
                                            sellerName = myUser?.name ?: "Unknown",
                                            sellerBlock = myUser?.block ?: "Unknown",
                                            dealsMade = dealsMade,
                                            dealsExpired = dealsExpired,
                                            isOwnItem = true,
                                            onClick = {},
                                            onDealClick = {},
                                            bottomActions = {
                                                ListedItemActions(
                                                    hapticManager = hapticManager,
                                                    onDelete = {
                                                        coroutineScope.launch {
                                                            val itemRepo = com.kush.swych.core.data.ItemRepository(context)
                                                            itemRepo.deleteItem(item.id)
                                                            loadDeals(forceRefresh = true)
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
        }
    }
}

@Composable
fun RowScope.ListedItemActions(hapticManager: HapticManager, onDelete: () -> Unit) {
    Button(
        onClick = { hapticManager.triggerFeedback(); onDelete() },
        modifier = Modifier.weight(1f).height(28.dp),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text("Listed", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(14.dp))
        }
    }
}

// Note: BuyerDealActions is no longer a separate composable here, we will inline it into DealsScreen so it can control the dropdown.

@Composable
fun RowScope.SellerDealActions(
    deal: Deal, 
    buyerPhone: String?, 
    hapticManager: HapticManager, 
    onAccept: () -> Unit, 
    onReject: () -> Unit, 
    onDelete: () -> Unit,
    onToggleDropdown: () -> Unit
) {
    val status = deal.status.uppercase().trim()
    if (status == "PENDING" || status == "OPEN" || status.isBlank()) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(28.dp)
                .clip(RoundedCornerShape(50))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(50)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { hapticManager.triggerFeedback(); onAccept() },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f), contentColor = Color(0xFF4CAF50))
            ) {
                Text("Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
            Button(
                onClick = { hapticManager.triggerFeedback(); onReject() },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    } else if (status == "REJECTED") {
        Button(
            onClick = { hapticManager.triggerFeedback(); onDelete() },
            modifier = Modifier.weight(1f).height(28.dp),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.error)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text("Rejected", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(14.dp))
            }
        }
    } else if (status == "SOLD") {
        Button(
            onClick = { hapticManager.triggerFeedback(); onToggleDropdown() },
            modifier = Modifier.weight(1f).height(28.dp),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f), contentColor = Color(0xFF4CAF50))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text("Accepted", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    } else {
        Button(
            onClick = { hapticManager.triggerFeedback(); onAccept() },
            modifier = Modifier.weight(1f).height(28.dp),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Unknown: '$status' - Tap to Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
