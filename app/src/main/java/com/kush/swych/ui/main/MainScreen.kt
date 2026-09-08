package com.kush.swych.ui.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
    var expandContactDev by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dealRepo = remember { DealRepository(context) }

    var browseSortName by remember { mutableStateOf("RECENT") }
    var browseLocationName by remember { mutableStateOf("CAMPUS") }

    LaunchedEffect(Unit) {
        while(isActive) {
            try {
                val result = dealRepo.getMyDeals()
                hasPendingDeals = result.getOrNull()?.any { it.status == "PENDING" } == true
            } catch (_: Exception) {
                // Silently ignore polling errors
            }
            delay(10000)
        }
    }

    BackHandler(enabled = selectedTabIndex != 0) {
        selectedTabIndex = 0
        expandContactDev = false
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
                        if (index != 4) {
                            expandContactDev = false
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = {
                    (slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { fullWidth -> if (targetState > initialState) fullWidth else -fullWidth }
                    ) + fadeIn(animationSpec = tween(300))) togetherWith (slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { fullWidth -> if (targetState > initialState) -fullWidth else fullWidth }
                    ) + fadeOut(animationSpec = tween(300)))
                },
                label = "tab_transition"
            ) { targetIndex ->
                when (targetIndex) {
                    0 -> HomeContent(
                        navController = navController,
                        onCategoryClick = { category ->
                            browseCategory = category
                            selectedTabIndex = 1
                        },
                        onNavigateToProfile = {
                            expandContactDev = true
                            selectedTabIndex = 4
                        }
                    )
                    1 -> BrowseContent(
                        navController = navController,
                        category = browseCategory,
                        onCategoryChange = { browseCategory = it },
                        sortName = browseSortName,
                        onSortChange = { browseSortName = it },
                        locationName = browseLocationName,
                        onLocationChange = { browseLocationName = it }
                    )
                    2 -> com.kush.swych.ui.postitem.PostItemScreen(navController = navController, onNavigateHome = { selectedTabIndex = 0 })
                    3 -> com.kush.swych.ui.deals.DealsScreen(navController = navController, onNavigateToMainTab = { selectedTabIndex = it })
                    4 -> ProfileScreen(
                        navController = navController,
                        onNavigateToMainTab = { tabIndex -> selectedTabIndex = tabIndex },
                        expandContactDev = expandContactDev
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp), // slightly smaller height for pill
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0.dp) // Remove default insets since we handle them outside
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
}





