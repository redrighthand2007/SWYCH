package com.kush.swych.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kush.swych.core.model.Item

@Composable
fun ItemCard(
    item: Item,
    sellerName: String = "",
    sellerBlock: String,
    dealsMade: Int = 0,
    dealsExpired: Int = 0,
    isOwnItem: Boolean,
    onClick: () -> Unit,
    onDealClick: () -> Unit,
    isApplied: Boolean = false,
    onRemoveClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    bottomActions: (@Composable () -> Unit)? = null
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .height(120.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left 30%: Image
            Box(modifier = Modifier.weight(0.3f).fillMaxHeight()) {
                AsyncImage(
                    model = item.photoUrl?.takeIf { it.isNotBlank() },
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Right 70%: Details
            Column(
                modifier = Modifier
                    .weight(0.7f)
                    .padding(12.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // First line: name, block, price
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "${item.title} • $sellerBlock",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "₹%.0f".format(item.price),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    // Second line: name of seller
                    if (sellerName.isNotEmpty()) {
                        Text(
                            text = sellerName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    
                    // Third line: Green/Red dots
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF4CAF50)))
                        Spacer(Modifier.width(4.dp))
                        Text("$dealsMade", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(12.dp))
                        Box(modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFFF44336)))
                        Spacer(Modifier.width(4.dp))
                        Text("$dealsExpired", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                // Bottom Row for Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (bottomActions != null) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            bottomActions()
                        }
                    } else {
                        if (!isOwnItem) {
                            val isPending = item.status.uppercase() == "PENDING"
                            val buttonDisabled = isApplied || isPending
                            Button(
                                onClick = onDealClick,
                                enabled = !buttonDisabled,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(
                                    text = if (isApplied) "Applied" else if (isPending) "Pending" else "Deal",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Spacer(Modifier.weight(1f))
                        }

                        if (isOwnItem) {
                            androidx.compose.material3.IconButton(
                                onClick = onRemoveClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove Item",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}










