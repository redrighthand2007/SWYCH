package com.kush.swych.testing

import com.kush.swych.core.data.AuthRepository
import com.kush.swych.core.data.DealRepository
import com.kush.swych.core.data.ItemRepository
import com.kush.swych.core.data.SessionState
import com.kush.swych.core.model.Deal
import com.kush.swych.core.model.Item
import com.kush.swych.core.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.Dispatchers
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Runs viewModelScope coroutines immediately on the test thread. */
class MainDispatcherRule(private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeAuthRepository(
    override var currentUserUid: String? = "me",
    var profile: User = User(uid = "me", name = "Me", block = "MH-A"),
    var users: Map<String, User> = emptyMap(),
    var loginResult: Result<Unit> = Result.success(Unit)
) : AuthRepository {
    override val sessionState = MutableStateFlow(SessionState.SignedIn)
    var loginCalls = 0
    var registerCalls = 0

    override suspend fun awaitInitialized() = Unit
    override suspend fun registerUser(name: String, block: String, phone: String, email: String, password: String): Result<Unit> {
        registerCalls++
        return Result.success(Unit)
    }
    override suspend fun loginUser(phoneOrEmail: String, password: String): Result<Unit> {
        loginCalls++
        return loginResult
    }
    override suspend fun getCurrentUserProfile() = Result.success(profile)
    override suspend fun getUsers(ids: Collection<String>, forceRefresh: Boolean) = users
    override suspend fun logout() = Unit
    override suspend fun deleteAccount() = Result.success(Unit)
}

class FakeItemRepository(var items: List<Item> = emptyList()) : ItemRepository {
    var posted = mutableListOf<String>()

    override suspend fun postItem(title: String, description: String, price: Double, category: String, photoUri: String): Result<Unit> {
        posted += title
        return Result.success(Unit)
    }
    override suspend fun getAllItems(forceRefresh: Boolean) = Result.success(items)
    override suspend fun getItemById(itemId: String) =
        items.firstOrNull { it.id == itemId }?.let { Result.success(it) } ?: Result.failure(Exception("Not found"))
    override suspend fun getMyItems() = Result.success(items.filter { it.sellerId == "me" })
    override suspend fun updateItem(itemId: String, title: String, description: String, price: Double) = Result.success(Unit)
    override suspend fun deleteItem(itemId: String) = Result.success(Unit)
    override fun clearCache() = Unit
}

class FakeDealRepository(var deals: List<Deal> = emptyList()) : DealRepository {
    val calls = mutableListOf<String>()
    var actionResult: Result<Unit> = Result.success(Unit)

    override suspend fun createDeal(itemId: String, itemTitle: String, itemPhotoUrl: String, sellerId: String, agreedPrice: Double): Result<Unit> {
        calls += "create:$itemId:$agreedPrice"
        // Like the server: a successful offer shows up in the buyer's deals
        if (actionResult.isSuccess) {
            deals = deals + Deal(id = "deal_$itemId", itemId = itemId, buyerId = "me", sellerId = sellerId, finalPrice = agreedPrice)
        }
        return actionResult
    }
    override suspend fun acceptDeal(dealId: String) = record("accept:$dealId")
    override suspend fun rejectDeal(dealId: String) = record("reject:$dealId")
    override suspend fun cancelDeal(dealId: String) = record("cancel:$dealId")
    override suspend fun confirmDeal(dealId: String) = record("confirm:$dealId")
    override suspend fun dismissDeal(dealId: String) = record("dismiss:$dealId")
    override suspend fun getMyDeals(expireStale: Boolean) = Result.success(deals)
    override suspend fun getContactPhone(dealId: String) = Result.success<String?>("9999999999")

    private fun record(call: String): Result<Unit> {
        calls += call
        return actionResult
    }
}
