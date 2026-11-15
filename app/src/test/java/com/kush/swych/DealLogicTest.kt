package com.kush.swych

import com.kush.swych.core.model.Category
import com.kush.swych.core.model.CategoryGroup
import com.kush.swych.core.model.Deal
import com.kush.swych.core.model.DealStatus
import com.kush.swych.core.util.userMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DealLogicTest {

    @Test
    fun liveDealsBlockAnotherOffer() {
        assertTrue(Deal(status = DealStatus.PENDING).isLive)
        assertTrue(Deal(status = DealStatus.ACCEPTED).isLive)
        assertFalse(Deal(status = DealStatus.COMPLETED).isLive)
        assertFalse(Deal(status = DealStatus.REJECTED).isLive)
    }

    @Test
    fun onlyFinishedUnsuccessfulDealsCanBeDismissed() {
        assertTrue(Deal(status = DealStatus.REJECTED).isClosed)
        assertTrue(Deal(status = DealStatus.CANCELLED).isClosed)
        assertTrue(Deal(status = DealStatus.EXPIRED).isClosed)
        assertFalse(Deal(status = DealStatus.PENDING).isClosed)
        assertFalse(Deal(status = DealStatus.ACCEPTED).isClosed)
        assertFalse(Deal(status = DealStatus.COMPLETED).isClosed)
    }

    @Test
    fun newDealStartsPending() {
        assertEquals(DealStatus.PENDING, Deal().status)
        assertFalse(Deal().buyerConfirmed)
        assertFalse(Deal().sellerConfirmed)
    }

    @Test
    fun databaseErrorsBecomeReadableMessages() {
        assertEquals(
            "You already have an offer on this item.",
            Exception("duplicate key value violates unique constraint \"deals_one_live_offer\"").userMessage()
        )
        assertEquals(
            "This phone number is already registered. Log in, or use a different number.",
            Exception("duplicate key value violates unique constraint \"users_phone_unique\"").userMessage()
        )
        assertEquals(
            "This item is no longer available.",
            Exception("new row violates row-level security policy for table \"deals\"").userMessage()
        )
        assertEquals(
            "Wrong email/phone or password.",
            Exception("Invalid login credentials").userMessage()
        )
        assertEquals(
            "We couldn't send the confirmation email. Please try again in a few minutes.",
            Exception("Error sending confirmation email").userMessage()
        )
    }

    @Test
    fun categoriesAreUniqueAndLookupWorks() {
        val names = Category.entries.map { it.displayName }
        assertEquals(names.size, names.toSet().size)
        assertTrue(CategoryGroup.entries.all { group -> Category.entries.any { it.group == group } })
        assertEquals(Category.SNACKS, Category.fromName("snacks"))
        assertEquals(null, Category.fromName("UNKNOWN"))
    }

    @Test
    fun otherErrorsKeepTheirMessage() {
        assertEquals("No account found with this phone number.", Exception("No account found with this phone number.").userMessage())
        assertEquals("Something went wrong on our side. Please try again in a minute.", RuntimeException().userMessage())
    }

    @Test
    fun rawDatabaseErrorsAreNeverShown() {
        assertEquals(
            "Something went wrong on our side. Please try again in a minute.",
            Exception("null value in column \"title\" of relation \"items\" violates not-null constraint").userMessage()
        )
        assertEquals(
            "Something went wrong on our side. Please try again in a minute.",
            IllegalStateException("Unexpected JSON token at offset 12").userMessage()
        )
    }
}
