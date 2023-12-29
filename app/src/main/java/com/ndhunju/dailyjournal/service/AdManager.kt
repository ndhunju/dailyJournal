package com.ndhunju.dailyjournal.service

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import com.google.android.gms.ads.*
import com.google.android.ump.*
import com.google.android.ump.ConsentInformation.*
import com.ndhunju.dailyjournal.BuildConfig
import com.ndhunju.dailyjournal.R
import com.ndhunju.dailyjournal.controller.DailyJournalApplication
import com.ndhunju.dailyjournal.controller.ads.AdsLayout
import com.ndhunju.dailyjournal.service.AnalyticsService.logEvent
import com.ndhunju.dailyjournal.util.UtilsView

object AdManager {

    fun initConsent(activity: Activity) {

        // https://developers.google.com/admob/android/privacy
        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(
                //ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_DISABLED
                ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
                //ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA
            )
            // Find addTestDeviceHashedId keyword in logs to get DeviceHashedId
            .addTestDeviceHashedId("")
            .build()

        val params = ConsentRequestParameters.Builder()
            .setAdMobAppId(activity.getString(R.string.admob_app_id))
            .setConsentDebugSettings(debugSettings)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        // You should request an update of the user's consent information at every app launch,
        // using requestConsentInfoUpdate() before loading a form. This can determine whether
        // or not your user needs to provide consent if they hadn't done so already
        // or if their consent has expired.
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // The consent information state was updated.
                // You are now ready to check if a form is available.
                if (consentInformation.isConsentFormAvailable) {
                    loadForm(activity, consentInformation)
                } else if (consentInformation.consentStatus == ConsentStatus.NOT_REQUIRED
                    || consentInformation.consentStatus == ConsentStatus.OBTAINED) {
                    initMobileAds(consentInformation)
                } else {
                    if (BuildConfig.DEBUG) {
                        UtilsView.toast(activity, consentInformation.getDebugInfo())
                    }
                }
            },
            { formError ->
                // Handle the error.
                AnalyticsService.logEvent("ConsentInfoFormError", formError.message)
            })

        // Simulate a user's first install experience
        //consentInformation.reset();

    }

    private fun loadForm(activity: Activity, consentInformation: ConsentInformation) {
        // Loads a consent form. Must be called on the main thread.
        UserMessagingPlatform.loadConsentForm(
            activity,
            { consentFormOut ->
                when (consentInformation.consentStatus) {
                    ConsentStatus.REQUIRED -> {
                        consentFormOut.show(
                            activity
                        ) { onConsentFormDismissedFormError ->
                            AnalyticsService.logEvent(
                                "OnConsentFormDismissed",
                                onConsentFormDismissedFormError?.message ?: ""
                            )
                            // Handle dismissal by reloading form.
                            loadForm(activity, consentInformation)
                        }
                    }
                    ConsentStatus.NOT_REQUIRED, ConsentStatus.OBTAINED -> {
                        initMobileAds(consentInformation)
                    }
                }
            },
            { onConsentFormLoadFailureFormError ->
                // Handle the error
                AnalyticsService.logEvent(
                    "OnConsentFormLoadFailure",
                    onConsentFormLoadFailureFormError.message
                )
            }
        )
    }

    private fun initMobileAds(consentInformation: ConsentInformation) {

        // Don't initialize MobileAds if consent is required
        if (consentInformation.consentStatus == ConsentStatus.REQUIRED) {
            return
        }

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

fun ConsentInformation.getDebugInfo(): String {
    val consentStatus = when(consentStatus) {
        ConsentStatus.REQUIRED -> "REQUIRED"
        ConsentStatus.OBTAINED -> "OBTAINED"
        ConsentStatus.NOT_REQUIRED -> "NOT_REQUIRED"
        else -> "UNKNOWN"}
    return "isConsentFormAvailable=${isConsentFormAvailable};" +
            "consentStatus=${consentStatus};"
    
}