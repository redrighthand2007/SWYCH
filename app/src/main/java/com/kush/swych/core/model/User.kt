package com.kush.swych.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String = "",
    val phone: String = "",
    val email: String = "",
    val name: String = "",
    val block: String = "",
    /** Completed deals. */
    @SerialName("green_dots")
    val greenDots: Int = 0,
    /** Deals this person cancelled. */
    @SerialName("red_dots")
    val redDots: Int = 0,
    /** Votes from other students across all of this person's listings. */
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    /** Confirmed a @vitstudent.ac.in email; shown as the golden V badge. */
    @SerialName("vit_verified")
    val vitVerified: Boolean = false,
    /** The confirmed VIT email; only filled in on the user's own profile. */
    @SerialName("vit_email")
    val vitEmail: String? = null
)
