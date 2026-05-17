package com.pramod.chessmasteroffline.ads

import android.app.Activity

/**
 * Reserved integration point for a future privacy-reviewed AdMob setup.
 * The app intentionally ships with no ad SDK and no network permission.
 */
class AdManagerPlaceholder {
    fun initialize(activity: Activity) = Unit

    fun showInterstitialIfAvailable(activity: Activity, placement: String) = Unit
}
