package com.ndhunju.dailyjournal.service

import android.annotation.SuppressLint
import android.view.View
import com.google.android.gms.ads.*
import com.ndhunju.dailyjournal.BuildConfig
import com.ndhunju.dailyjournal.R
import com.ndhunju.dailyjournal.controller.DailyJournalApplication
import com.ndhunju.dailyjournal.controller.ads.AdsLayout
import com.ndhunju.dailyjournal.service.AnalyticsService.logEvent

object AdManager {

    init {
        if (BuildConfig.DEBUG) {
            MobileAds.setRequestConfiguration(RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            )
        }
        MobileAds.initialize(DailyJournalApplication.getInstance()) {}
    }

    fun loadAdIfAllowed(
        adsLayout: AdsLayout?,
        adUnitId: String,
        screenName: String
    ) {
        if (adsLayout == null) {
            return
        }

        val ps = PreferenceService.from(adsLayout.context)
        val isAllowed = ps.getVal(R.string.key_pref_item_ad, 0) == 0

        if (isAllowed.not()) {
            adsLayout.visibility = View.GONE
            return
        }

        adsLayout.adUnitId = adUnitId
        adsLayout.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                adsLayout.visibility = View.VISIBLE
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

        adsLayout.loadAd(AdRequest.Builder().build())
    }
}