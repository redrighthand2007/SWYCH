package com.kush.swych.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kush.swych.core.data.AuthRepository
import com.kush.swych.core.data.DealRepository
import com.kush.swych.core.data.ItemRepository
import com.kush.swych.core.designsystem.component.DotBadge
import com.kush.swych.core.designsystem.component.shimmerEffect
import com.kush.swych.core.model.Deal
import com.kush.swych.core.model.Item
import com.kush.swych.core.model.User
import com.kush.swych.core.util.SettingsManager
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavController,
    onNavigateToMainTab: (Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = LocalHapticFeedback.current
    val authRepo = remember { AuthRepository(context) }
    val dealRepo = remember { DealRepository(context) }
    val itemRepo = remember { ItemRepository(context) }

    var user by remember { mutableStateOf<User?>(null) }
    var myItems by remember { mutableStateOf<List<Item>?>(null) }
    var boughtDeals by remember { mutableStateOf<List<Deal>?>(null) }
    var soldDeals by remember { mutableStateOf<List<Deal>?>(null) }
    var counterpartyDots by remember { mutableStateOf<Map<String, Pair<Int, Int>>>(emptyMap()) } 

    val themeMode by SettingsManager.themeMode.collectAsState()
    val hapticEnabled by SettingsManager.hapticEnabled.collectAsState()

    var showSignOutDialog by remember { mutableStateOf(false) }
    var myItemsExpanded by remember { mutableStateOf(false) }
    var boughtExpanded by remember { mutableStateOf(false) }
    var historyExpanded by remember { mutableStateOf(false) }

    val performHaptic = {
        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    LaunchedEffect(Unit) {
        val fetchedUser = authRepo.getCurrentUserProfile().getOrNull()
        // Fallback to mock data if backend fails (since backend is on hold)
        val u = fetchedUser ?: com.kush.swych.core.model.User(
            uid = "fallback_uid",
            name = "Test User",
            email = "test@vitstudent.ac.in",
            phone = "+91 9999999999",
            block = "MH-B",
            greenDots = 5,
            redDots = 0
        )
        user = u
        if (fetchedUser != null) {
            val itemsResult = itemRepo.getMyItems()
            val items = itemsResult.getOrNull() ?: emptyList()
            myItems = items.filter { it.status == com.kush.swych.core.util.Constants.STATUS_OPEN }

            val dealsResult = dealRepo.getMyDeals()
            val deals = dealsResult.getOrNull() ?: emptyList()
            val completed = deals.filter { it.status == "completed" }
            boughtDeals = completed.filter { it.buyerId == u.uid }
            soldDeals = completed.filter { it.sellerId == u.uid }

            val dotsMap = mutableMapOf<String, Pair<Int, Int>>()
            completed.forEach { deal ->
                val otherId = if (deal.sellerId == u.uid) deal.buyerId else deal.sellerId
                if (!dotsMap.containsKey(otherId)) {
                    // TODO: Replace with real user fetch
                    val otherUser: User? = null
                    if (otherUser != null) {
                        dotsMap[otherId] = Pair(otherUser.greenDots, otherUser.redDots)
                    }
                }
            }
            counterpartyDots = dotsMap
        } else {
            // Load mock lists
            val fallbackItem = com.kush.swych.core.model.Item(
                id = "item1",
                sellerId = "fallback_uid",
                category = "Electronics",
                title = "Scientific Calculator",
                description = "Like new",
                price = 500.0,
                status = com.kush.swych.core.util.Constants.STATUS_OPEN
            )
            myItems = listOf(fallbackItem)
            boughtDeals = emptyList()
            soldDeals = emptyList()
        }
    }

    if (user == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shimmerEffect(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(0.25f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(0.75f)) {
                        Box(modifier = Modifier.height(20.dp).fillMaxWidth(0.6f).background(Color.Gray.copy(alpha = 0.3f)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.height(16.dp).fillMaxWidth(0.4f).background(Color.Gray.copy(alpha = 0.3f)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.height(16.dp).fillMaxWidth(0.5f).background(Color.Gray.copy(alpha = 0.3f)))
                    }
                }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(0.25f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.fillMaxSize(0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(0.75f)
                        ) {
                            Text(
                                text = user!!.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val username = user!!.email.substringBefore("@")
                            Text(
                                text = "@$username",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Hostel: ${user!!.block}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = user!!.phone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            DotBadge(greenDots = user!!.greenDots, redDots = user!!.redDots)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            item {
                ExpandableListSection(
                    title = "Items kept for selling by me",
                    icon = Icons.Default.Sell,
                    count = myItems?.size ?: 0,
                    expanded = myItemsExpanded,
                    onToggle = { 
                        performHaptic()
                        myItemsExpanded = !myItemsExpanded 
                    }
                ) {
                    if (myItems == null) {
                        EmptyListMessage("Loading...")
                    } else if (myItems!!.isEmpty()) {
                        EmptyListMessage("You haven't listed any items.")
                    } else {
                        myItems!!.forEach { item ->
                            HistoryCard(title = item.title, subtitle = "₹${item.price}", dots = null)
                        }
                    }
                }
            }

            item {
                ExpandableListSection(
                    title = "Items which I bought",
                    icon = Icons.Default.ShoppingBag,
                    count = boughtDeals?.size ?: 0,
                    expanded = boughtExpanded,
                    onToggle = { 
                        performHaptic()
                        boughtExpanded = !boughtExpanded 
                    }
                ) {
                    if (boughtDeals == null) {
                        EmptyListMessage("Loading...")
                    } else if (boughtDeals!!.isEmpty()) {
                        EmptyListMessage("You haven't bought any items yet.")
                    } else {
                        boughtDeals!!.forEach { deal ->
                            val dots = counterpartyDots[deal.sellerId]
                            HistoryCard(title = deal.itemTitle, subtitle = "From Seller", dots = dots)
                        }
                    }
                }
            }

            item {
                ExpandableListSection(
                    title = "History (Deals done by me)",
                    icon = Icons.Default.History,
                    count = soldDeals?.size ?: 0,
                    expanded = historyExpanded,
                    onToggle = { 
                        performHaptic()
                        historyExpanded = !historyExpanded 
                    }
                ) {
                    if (soldDeals == null) {
                        EmptyListMessage("Loading...")
                    } else if (soldDeals!!.isEmpty()) {
                        EmptyListMessage("You haven't completed any sales yet.")
                    } else {
                        soldDeals!!.forEach { deal ->
                            val dots = counterpartyDots[deal.buyerId]
                            HistoryCard(title = deal.itemTitle, subtitle = "Sold to Buyer", dots = dots)
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("Theme Options", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(12.dp))
                    ThemeSegmentedControl(
                        selectedMode = themeMode,
                        onModeSelected = { 
                            performHaptic()
                            SettingsManager.setThemeMode(it)
                        }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newState = !hapticEnabled
                            SettingsManager.setHapticEnabled(newState)
                            if (newState) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Haptic Feedback",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = hapticEnabled,
                        onCheckedChange = {
                            SettingsManager.setHapticEnabled(it)
                            if (it) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            performHaptic()
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@swych.com"))
                                putExtra(Intent.EXTRA_SUBJECT, "Swych Support")
                            }
                            context.startActivity(Intent.createChooser(intent, "Send Email"))
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Contact Us",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Report issues or suggest features",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            performHaptic()
                            showSignOutDialog = true
                        }
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out?") },
            text = { Text("You'll need to sign in again to use Swych.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        performHaptic()
                        showSignOutDialog = false
                        kotlinx.coroutines.GlobalScope.launch {
                            authRepo.logout()
                        }
                        navController.navigate(com.kush.swych.ui.navigation.AuthRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ThemeSegmentedControl(selectedMode: Int, onModeSelected: (Int) -> Unit) {
    val modes = listOf(1 to "Light", 0 to "System", 2 to "Dark")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        modes.forEachIndexed { index, (mode, label) ->
            val isSelected = selectedMode == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable { onModeSelected(mode) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
            if (index < modes.size - 1) {
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outline))
            }
        }
    }
}

@Composable
private fun ExpandableListSection(
    title: String,
    icon: ImageVector,
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "$title ($count)",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun EmptyListMessage(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun HistoryCard(title: String, subtitle: String, dots: Pair<Int, Int>?) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (dots != null) {
                    DotBadge(greenDots = dots.first, redDots = dots.second)
                }
            }
        }
    }
}





