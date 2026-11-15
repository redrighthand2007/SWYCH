package com.kush.swych.ui.main

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kush.swych.core.data.AuthRepository
import com.kush.swych.core.data.DealRepository
import com.kush.swych.core.model.DealStatus
import com.kush.swych.core.notifications.DealNotifier
import com.kush.swych.core.util.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Tab selection and the "new offer" badge for the main screen. */
@HiltViewModel
class MainViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val savedState: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val dealRepository: DealRepository
) : ViewModel() {

    /** Survives rotation and the app being killed in the background. */
    var selectedTab by mutableStateOf(savedState[KEY_TAB] ?: TAB_HOME)
        private set

    var hasPendingOffers by mutableStateOf(false)
        private set

    /** Opens Profile with "Contact Developer" expanded (from Home's footer link). */
    var expandContactDev by mutableStateOf(false)
        private set

    fun selectTab(tab: Int, expandContact: Boolean = false) {
        selectedTab = tab
        savedState[KEY_TAB] = tab
        expandContactDev = expandContact
    }

    /** Called every few seconds while the app is visible. */
    suspend fun checkDeals() {
        val myUid = authRepository.currentUserUid ?: return
        val deals = dealRepository.getMyDeals(expireStale = false).getOrNull() ?: return
        // Only offers waiting on you as the seller, not your own outgoing ones
        hasPendingOffers = deals.any { it.sellerId == myUid && it.status == DealStatus.PENDING }
        if (SettingsManager.notificationsEnabled.value) {
            DealNotifier.processChanges(context, deals, myUid)
        }
    }

    companion object {
        const val TAB_HOME = 0
        const val TAB_BROWSE = 1
        const val TAB_POST = 2
        const val TAB_DEALS = 3
        const val TAB_PROFILE = 4
        private const val KEY_TAB = "selected_tab"
    }
}
