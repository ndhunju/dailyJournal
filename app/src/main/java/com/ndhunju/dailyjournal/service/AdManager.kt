package com.ndhunju.dailyjournal.service

import android.annotation.SuppressLint
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.android.gms.ads.*
import com.ndhunju.dailyjournal.R
import com.ndhunju.dailyjournal.controller.DailyJournalApplication
import com.ndhunju.dailyjournal.controller.preference.MyPreferenceActivity
import com.ndhunju.dailyjournal.service.AnalyticsService.logEvent
import com.ndhunju.dailyjournal.util.UtilsView

object AdManager {

    init {
        MobileAds.initialize(DailyJournalApplication.getInstance()) {}
    }

    @SuppressLint("MissingPermission") // It is already added
    fun addAdView(
        adViewContainer: FrameLayout?,
        adUnitId: String,
        screenName: String
    ) {
        if (adViewContainer == null) {
            return
        }

        val ps = PreferenceService.from(adViewContainer.context)
        val showAd = ps.getVal(R.string.key_pref_item_ad, 0) == 0

        if (!showAd) {
            adViewContainer.visibility = View.GONE
            return
        }

        val adView = AdView(adViewContainer.context)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId = adUnitId
        adViewContainer.addView(adView)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                val adViewLayoutParams = adView.layoutParams as FrameLayout.LayoutParams
                adViewLayoutParams.gravity = Gravity.START

                // Ad an icon so that user can enable/disable the ads conveniently
                val adSettingsView = ImageView(adViewContainer.context)
                adSettingsView.setImageResource(R.drawable.ic_baseline_ad_setting_48)
                val fiveDp = UtilsView.dpToPx(adSettingsView.context, 5)
                adSettingsView.setPaddingRelative(fiveDp, 0, 0, 0)
                val layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                adViewContainer.addView(adSettingsView, layoutParams)
                adSettingsView.setOnClickListener { v: View ->
                    v.context.startActivity(
                        Intent(
                            v.context,
                            MyPreferenceActivity::class.java
                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                }

                // Improve the UI of Ads a bit
                adViewContainer.setBackgroundResource(R.drawable.border)
                (adViewContainer.layoutParams as MarginLayoutParams).setMargins(
                    fiveDp,
                    fiveDp / 2,
                    fiveDp,
                    fiveDp / 2
                )
            }

            override fun onAdClicked() {
                super.onAdClicked()
                logEvent("didClickOnAdIn$screenName")
            }

            @SuppressLint("DefaultLocale")
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                logEvent(
                    "didFailToLoadAdIn$screenName", String.format(
                        "domain: %s, code: %d, message: %s",
                        loadAdError.domain,
                        loadAdError.code,
                        loadAdError.message
                    )
                )
            }
        }
        adView.loadAd(AdRequest.Builder().build())
    }
}