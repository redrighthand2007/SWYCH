package com.kush.swych.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.kush.swych.MainActivity
import com.kush.swych.R
import com.kush.swych.core.model.Deal
import com.kush.swych.core.model.DealStatus

/**
 * Compares the user's deals with what was seen last time and posts a notification
 * for every change the user should hear about.
 */
object DealNotifier {
    private const val CHANNEL_ID = "deals"
    private const val PREFS = "deal_notifications"
    private const val KEY_SEEN = "seen" // "dealId=status:buyerConfirmed:sellerConfirmed" entries
    private const val KEY_PRIMED = "primed"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL_ID, "Offers and deals", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "New offers, accepted or rejected offers and deal updates"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Records the current state without notifying, e.g. right after login. */
    fun prime(context: Context, deals: List<Deal>) {
        prefs(context).edit {
            putStringSet(KEY_SEEN, deals.map(::fingerprint).toSet())
            putBoolean(KEY_PRIMED, true)
        }
    }

    fun clear(context: Context) {
        prefs(context).edit { clear() }
    }

    fun processChanges(context: Context, deals: List<Deal>, myUid: String) {
        val prefs = prefs(context)
        if (!prefs.getBoolean(KEY_PRIMED, false)) {
            // First run: everything already there is old news
            prime(context, deals)
            return
        }
        val previous = prefs.getStringSet(KEY_SEEN, emptySet()).orEmpty()
            .associate { it.substringBefore('=') to it.substringAfter('=') }

        for (deal in deals) {
            val before = previous[deal.id]
            if (before == fingerprint(deal).substringAfter('=')) continue
            val message = describeChange(deal, before, myUid) ?: continue
            notify(context, deal.id.hashCode(), message.first, message.second)
        }
        prefs.edit { putStringSet(KEY_SEEN, deals.map(::fingerprint).toSet()) }
    }

    private fun describeChange(deal: Deal, before: String?, myUid: String): Pair<String, String>? {
        val isSeller = deal.sellerId == myUid
        val price = "₹%.0f".format(deal.finalPrice)
        val previousStatus = before?.substringBefore(':')
        return when {
            before == null && deal.status == DealStatus.PENDING && isSeller ->
                "New offer" to "Someone offered $price for ${deal.itemTitle}."
            previousStatus == deal.status && deal.status == DealStatus.ACCEPTED -> {
                // Only the confirmations changed
                val theyConfirmed = if (isSeller) deal.buyerConfirmed else deal.sellerConfirmed
                if (theyConfirmed) "Confirm the hand-over" to "The ${if (isSeller) "buyer" else "seller"} marked ${deal.itemTitle} as done. Tap Done once you've exchanged it."
                else null
            }
            deal.status == DealStatus.ACCEPTED && !isSeller ->
                "Offer accepted 🎉" to "Your $price offer for ${deal.itemTitle} was accepted. Call the seller to meet up."
            deal.status == DealStatus.REJECTED && !isSeller ->
                "Offer not accepted" to "Your offer for ${deal.itemTitle} wasn't accepted."
            deal.status == DealStatus.COMPLETED ->
                "Deal completed ✅" to "${deal.itemTitle} is done. Thanks for swapping!"
            deal.status == DealStatus.CANCELLED && deal.cancelledBy != myUid ->
                "Deal cancelled" to "The deal for ${deal.itemTitle} was cancelled."
            deal.status == DealStatus.EXPIRED ->
                "Deal expired" to "The deal for ${deal.itemTitle} expired after 3 days without confirmation."
            else -> null
        }
    }

    private fun notify(context: Context, id: Int, title: String, text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // Permission revoked between the check and the call
        }
    }

    private fun fingerprint(deal: Deal) = "${deal.id}=${deal.status}:${deal.buyerConfirmed}:${deal.sellerConfirmed}"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
