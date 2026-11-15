package com.kush.swych.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kush.swych.core.model.Item

@Composable
private fun InfoPill(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .height(28.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun ItemCard(
    item: Item,
    sellerName: String = "",
    sellerBlock: String,
    /** Shows the golden V next to the name. */
    sellerVerified: Boolean = false,
    /** Seller's vote totals across all their listings. */
    upvotes: Int = 0,
    downvotes: Int = 0,
    /** This user's vote on this listing: 1, -1 or 0. */
    myVote: Int = 0,
    /** Called with 1 (up) or -1 (down); null shows the totals without buttons. */
    onVote: ((Int) -> Unit)? = null,
    isOwnItem: Boolean,
    onClick: () -> Unit,
    onDealClick: () -> Unit,
    isApplied: Boolean = false,
    onRemoveClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    bottomActions: (@Composable RowScope.() -> Unit)? = null,
    dropdownContent: (@Composable ColumnScope.() -> Unit)? = null
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
            .heightIn(min = 130.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().height(130.dp)) {
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

                // Right ~2/3 Details (Pill Layout)
                Column(
                    modifier = Modifier
                        .weight(0.65f)
                        .padding(8.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Row 1: Item and Price
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        InfoPill(modifier = Modifier.weight(0.6f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        InfoPill(modifier = Modifier.weight(0.4f)) {
                            Text(
                                text = "₹%.0f".format(item.price),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Row 2: Name, Block, Votes
                    val firstName = sellerName.split(" ").firstOrNull() ?: "User"
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        InfoPill(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = firstName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (sellerVerified) {
                                    Spacer(Modifier.width(3.dp))
                                    VitBadge(size = 16.dp)
                                }
                            }
                        }
                        InfoPill(modifier = Modifier.weight(0.8f)) {
                            Text(
                                text = sellerBlock.takeIf { it.isNotBlank() } ?: "N/A",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            modifier = Modifier
                                .weight(1.4f)
                                .height(28.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(50))
                                .padding(horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            VoteButton(
                                icon = Icons.Filled.ThumbUp,
                                label = "Upvote",
                                count = upvotes,
                                color = UpvoteGreen,
                                selected = myVote == 1,
                                onClick = onVote?.let { vote -> { vote(1) } }
                            )
                            VoteButton(
                                icon = Icons.Filled.ThumbDown,
                                label = "Downvote",
                                count = downvotes,
                                color = DownvoteRed,
                                selected = myVote == -1,
                                onClick = onVote?.let { vote -> { vote(-1) } }
                            )
                        }
                    }

                    // Row 3: Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (bottomActions != null) {
                            bottomActions()
                        } else {
                            // Browse Screen Default Actions
                            if (!isOwnItem) {
                                val buttonDisabled = isApplied
                                Button(
                                    onClick = onDealClick,
                                    enabled = !buttonDisabled,
                                    shape = RoundedCornerShape(50),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(28.dp).weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text(
                                        text = if (isApplied) "Applied" else "Deal",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            } else {
                                Button(
                                    onClick = onRemoveClick,
                                    shape = RoundedCornerShape(50),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(28.dp).weight(1f),
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

            if (dropdownContent != null) {
                dropdownContent()
            }
        }
    }
}

private val UpvoteGreen = Color(0xFF2E9E48)
private val DownvoteRed = Color(0xFFE53935)

/** Thumb and count; tappable when [onClick] is set. The picked one gets a tinted background. */
@Composable
private fun VoteButton(
    icon: ImageVector,
    label: String,
    count: Int,
    color: Color,
    selected: Boolean,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) color.copy(alpha = 0.2f) else Color.Transparent)
            .then(
                if (onClick != null) Modifier.clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
                else Modifier
            )
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(3.dp))
        Text(
            "$count",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
