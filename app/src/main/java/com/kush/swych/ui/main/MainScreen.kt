package com.kush.swych.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kush.swych.ui.home.HomeContent
import com.kush.swych.ui.browse.BrowseContent

import com.kush.swych.ui.profile.ProfileScreen

private data class NavTab(val label: String, val emoji: String)

private val navTabs = listOf(
    NavTab("Home", "????"),
    NavTab("Browse", "????"),
    NavTab("Sell", "???"),
    NavTab("Deals", "????"),
    NavTab("Profile", "????")
)

@Composable
fun MainScreen(
    navController: NavController,
    initialTab: Int = 0,
    initialBrowseCategory: String = ""
) {
    var selectedTabIndex by remember { mutableIntStateOf(initialTab) }
    var browseCategory by remember { mutableStateOf(initialBrowseCategory) }
    var hasNotification by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            SwychTopBar(
                hasNotification = hasNotification,
                onNotificationClick = {
                    hasNotification = false
                    navController.navigate(com.kush.swych.ui.navigation.GlobalOffersRoute)
                },
                onTitleClick = {
                    selectedTabIndex = 0 // Home
                }
            )
        },
        bottomBar = {
            SwychBottomBar(
                selectedIndex = selectedTabIndex,
                onTabSelected = { index ->
                    selectedTabIndex = index
                    if (index == 1 && browseCategory.isNotEmpty()) {
                        browseCategory = ""
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> HomeContent(
                    navController = navController,
                    onCategoryClick = { category ->
                        browseCategory = category
                        selectedTabIndex = 1
                    }
                )
                1 -> BrowseContent(
                    navController = navController,
                    initialCategory = browseCategory
                )
                2 -> com.kush.swych.ui.postitem.PostItemScreen(navController = navController, onNavigateHome = { selectedTabIndex = 0 })
                3 -> Text("Deals")
                4 -> ProfileScreen(
                    navController = navController,
                    onNavigateToMainTab = { tabIndex -> selectedTabIndex = tabIndex }
                )
            }
        }
    }
}

// ????????? Top Bar ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

@Composable
private fun SwychTopBar(
    hasNotification: Boolean,
    onNotificationClick: () -> Unit,
    onTitleClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { onTitleClick() }
            ) {
                Text(
                    text = "Cache",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Deal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-0.5).sp
                )
            }

            Box {
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Deals/Offers",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                if (hasNotification) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF5252))
                    )
                }
            }
        }
    }
}

// ????????? Bottom Navigation Bar ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

@Composable
private fun SwychBottomBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background, // Match top bar background
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            navTabs.forEachIndexed { index, tab ->
                val isSelected = selectedIndex == index
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "nav_scale_$index"
                )
                val labelColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "nav_label_$index"
                )

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(index) },
                    icon = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = slideInVertically { it } + fadeIn(),
                                exit = slideOutVertically { it } + fadeOut()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(32.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                            if (!isSelected) Spacer(Modifier.height(3.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = tab.emoji,
                                fontSize = (20 * iconScale).sp
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = labelColor
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ),
                    alwaysShowLabel = true
                )
            }
        }
    }
}












