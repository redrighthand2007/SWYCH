package com.kush.swych.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.kush.swych.core.util.HapticManager
import com.kush.swych.ui.browse.BrowseContent
import com.kush.swych.ui.browse.BrowseViewModel
import com.kush.swych.ui.deals.DealsScreen
import com.kush.swych.ui.home.HomeContent
import com.kush.swych.ui.postitem.PostItemScreen
import com.kush.swych.ui.profile.ProfileScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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
    viewModel: MainViewModel = hiltViewModel(),
    // Scoped to the main screen, so Browse keeps its items and filters across tab switches
    browseViewModel: BrowseViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val selectedTabIndex = viewModel.selectedTab

    // Ask once for notification permission (Android 13+) so deal updates can be shown
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Checks for new offers every 10 s, only while the app is in the foreground
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                viewModel.checkDeals()
                delay(10_000)
            }
        }
    }

    BackHandler(enabled = selectedTabIndex != MainViewModel.TAB_HOME) {
        viewModel.selectTab(MainViewModel.TAB_HOME)
    }

    val hapticManager = remember { HapticManager(context) }

    // Bottom bar floats over the tabs; each tab adds its own bottom padding for it
    Box(modifier = Modifier.fillMaxSize()) {
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
            label = "tab_transition",
            modifier = Modifier.fillMaxSize()
        ) { targetIndex ->
            when (targetIndex) {
                MainViewModel.TAB_HOME -> HomeContent(
                    onCategoryClick = { category ->
                        browseViewModel.selectCategory(category)
                        viewModel.selectTab(MainViewModel.TAB_BROWSE)
                    },
                    onNavigateToProfile = {
                        viewModel.selectTab(MainViewModel.TAB_PROFILE, expandContact = true)
                    }
                )
                MainViewModel.TAB_BROWSE -> BrowseContent(navController = navController, viewModel = browseViewModel)
                MainViewModel.TAB_POST -> PostItemScreen()
                MainViewModel.TAB_DEALS -> DealsScreen(navController = navController)
                MainViewModel.TAB_PROFILE -> ProfileScreen(expandContactDev = viewModel.expandContactDev)
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            SwychBottomBar(
                selectedIndex = selectedTabIndex,
                hasPendingDeals = viewModel.hasPendingOffers,
                onTabSelected = { index ->
                    if (selectedTabIndex != index) {
                        hapticManager.triggerFeedback()
                        // Opening Browse from the tab bar shows everything, not the last category
                        if (index == MainViewModel.TAB_BROWSE) browseViewModel.selectCategory("")
                        viewModel.selectTab(index)
                    }
                }
            )
        }
    }
}

// Bottom Navigation Bar

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
                .fillMaxWidth(),
            shape = RoundedCornerShape(50),
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
