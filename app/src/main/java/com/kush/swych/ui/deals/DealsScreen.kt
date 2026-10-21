package com.kush.swych.ui.deals

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.kush.swych.core.data.AuthRepository
import com.kush.swych.core.data.DealRepository
import com.kush.swych.core.data.ItemRepository
import com.kush.swych.core.designsystem.component.ItemCard
import com.kush.swych.core.model.Deal
import com.kush.swych.core.model.DealStatus
import com.kush.swych.core.model.Item
import com.kush.swych.core.model.ItemStatus
import com.kush.swych.core.model.User
import com.kush.swych.core.network.SupabaseManager
import com.kush.swych.core.util.HapticManager
import com.kush.swych.core.util.userMessage
import com.kush.swych.ui.navigation.ItemDetailRoute
import kotlinx.coroutines.launch

private val SuccessGreen = Color(0xFF4CAF50)

/** A destructive or important action waiting for the user to confirm it in a dialog. */
private class PendingConfirmation(
    val title: String,
    val text: String,
    val confirmLabel: String,
    val onConfirm: () -> Unit
)

@Composable
fun DealsScreen(navController: NavController, onNavigateToMainTab: (Int) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dealRepo = remember { DealRepository(context) }
    val itemRepo = remember { ItemRepository(context) }
    val authRepo = remember { AuthRepository(context) }
    val hapticManager = remember { HapticManager(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var deals by remember { mutableStateOf<List<Deal>?>(null) }
    var myItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var users by remember { mutableStateOf<Map<String, User>>(emptyMap()) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    // Deals with a request in flight; their buttons are disabled so nothing is sent twice
    var busyIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var confirmation by remember { mutableStateOf<PendingConfirmation?>(null) }

    val currentUid = SupabaseManager.currentUid

    // Always fetches fresh data, so an offer behind the red badge is visible right away
    suspend fun load() {
        val dealsResult = dealRepo.getMyDeals()
        dealsResult.onSuccess { deals = it; loadError = null }
            .onFailure { loadError = it.userMessage() }
        itemRepo.getMyItems().onSuccess { myItems = it }
        val loaded = deals.orEmpty()
        users = authRepo.getUsers(loaded.flatMap { listOf(it.buyerId, it.sellerId) }.toSet())
        isRefreshing = false
    }

    fun runAction(id: String, successMessage: String?, action: suspend () -> Result<Unit>) {
        if (id in busyIds) return
        hapticManager.triggerFeedback()
        busyIds = busyIds + id
        scope.launch {
            val result = action()
            busyIds = busyIds - id
            result.onSuccess { if (successMessage != null) snackbarHostState.showSnackbar(successMessage) }
                .onFailure { snackbarHostState.showSnackbar(it.userMessage()) }
            load()
        }
    }

    fun callOtherSide(deal: Deal) {
        hapticManager.triggerFeedback()
        scope.launch {
            val phone = dealRepo.getContactPhone(deal.id).getOrNull()
            if (phone.isNullOrBlank()) {
                snackbarHostState.showSnackbar("Phone number isn't available for this deal.")
                return@launch
            }
            try {
                context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$phone".toUri()))
            } catch (_: Exception) {
                snackbarHostState.showSnackbar("No dialer app found. Number: $phone")
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()
        ) {
            SegmentedControl(selectedTabIndex = selectedTab, onTabSelected = { selectedTab = it })

            Box(modifier = Modifier.fillMaxSize().weight(1f).padding(top = 8.dp)) {
                @OptIn(ExperimentalMaterial3Api::class)
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        scope.launch { load() }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    val loadedDeals = deals
                    if (loadedDeals == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (loadError == null) CircularProgressIndicator()
                            else Text(loadError ?: "", textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
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
                                loadedDeals.filter { it.buyerId == currentUid }.sortedBy { statusOrder(it.status) }
                            } else {
                                val sellerDeals = loadedDeals.filter { it.sellerId == currentUid }
                                // Open listings with no live offer get their own card so they can be managed
                                val itemsWithLiveDeals = sellerDeals.filter { it.isLive }.map { it.itemId }.toSet()
                                val idleListings = myItems.filter { it.status == ItemStatus.OPEN && it.id !in itemsWithLiveDeals }
                                sellerDeals.sortedBy { statusOrder(it.status) } + idleListings
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
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    items(displayList, key = { if (it is Deal) it.id else (it as Item).id }) { entry ->
                                        if (entry is Deal) {
                                            DealCard(
                                                deal = entry,
                                                currentUid = currentUid,
                                                otherUser = users[if (entry.buyerId == currentUid) entry.sellerId else entry.buyerId],
                                                isBusy = entry.id in busyIds,
                                                onOpenItem = { navController.navigate(ItemDetailRoute(entry.itemId)) },
                                                onAccept = {
                                                    val buyerName = users[entry.buyerId]?.name ?: "this buyer"
                                                    confirmation = PendingConfirmation(
                                                        title = "Accept offer?",
                                                        text = "Accept ₹%.0f from %s for %s? Other offers on this item will be rejected.".format(entry.finalPrice, buyerName, entry.itemTitle),
                                                        confirmLabel = "Accept"
                                                    ) { runAction(entry.id, "Offer accepted. Call the buyer to meet up.") { dealRepo.acceptDeal(entry.id) } }
                                                },
                                                onReject = { runAction(entry.id, "Offer rejected.") { dealRepo.rejectDeal(entry.id) } },
                                                onWithdraw = {
                                                    confirmation = PendingConfirmation(
                                                        title = "Withdraw offer?",
                                                        text = "Your offer for ${entry.itemTitle} will be removed.",
                                                        confirmLabel = "Withdraw"
                                                    ) { runAction(entry.id, "Offer withdrawn.") { dealRepo.cancelDeal(entry.id) } }
                                                },
                                                onConfirmHandover = {
                                                    confirmation = PendingConfirmation(
                                                        title = "Mark as done?",
                                                        text = "Only confirm once the item and payment have been exchanged. The deal completes when both of you confirm.",
                                                        confirmLabel = "Confirm"
                                                    ) { runAction(entry.id, "Marked as done.") { dealRepo.confirmDeal(entry.id) } }
                                                },
                                                onCancelDeal = {
                                                    confirmation = PendingConfirmation(
                                                        title = "Cancel this deal?",
                                                        text = "The item goes back on sale and a cancelled deal is added to your profile.",
                                                        confirmLabel = "Cancel deal"
                                                    ) { runAction(entry.id, "Deal cancelled.") { dealRepo.cancelDeal(entry.id) } }
                                                },
                                                onDismiss = { runAction(entry.id, null) { dealRepo.dismissDeal(entry.id) } },
                                                onCall = { callOtherSide(entry) }
                                            )
                                        } else if (entry is Item) {
                                            val me = users[currentUid]
                                            ItemCard(
                                                item = entry,
                                                sellerName = me?.name ?: "You",
                                                sellerBlock = me?.block ?: "",
                                                dealsMade = me?.greenDots ?: 0,
                                                dealsExpired = me?.redDots ?: 0,
                                                isOwnItem = true,
                                                onClick = { navController.navigate(ItemDetailRoute(entry.id)) },
                                                onDealClick = {},
                                                bottomActions = {
                                                    ActionPill("Listed", MaterialTheme.colorScheme.onSurfaceVariant, enabled = false) {}
                                                    ActionPill("Remove", MaterialTheme.colorScheme.error, enabled = entry.id !in busyIds) {
                                                        confirmation = PendingConfirmation(
                                                            title = "Remove listing?",
                                                            text = "${entry.title} will be removed from Swych.",
                                                            confirmLabel = "Remove"
                                                        ) { runAction(entry.id, "Listing removed.") { itemRepo.deleteItem(entry.id) } }
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp, start = 24.dp, end = 24.dp)
        )
    }

    confirmation?.let { pending ->
        AlertDialog(
            onDismissRequest = { confirmation = null },
            title = { Text(pending.title) },
            text = { Text(pending.text) },
            confirmButton = {
                TextButton(onClick = { confirmation = null; pending.onConfirm() }) { Text(pending.confirmLabel) }
            },
            dismissButton = {
                TextButton(onClick = { confirmation = null }) { Text("Back") }
            }
        )
    }
}

/** Things needing action first, finished deals last. */
private fun statusOrder(status: String): Int = when (status) {
    DealStatus.ACCEPTED -> 0
    DealStatus.PENDING -> 1
    DealStatus.COMPLETED -> 2
    else -> 3
}

@Composable
private fun DealCard(
    deal: Deal,
    currentUid: String?,
    otherUser: User?,
    isBusy: Boolean,
    onOpenItem: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onWithdraw: () -> Unit,
    onConfirmHandover: () -> Unit,
    onCancelDeal: () -> Unit,
    onDismiss: () -> Unit,
    onCall: () -> Unit
) {
    val isBuyer = deal.buyerId == currentUid
    val iConfirmed = if (isBuyer) deal.buyerConfirmed else deal.sellerConfirmed
    val theyConfirmed = if (isBuyer) deal.sellerConfirmed else deal.buyerConfirmed
    val enabled = !isBusy

    ItemCard(
        item = Item(
            id = deal.itemId,
            sellerId = deal.sellerId,
            title = deal.itemTitle,
            price = deal.finalPrice,
            status = deal.status,
            photoUrl = deal.itemPhotoUrl
        ),
        sellerName = otherUser?.name ?: "Unknown",
        sellerBlock = otherUser?.block ?: "",
        dealsMade = otherUser?.greenDots ?: 0,
        dealsExpired = otherUser?.redDots ?: 0,
        isOwnItem = !isBuyer,
        onClick = onOpenItem,
        onDealClick = {},
        bottomActions = {
            when (deal.status) {
                DealStatus.PENDING -> if (isBuyer) {
                    ActionPill("Applied", MaterialTheme.colorScheme.primary, enabled = false) {}
                    ActionPill("Withdraw", MaterialTheme.colorScheme.error, enabled = enabled, onClick = onWithdraw)
                } else {
                    ActionPill("Accept", SuccessGreen, enabled = enabled, onClick = onAccept)
                    ActionPill("Reject", MaterialTheme.colorScheme.error, enabled = enabled, onClick = onReject)
                }
                DealStatus.ACCEPTED -> {
                    ActionPill("Call", MaterialTheme.colorScheme.primary, icon = Icons.Default.Call, onClick = onCall)
                    if (iConfirmed) {
                        ActionPill("Waiting", SuccessGreen, enabled = false) {}
                    } else {
                        ActionPill("Done", SuccessGreen, enabled = enabled, onClick = onConfirmHandover)
                    }
                    ActionPill("Cancel", MaterialTheme.colorScheme.error, enabled = enabled, onClick = onCancelDeal)
                }
                DealStatus.COMPLETED -> {
                    ActionPill("Completed", SuccessGreen, enabled = false) {}
                    ActionPill("Call", MaterialTheme.colorScheme.primary, icon = Icons.Default.Call, onClick = onCall)
                }
                else -> {
                    val label = when (deal.status) {
                        DealStatus.REJECTED -> "Rejected"
                        DealStatus.CANCELLED -> "Cancelled"
                        DealStatus.EXPIRED -> "Expired"
                        else -> deal.status.lowercase().replaceFirstChar { it.uppercase() }
                    }
                    ActionPill(label, MaterialTheme.colorScheme.error, icon = Icons.Default.Close, enabled = enabled, onClick = onDismiss)
                }
            }
        },
        dropdownContent = if (deal.status == DealStatus.ACCEPTED) {
            {
                val hint = when {
                    iConfirmed -> "Waiting for the ${if (isBuyer) "seller" else "buyer"} to confirm the hand-over."
                    theyConfirmed -> "The ${if (isBuyer) "seller" else "buyer"} marked this done. Tap Done once you've exchanged the item."
                    else -> "Meet up, then both tap Done. Unconfirmed deals expire after 3 days."
                }
                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        } else null
    )
}

@Composable
private fun RowScope.ActionPill(
    text: String,
    color: Color,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.weight(1f).height(28.dp),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.1f),
            contentColor = color,
            disabledContainerColor = color.copy(alpha = 0.08f),
            disabledContentColor = color.copy(alpha = 0.8f)
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(2.dp))
            }
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
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
