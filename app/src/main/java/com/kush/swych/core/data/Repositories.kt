package com.kush.swych.core.data

import com.kush.swych.core.model.Deal
import com.kush.swych.core.model.Item
import com.kush.swych.core.model.User
import kotlinx.coroutines.flow.StateFlow

/** Whether a user is signed in, from the app's point of view. */
enum class SessionState { Loading, SignedIn, SignedOut }

/** What happens after a successful sign-up. */
enum class SignUpOutcome {
    /** Signed in straight away (email confirmation is off). */
    SignedIn,
    /** Account created; the student must open the link in their email, then log in. */
    ConfirmEmail
}

interface AuthRepository {
    val sessionState: StateFlow<SessionState>
    val currentUserUid: String?

    /** Waits until a saved session (if any) has been loaded from storage. */
    suspend fun awaitInitialized()

    suspend fun registerUser(name: String, block: String, phone: String, email: String, password: String): Result<SignUpOutcome>
    suspend fun loginUser(phoneOrEmail: String, password: String): Result<Unit>
    suspend fun getCurrentUserProfile(): Result<User>

    /** Public profiles by id; refetches when someone new (e.g. a user who just signed up) is missing. */
    suspend fun getUsers(ids: Collection<String>, forceRefresh: Boolean = false): Map<String, User>

    /** Emails a one-time link to [vitEmail]; opening it adds the VIT badge to this account. */
    suspend fun requestVitVerification(vitEmail: String): Result<Unit>

    suspend fun logout()
    suspend fun deleteAccount(): Result<Unit>
}

interface ItemRepository {
    suspend fun postItem(title: String, description: String, price: Double, category: String, photoUri: String): Result<Unit>

    /** Newest first. */
    suspend fun getAllItems(forceRefresh: Boolean = false): Result<List<Item>>
    suspend fun getItemById(itemId: String): Result<Item>
    suspend fun getMyItems(): Result<List<Item>>
    suspend fun updateItem(itemId: String, title: String, description: String, price: Double): Result<Unit>
    suspend fun deleteItem(itemId: String): Result<Unit>
    fun clearCache()
}

/**
 * Offers and deals. Creating an offer is a plain insert (checked by row level security);
 * every later status change runs in a Supabase function so it happens in one transaction.
 */
interface DealRepository {
    suspend fun createDeal(itemId: String, itemTitle: String, itemPhotoUrl: String, sellerId: String, agreedPrice: Double): Result<Unit>

    /** Seller accepts: deal ACCEPTED, item LOCKED, other offers REJECTED. */
    suspend fun acceptDeal(dealId: String): Result<Unit>
    suspend fun rejectDeal(dealId: String): Result<Unit>

    /** Buyer withdraws a pending offer, or either side backs out of an accepted deal. */
    suspend fun cancelDeal(dealId: String): Result<Unit>

    /** Confirms the hand-over; the deal completes once both sides have confirmed. */
    suspend fun confirmDeal(dealId: String): Result<Unit>

    /** Removes a rejected, cancelled or expired deal from the list. */
    suspend fun dismissDeal(dealId: String): Result<Unit>

    /** Deals where the signed-in user is buyer or seller. */
    suspend fun getMyDeals(expireStale: Boolean = true): Result<List<Deal>>

    /** The other side's phone number, available once the deal is accepted. */
    suspend fun getContactPhone(dealId: String): Result<String?>
}
