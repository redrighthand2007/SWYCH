package com.kush.swych.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.kush.swych.core.model.Category
import com.kush.swych.ui.navigation.BrowseRoute
import kotlinx.coroutines.launch

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.ui.graphics.Brush

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable
fun HomeContent(
    navController: NavController,
    onCategoryClick: (String) -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Upper Half: Trendy branding (scrolls away) - FULL SCREEN WIDTH
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Swych",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1.5).sp,
                            fontSize = 64.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text(
                            text = "Swap n' Switch",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Category Groups
        val categoryGroups = listOf(
            "Hungry ?" to listOf("Snacks", "Drinks", "Puffs", "Sweets", "Cooking"),
            "Lock In." to listOf("Books", "Notes", "Stationery", "Courses", "Exams"),
            "Wanna' Chill..." to listOf("Games", "Movies", "Music", "Hub", "Online Games"),
            "Care" to listOf("Meds", "Skincare", "Hygiene"),
            "2nd Hand Items" to listOf("Accessories", "Kettles", "Ext. Boards", "Cycles", "Online -> Cash", "Cash -> Online")
        )

        categoryGroups.forEach { (groupName, categories) ->
            item {
                Text(
                    text = groupName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            categories.chunked(2).forEach { rowItems ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowItems.forEach { category ->
                            Box(modifier = Modifier.weight(1f)) {
                                CategoryBox(
                                    categoryName = category,
                                    onClick = {
                                        val enumName = Category.values().find { it.displayName == category }?.name ?: category
                                        onCategoryClick(enumName)
                                    }
                                )
                            }
                        }
                        if (rowItems.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Footer link
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Suggest more needy categories",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToProfile() }
                )
            }
        }
    }
}

@Composable
fun CategoryBox(
    categoryName: String,
    onClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val formattedName = categoryName.lowercase().replace(" ", "_").replace("-", "_").replace(">", "_").replace(".", "")
    val resId = context.resources.getIdentifier("cat_$formattedName", "drawable", context.packageName)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (resId != 0) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = resId),
                    contentDescription = categoryName,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(modifier = Modifier.size(64.dp).background(Color.Gray, RoundedCornerShape(16.dp)))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}





