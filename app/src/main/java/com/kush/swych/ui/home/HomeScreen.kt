package com.kush.swych.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kush.swych.core.model.Category
import com.kush.swych.core.model.CategoryGroup
import kotlinx.coroutines.delay

@Composable
fun HomeContent(
    onCategoryClick: (String) -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
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
                    HeaderTicker()
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

        // Category groups, straight from the Category list so Home always matches Browse and Post
        CategoryGroup.entries.forEach { group ->
            val categories = Category.entries.filter { it.group == group }
            if (categories.isEmpty()) return@forEach

            item(key = "group_${group.name}") {
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            categories.chunked(2).forEach { rowItems ->
                item(key = "row_${rowItems.first().name}") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowItems.forEach { category ->
                            Box(modifier = Modifier.weight(1f)) {
                                CategoryBox(
                                    category = category,
                                    onClick = { onCategoryClick(category.name) }
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
    category: Category,
    onClick: () -> Unit
) {
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
            Image(
                painter = painterResource(id = category.iconRes),
                contentDescription = category.displayName,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * The app name, then how a deal works, one step at a time, looping back to the name.
 * Starts again from "Swych" every time the Home tab is opened.
 */
private val tickerSteps = listOf(
    "Swych",
    "Find it on campus",
    "Make your offer",
    "Lock the deal",
    "Meet at a public spot",
    "Swap & done"
)

private const val NAME_HOLD_MILLIS = 2600L
private const val STEP_HOLD_MILLIS = 2000L

@Composable
private fun HeaderTicker() {
    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(if (index == 0) NAME_HOLD_MILLIS else STEP_HOLD_MILLIS)
            index = (index + 1) % tickerSteps.size
        }
    }

    // Fixed height so the cards below don't jump as the text size changes
    Box(modifier = Modifier.height(84.dp), contentAlignment = Alignment.Center) {
        AnimatedContent(
            targetState = index,
            transitionSpec = {
                (slideInVertically(tween(450)) { it / 2 } + fadeIn(tween(450))) togetherWith
                    (slideOutVertically(tween(350)) { -it / 2 } + fadeOut(tween(350)))
            },
            label = "header_ticker"
        ) { step ->
            val isName = step == 0
            Text(
                text = tickerSteps[step],
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = if (isName) FontWeight.Black else FontWeight.ExtraBold,
                    letterSpacing = if (isName) (-1.5).sp else (-0.5).sp,
                    fontSize = if (isName) 64.sp else 28.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
