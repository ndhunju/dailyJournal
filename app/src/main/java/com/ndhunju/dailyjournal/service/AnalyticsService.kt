package com.ndhunju.dailyjournal.service

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.Event

object AnalyticsService {

    // Constants
    private const val TAG = "AnalyticsService"
    // Member Variables
    private var firebase: FirebaseAnalytics? = null

    // Sets up this class for usage
    fun setup(context: Context) {
        firebase = FirebaseAnalytics.getInstance(context)
        firebase?.appInstanceId?.addOnSuccessListener { appInstanceId ->
            firebase?.setUserProperty("appInstanceId", appInstanceId)
            Log.d(TAG, "appInstanceId=$appInstanceId")
        }
    }

    fun logEvent(name: String) {
        firebase?.logEvent(name, null)
    }

    fun logEvent(name: String, info: String) {
        val bundle = Bundle()
        bundle.putString("Info", info)
        firebase?.logEvent(name, bundle)
    }

    fun logAppOpenEvent() {
        firebase?.logEvent(Event.APP_OPEN, null)
    }

    fun logAppShareEvent() {
        firebase?.logEvent(Event.SHARE, null)
    }

    fun logScreenViewEvent(screenName: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        firebase?.logEvent(Event.SCREEN_VIEW, bundle)
    }
}