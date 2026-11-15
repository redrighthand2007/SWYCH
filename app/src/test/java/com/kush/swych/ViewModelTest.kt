package com.kush.swych

import android.content.ContextWrapper
import com.kush.swych.core.data.SignUpOutcome
import com.kush.swych.core.model.Category
import com.kush.swych.core.model.Deal
import com.kush.swych.core.model.DealStatus
import com.kush.swych.core.model.Item
import com.kush.swych.core.model.ItemStatus
import com.kush.swych.core.model.User
import com.kush.swych.testing.FakeAuthRepository
import com.kush.swych.testing.FakeDealRepository
import com.kush.swych.testing.FakeItemRepository
import com.kush.swych.testing.MainDispatcherRule
import com.kush.swych.ui.auth.LoginViewModel
import com.kush.swych.ui.auth.SignUpViewModel
import com.kush.swych.ui.browse.BrowseViewModel
import com.kush.swych.ui.browse.LocationFilter
import com.kush.swych.ui.browse.SortOption
import com.kush.swych.ui.deals.DealsViewModel
import com.kush.swych.ui.postitem.PostItemViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private fun item(id: String, seller: String, price: Double, status: String = ItemStatus.OPEN, category: String = "SNACKS") =
        Item(id = id, sellerId = seller, title = id, price = price, status = status, category = category)

    // --- Browse -------------------------------------------------------------------------

    @Test
    fun browseShowsOnlyOtherSellersOpenItems() {
        val items = listOf(
            item("mine", seller = "me", price = 10.0),
            item("sold", seller = "bob", price = 10.0, status = ItemStatus.SOLD),
            item("reserved", seller = "bob", price = 10.0, status = ItemStatus.LOCKED),
            item("open", seller = "bob", price = 10.0)
        )
        val visible = BrowseViewModel.filterItems(items, "", LocationFilter.CAMPUS, SortOption.RECENT, "me", null, emptyMap())
        assertEquals(listOf("open"), visible.map { it.id })
    }

    @Test
    fun browseFiltersByCategoryHostelAndSortsByPrice() {
        val me = User(uid = "me", block = "MH-A")
        val users = mapOf("a" to User(uid = "a", block = "MH-A"), "b" to User(uid = "b", block = "MH-B"))
        val items = listOf(
            item("pricey", seller = "a", price = 90.0),
            item("cheap", seller = "a", price = 10.0),
            item("other-block", seller = "b", price = 5.0),
            item("book", seller = "a", price = 1.0, category = "BOOKS")
        )
        val visible = BrowseViewModel.filterItems(items, "SNACKS", LocationFilter.HOSTEL, SortOption.LOW_TO_HIGH, "me", me, users)
        assertEquals(listOf("cheap", "pricey"), visible.map { it.id })
    }

    @Test
    fun browseMarksItemAsAppliedAfterOffer() {
        val target = item("open", seller = "bob", price = 50.0)
        val deals = FakeDealRepository()
        val vm = BrowseViewModel(FakeAuthRepository(), FakeItemRepository(listOf(target)), deals)

        vm.sendOffer(target)

        assertTrue("open" in vm.appliedItemIds)
        assertEquals(listOf("create:open:50.0"), deals.calls)
        assertNull(vm.pendingOffer)
    }

    // --- Post ---------------------------------------------------------------------------

    @Test
    fun postRejectsIncompleteListingWithoutCallingServer() {
        val items = FakeItemRepository()
        val vm = PostItemViewModel(ContextWrapper(null), items)

        vm.onTitleChange("  Kettle")
        vm.onPriceChange("12ab0")
        vm.submit()

        assertEquals("Kettle", vm.title)
        assertEquals("120", vm.price)
        assertTrue(vm.categoryError)
        assertTrue(vm.imageError)
        assertFalse(vm.titleError)
        assertTrue(items.posted.isEmpty())
    }

    @Test
    fun postDraftKeepsValuesUntilSubmitted() {
        val vm = PostItemViewModel(ContextWrapper(null), FakeItemRepository())
        vm.onTitleChange("Cycle")
        vm.onCategoryChange(Category.CYCLES)
        vm.description = "Barely used"
        // The same ViewModel instance is reused when the user comes back to the tab
        assertEquals("Cycle", vm.title)
        assertEquals(Category.CYCLES, vm.category)
        assertEquals("Barely used", vm.description)
    }

    // --- Auth ---------------------------------------------------------------------------

    @Test
    fun loginNeedsBothFields() {
        val auth = FakeAuthRepository()
        val vm = LoginViewModel(auth)
        vm.email = "someone@vitstudent.ac.in"
        vm.login()
        assertEquals("Please enter both email and password.", vm.errorMessage)
        assertEquals(0, auth.loginCalls)
    }

    @Test
    fun loginShowsFriendlyErrorOnWrongPassword() {
        val auth = FakeAuthRepository(loginResult = Result.failure(Exception("Invalid login credentials")))
        val vm = LoginViewModel(auth)
        vm.email = "someone@vitstudent.ac.in"
        vm.password = "secret1"
        vm.login()
        assertEquals("Wrong email/phone or password.", vm.errorMessage)
        assertFalse(vm.isLoading)
    }

    @Test
    fun signUpValidatesCollegeEmailPhoneAndPassword() {
        val auth = FakeAuthRepository()
        val vm = SignUpViewModel(auth)
        vm.name = "Kush"
        vm.hostelBlock = "MH-R"
        vm.onPhoneChange("98ab7654321999")
        vm.email = "kush@gmail.com"
        vm.onPasswordChange("abc123")
        vm.confirmPassword = "abc123"

        vm.register()
        assertEquals("9876543219", vm.phone)
        assertFalse(vm.isEmailValid)
        assertEquals(0, auth.registerCalls)

        vm.email = " Kush@VITstudent.ac.in "
        vm.register()
        assertEquals(1, auth.registerCalls)
    }

    @Test
    fun signUpAsksToCheckInboxWhenConfirmationIsOn() {
        val auth = FakeAuthRepository().apply { signUpOutcome = SignUpOutcome.ConfirmEmail }
        val vm = SignUpViewModel(auth)
        vm.name = "Asha"
        vm.hostelBlock = "MH-A"
        vm.onPhoneChange("9876543210")
        vm.email = "Asha@VITstudent.ac.in"
        vm.onPasswordChange("abc123")
        vm.confirmPassword = "abc123"

        vm.register()

        assertEquals("asha@vitstudent.ac.in", vm.confirmationSentTo)
        assertNull(vm.errorMessage)
    }

    // --- Deals --------------------------------------------------------------------------

    @Test
    fun acceptingAnOfferCallsServerAndShowsMessage() {
        val deal = Deal(id = "d1", itemId = "i1", buyerId = "bob", sellerId = "me", status = DealStatus.PENDING)
        val deals = FakeDealRepository(listOf(deal))
        val vm = DealsViewModel(FakeAuthRepository(), FakeItemRepository(), deals)

        vm.acceptDeal("d1")

        assertEquals(listOf("accept:d1"), deals.calls)
        assertEquals("Offer accepted. Call the buyer to meet up.", vm.message)
        assertTrue(vm.busyIds.isEmpty())
    }

    @Test
    fun failedActionShowsReadableErrorNotRawDatabaseText() {
        val deals = FakeDealRepository().apply {
            actionResult = Result.failure(Exception("duplicate key value violates unique constraint \"deals_one_live_offer\""))
        }
        val vm = DealsViewModel(FakeAuthRepository(), FakeItemRepository(), deals)
        vm.cancelDeal("d1")
        assertEquals("You already have an offer on this item.", vm.message)
    }
}
