package com.kush.swych.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kush.swych.core.data.AuthRepository
import com.kush.swych.core.model.User
import android.widget.Toast
import com.kush.swych.core.util.SettingsManager
import com.kush.swych.core.util.userMessage
import com.kush.swych.ui.navigation.AuthRoute
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavController,
    expandContactDev: Boolean = false
) {
    val context = LocalContext.current
    val authRepo = remember { AuthRepository(context) }
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var scrollState = androidx.compose.foundation.rememberScrollState()
    
    LaunchedEffect(Unit) {
        // Reputation counts (green/red dots) are kept up to date by the deal functions in Supabase
        user = authRepo.getCurrentUserProfile().getOrNull()
        isLoading = false
    }
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }


    val hapticEnabled by SettingsManager.hapticEnabled.collectAsState()
    val themeMode by SettingsManager.themeMode.collectAsState()
    val notificationsEnabled by SettingsManager.notificationsEnabled.collectAsState()

    var isCardExpanded by remember { mutableStateOf(false) }


    var isContactDevExpanded by remember { mutableStateOf(expandContactDev) }
    var isFeedbackExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scrollState.scrollTo(0)
        isCardExpanded = false
        isContactDevExpanded = expandContactDev
        isFeedbackExpanded = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Profile Card
            if (user != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user!!.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Name & Block
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user!!.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Block: " + user!!.block,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Expanded Info
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Phone Number: " + user!!.phone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Successful Deals: " + user!!.greenDots,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(Color(0xFF4CAF50))
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Cancelled Deals: " + user!!.redDots,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(Color(0xFFF44336))
                                )
                            }
                        }
                    }
                }
            } else if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("Profile not found or incomplete. Try logging out.", color = MaterialTheme.colorScheme.error)
                }
            }

            // Options List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {


                // Theme
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Text("Theme", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("System", "Light", "Dark").forEachIndexed { mode, label ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = { SettingsManager.setThemeMode(mode) },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                // Deal notifications
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { SettingsManager.setNotificationsEnabled(!notificationsEnabled) }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Text("Deal Notifications", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { SettingsManager.setNotificationsEnabled(it) }
                    )
                }

                // Haptic Feedback
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { SettingsManager.setHapticEnabled(!hapticEnabled) }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Text("Haptic Feedback", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    }
                    Switch(
                        checked = hapticEnabled,
                        onCheckedChange = { SettingsManager.setHapticEnabled(it) }
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                ExpandableProfileOption(
                    icon = Icons.Default.Code, 
                    text = "Contact Developer",
                    isExpanded = isContactDevExpanded,
                    onExpandedChange = { isContactDevExpanded = it }
                ) {
                    Text("Email: support@swych.app", style = MaterialTheme.typography.bodyMedium)
                }
                ExpandableProfileOption(
                    icon = Icons.Default.Feedback, 
                    text = "Feedback",
                    isExpanded = isFeedbackExpanded,
                    onExpandedChange = { isFeedbackExpanded = it }
                ) {
                    Text("We'd love to hear your thoughts! Drop us a review.", style = MaterialTheme.typography.bodyMedium)
                }
                ExpandableProfileOption(icon = Icons.AutoMirrored.Filled.HelpOutline, text = "FAQs") {
                    Text(
                        "Q: How do I buy something?\n" +
                            "A: Tap Deal on an item (or make your own offer on its page). The seller sees it in their Deals tab.\n\n" +
                            "Q: What happens when my offer is accepted?\n" +
                            "A: The item is reserved for you and a Call button appears. Meet up, exchange the item, then both of you tap Done.\n\n" +
                            "Q: What if we can't meet?\n" +
                            "A: Either side can Cancel the deal and the item goes back on sale. Cancelling adds a red dot to your profile. Deals nobody confirms expire after 3 days.\n\n" +
                            "Q: What do the dots mean?\n" +
                            "A: Green = completed deals, red = deals you cancelled.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                ExpandableProfileOption(icon = Icons.Default.Gavel, text = "Legal Info") {
                    Text(
                        "Your phone number is only shared with the other person once a deal is accepted. " +
                            "You can delete your account and all your data at any time below.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Logout Button
                Button(
                    onClick = { showLogoutDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                TextButton(
                    onClick = { showDeleteDialog = true },
                    enabled = !isDeleting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete account", color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Log Out") },
                text = { Text("Are you sure you want to log out?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            scope.launch {
                                authRepo.logout()
                                navController.navigate(AuthRoute) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    ) { Text("Log Out", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete account?") },
                text = { Text("This permanently deletes your account, your listings and your deal history. It can't be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            isDeleting = true
                            scope.launch {
                                authRepo.deleteAccount()
                                    .onSuccess {
                                        navController.navigate(AuthRoute) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                    .onFailure { e ->
                                        isDeleting = false
                                        Toast.makeText(context, e.userMessage(), Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                    ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun ExpandableProfileOption(
    icon: ImageVector,
    text: String,
    isExpanded: Boolean = false,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var internalExpanded by remember { mutableStateOf(false) }
    val expanded = if (onExpandedChange != null) isExpanded else internalExpanded
    
    val toggle = {
        if (onExpandedChange != null) {
            onExpandedChange(!isExpanded)
        } else {
            internalExpanded = !internalExpanded
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (expanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.Transparent)
            .clickable(onClick = toggle)
            .padding(vertical = 14.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            }
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        androidx.compose.animation.AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 40.dp)
            ) {
                content()
            }
        }
    }
}

