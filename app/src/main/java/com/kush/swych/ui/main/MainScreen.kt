package com.kush.swych.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.kush.swych.core.data.DealRepository
import com.kush.swych.ui.home.HomeContent
import com.kush.swych.ui.browse.BrowseContent
import com.kush.swych.ui.profile.ProfileScreen

private data class NavTab(val outlinedIcon: ImageVector, val filledIcon: ImageVector)

private val navTabs = listOf(
    NavTab(Icons.Outlined.Home, Icons.Filled.Home),
    NavTab(Icons.Outlined.Search, Icons.Filled.Search),
    NavTab(Icons.Filled.Add, Icons.Filled.Add),
    NavTab(Icons.Outlined.Notifications, Icons.Filled.Notifications),
    NavTab(Icons.Outlined.Person, Icons.Filled.Person)
)

@Composable
fun MainScreen(
    navController: NavController,
    initialTab: Int = 0,
    initialBrowseCategory: String = ""
) {
    var selectedTabIndex by remember { mutableIntStateOf(initialTab) }
    var browseCategory by remember { mutableStateOf(initialBrowseCategory) }
    var hasPendingDeals by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dealRepo = remember { DealRepository(context) }

    LaunchedEffect(Unit) {
        while(isActive) {
            val result = dealRepo.getMyDeals()
            hasPendingDeals = result.getOrNull()?.isNotEmpty() == true
            delay(5000)
        }
    }

    BackHandler(enabled = selectedTabIndex != 0) {
        selectedTabIndex = 0
    }

    val hapticManager = remember { com.kush.swych.core.util.HapticManager(context) }

    Scaffold(
        bottomBar = {
            SwychBottomBar(
                selectedIndex = selectedTabIndex,
                hasPendingDeals = hasPendingDeals,
                onTabSelected = { index ->
                    if (selectedTabIndex != index) {
                        hapticManager.triggerFeedback()
                        selectedTabIndex = index
                        if (index == 1 && browseCategory.isNotEmpty()) {
                            browseCategory = ""
                        }
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
            androidx.compose.animation.AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        androidx.compose.animation.slideInHorizontally { width -> width } + androidx.compose.animation.fadeIn() togetherWith
                        androidx.compose.animation.slideOutHorizontally { width -> -width } + androidx.compose.animation.fadeOut()
                    } else {
                        androidx.compose.animation.slideInHorizontally { width -> -width } + androidx.compose.animation.fadeIn() togetherWith
                        androidx.compose.animation.slideOutHorizontally { width -> width } + androidx.compose.animation.fadeOut()
                    }
                },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
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
                3 -> com.kush.swych.ui.deals.DealsScreen(navController = navController, onNavigateToMainTab = { selectedTabIndex = it })
                4 -> ProfileScreen(
                    navController = navController,
                    onNavigateToMainTab = { tabIndex -> selectedTabIndex = tabIndex }
                )
            }
        }
    }
}
}

// Bottom Navigation Bar ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

@Composable
private fun SwychBottomBar(
    selectedIndex: Int,
    hasPendingDeals: Boolean,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
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

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(index) },
                    icon = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.filledIcon else tab.outlinedIcon,
                                contentDescription = null,
                                modifier = Modifier.size((24 * iconScale).dp),
                                tint = if (index == 3 && hasPendingDeals) Color.Red else if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = Color.Transparent
                    ),
                    alwaysShowLabel = false
                )
            }
        }
    }
}





