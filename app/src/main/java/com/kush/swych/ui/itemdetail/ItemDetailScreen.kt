package com.kush.swych.ui.itemdetail

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.kush.swych.core.data.AuthRepository
import com.kush.swych.core.data.DealRepository
import com.kush.swych.core.data.ItemRepository
import com.kush.swych.core.designsystem.component.shimmerEffect
import com.kush.swych.core.model.Item
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(navController: NavController, itemId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val itemRepo = remember { ItemRepository(context) }
    val dealRepo = remember { DealRepository(context) }
    val authRepo = remember { AuthRepository(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var item by remember { mutableStateOf<Item?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var offerAmount by remember { mutableStateOf("") }
    var isSubmittingOffer by remember { mutableStateOf(false) }
    var offerSubmitted by remember { mutableStateOf(false) }

    val currentUid = authRepo.currentUserUid

    LaunchedEffect(itemId) {
        val result = itemRepo.getItemById(itemId)
        item = result.getOrNull()
        isLoading = false
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        if (isLoading) {
            // Shimmer skeleton
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .shimmerEffect()
                )
                Spacer(Modifier.height(16.dp))
                Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.fillMaxWidth(0.5f).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Box(Modifier.fillMaxWidth(0.8f).height(32.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Box(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Box(Modifier.fillMaxWidth(0.6f).height(14.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                }
            }
        } else if (item == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("😕", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Item not found", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { navController.popBackStack() }) { Text("Go Back") }
                }
            }
        } else {
            val it = item!!
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // — Parallax Image —
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        AsyncImage(
                            model = it.photoUrl?.ifBlank { null },
                            contentDescription = it.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                                        startY = 150f
                                    )
                                )
                        )
                        // Back button
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .padding(12.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }

                    // — Content —
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Category chip
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = it.category,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        // Title
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Price
                        Text(
                            text = "₹${it.price.toInt()}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Status badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (it.status.uppercase()) {
                                "OPEN" -> Color(0xFF1B5E20).copy(alpha = 0.15f)
                                else -> Color.Gray.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = it.status.replaceFirstChar { c -> c.uppercase() },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = when (it.status.uppercase()) {
                                    "OPEN" -> Color(0xFF4CAF50)
                                    else -> Color.Gray
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Divider()

                        // Description
                        if (!it.description.isNullOrBlank()) {
                            Text(
                                text = "About this item",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                  text = it.description ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Divider()
                        }

                        // — Make an Offer / Own listing —
                        if (it.status.uppercase() == "OPEN" && it.sellerId != currentUid) {
                            // Make an offer section
                            Text(
                                text = "Make an Offer 🤝",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (offerSubmitted) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1B5E20).copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text("✅", fontSize = 24.sp)
                                        Text(
                                            "Deal initiated! The seller will see your offer.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                }
                            } else {
                                OutlinedTextField(
                                    value = offerAmount,
                                    onValueChange = { v -> offerAmount = v.filter { c -> c.isDigit() || c == '.' } },
                                    label = { Text("Your Offer Price (₹)") },
                                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        val offerPrice = offerAmount.toDoubleOrNull()
                                        if (offerPrice == null || offerPrice <= 0) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Enter a valid offer amount")
                                            }
                                            return@Button
                                        }
                                        isSubmittingOffer = true
                                        scope.launch {
                                            val result = dealRepo.createDeal(
                                                itemId = it.id,
                                                sellerId = it.sellerId,
                                                itemTitle = it.title,
                                                itemPhotoUrl = it.photoUrl ?: "",
                                                agreedPrice = offerPrice
                                            )
                                            isSubmittingOffer = false
                                            if (result.isSuccess) {
                                                offerSubmitted = true
                                            } else {
                                                snackbarHostState.showSnackbar(
                                                    result.exceptionOrNull()?.message ?: "Failed to submit offer"
                                                )
                                            }
                                        }
                                    },
                                    enabled = !isSubmittingOffer,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isSubmittingOffer) {
                                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                                    } else {
                                        Text("Submit Deal 🤝", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Gray.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    "This item is no longer available.",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}



