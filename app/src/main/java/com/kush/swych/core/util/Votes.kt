package com.kush.swych.core.util

import com.kush.swych.core.model.User

/** Tapping the arrow you already picked takes the vote back; otherwise it becomes the new vote. */
fun nextVote(current: Int, tapped: Int): Int = if (current == tapped) 0 else tapped

/** The seller's totals after one person's vote on a listing changed from [old] to [new]. */
fun User.withVoteChange(old: Int, new: Int): User = copy(
    upvotes = (upvotes - (if (old == 1) 1 else 0) + (if (new == 1) 1 else 0)).coerceAtLeast(0),
    downvotes = (downvotes - (if (old == -1) 1 else 0) + (if (new == -1) 1 else 0)).coerceAtLeast(0)
)
