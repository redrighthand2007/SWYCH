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
            .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left ~1/3 Image
            Box(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            ) {
                AsyncImage(
                    model = item.photoUrl?.takeIf { it.isNotBlank() },
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Divider
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)))

            // Right ~2/3 Details
            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .padding(12.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: Item Name and Price Box
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "₹ %.0f".format(item.price),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Middle row: Seller First Name, Block, Dots
                val firstName = sellerName.split(" ").firstOrNull() ?: "Unknown"
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$firstName, $sellerBlock,",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(modifier = Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF4CAF50)))
                    Spacer(Modifier.width(2.dp))
                    Text("$dealsMade", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Box(modifier = Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFFF44336)))
                    Spacer(Modifier.width(2.dp))
                    Text("$dealsExpired", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                // Bottom row: Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (bottomActions != null) {
                        bottomActions()
                    } else {
                        // Browse Screen Default Actions
                        if (!isOwnItem) {
                            val isPending = item.status.uppercase() == "PENDING"
                            val buttonDisabled = isApplied || isPending
                            Button(
                                onClick = onDealClick,
                                enabled = !buttonDisabled,
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = if (isApplied) "Applied" else if (isPending) "Pending" else "Deal",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        } else {
                            // Empty space where Deal button would be
                            Spacer(Modifier.width(60.dp))
                        }
                        
                        Spacer(Modifier.weight(1f))

                        if (isOwnItem) {
                            Button(
                                onClick = onRemoveClick,
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Text("Delete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}










