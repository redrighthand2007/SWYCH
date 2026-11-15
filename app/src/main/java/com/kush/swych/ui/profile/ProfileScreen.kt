package com.kush.swych.ui.profile

import androidx.compose.foundation.BorderStroke
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kush.swych.core.util.SettingsManager

@Composable
fun ProfileScreen(
    expandContactDev: Boolean = false,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val user = viewModel.user
    val isLoading = viewModel.isLoading
    val isDeleting = viewModel.isDeleting
    val scrollState = androidx.compose.foundation.rememberScrollState()

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(viewModel.message) {
        viewModel.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.message = null
        }
    }

    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val hapticEnabled by SettingsManager.hapticEnabled.collectAsState()
    val themeMode by SettingsManager.themeMode.collectAsState()
    val notificationsEnabled by SettingsManager.notificationsEnabled.collectAsState()

    var isContactDevExpanded by remember { mutableStateOf(expandContactDev) }
    var isFeedbackExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scrollState.scrollTo(0)
        isContactDevExpanded = expandContactDev
        isFeedbackExpanded = false
    }

    // Like the Post screen, the groups spread over the full height with Log Out just above
    // the nav bar. It only scrolls if a long section (like FAQs) is opened.
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .heightIn(min = maxHeight)
                // Same margins as the Post screen; bottom padding clears the floating nav bar
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Profile card
            val profile = user
            if (profile != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Block: ${profile.block}  ·  ${profile.phone}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            ReputationCount(color = Color(0xFF4CAF50), label = "Successful deals", count = profile.greenDots)
                            ReputationCount(color = Color(0xFFF44336), label = "Cancelled", count = profile.redDots)
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

            // Settings
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingRow(icon = Icons.Default.Palette, label = "Theme") {
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
                    SettingRow(
                        icon = Icons.Default.Notifications,
                        label = "Deal Notifications",
                        onClick = { SettingsManager.setNotificationsEnabled(!notificationsEnabled) }
                    ) {
                        Switch(checked = notificationsEnabled, onCheckedChange = { SettingsManager.setNotificationsEnabled(it) })
                    }
                    SettingRow(
                        icon = Icons.Default.Vibration,
                        label = "Haptic Feedback",
                        onClick = { SettingsManager.setHapticEnabled(!hapticEnabled) }
                    ) {
                        Switch(checked = hapticEnabled, onCheckedChange = { SettingsManager.setHapticEnabled(it) })
                    }
                }
            }

            // Help and info
            Column(
                modifier = Modifier.padding(vertical = 6.dp),
                // Gap so opened sections don't run into each other
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ExpandableProfileOption(
                    icon = Icons.Default.Code,
                    text = "Contact Developer",
                    isExpanded = isContactDevExpanded,
                    onExpandedChange = { isContactDevExpanded = it }
                ) {
                    Text("Email: whiterighthand2007@gmail.com", style = MaterialTheme.typography.bodyMedium)
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
            }

            // Account actions
            // Kept low-key on purpose: neither should draw the eye
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Log Out", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
                TextButton(onClick = { showDeleteDialog = true }, enabled = !isDeleting) {
                    Text(
                        "Delete account",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
                        viewModel.logout()
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
                        viewModel.deleteAccount()
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    label: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
        trailing()
    }
}

@Composable
private fun ReputationCount(color: Color, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
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
            .padding(vertical = 11.dp, horizontal = 16.dp)
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
                    .padding(top = 10.dp, start = 40.dp, bottom = 4.dp)
            ) {
                // Readable text colour in both light and dark mode
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                    content()
                }
            }
        }
    }
}
