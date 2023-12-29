package com.ndhunju.dailyjournal.controller.ads

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.ndhunju.dailyjournal.R
import com.ndhunju.dailyjournal.controller.preference.MyPreferenceActivity
import com.ndhunju.dailyjournal.util.UtilsView

class AdsLayout: LinearLayout {

    private lateinit var adView: AdView

    var adUnitId: String
        get() {
            return adView.adUnitId
        }
        set(value) {
            adView.adUnitId = value
        }

    var adListener: AdListener
        get() {
            return adView.adListener
        }
        set(value) {
            adView.adListener = value
        }

    // Constructors
    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?): super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrSet: AttributeSet?, defStyleAttr: Int) {

        // Setup this layout
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        setBackgroundResource(R.drawable.border)

        val dp5 = UtilsView.dpToPx(context, 5);
        setPaddingRelative(dp5, dp5, dp5, dp5)

        // Add AdView
        adView = AdView(context)
        adView.setAdSize(AdSize.BANNER)

        if (isInEditMode) {
            addView(TextView(context).apply {
                text = "Ad will be shown here at run time."
            })
        } else {
            addView(adView)
        }

        // Ad an icon so that user can access settings to
        // enable/disable the ads conveniently
        val adSettingsView = ImageView(context)
        adSettingsView.setImageResource(R.drawable.ic_baseline_ad_setting_48)
        adSettingsView.setPaddingRelative(dp5, 0, 0, 0)
        addView(adSettingsView)
        adSettingsView.setOnClickListener { v: View ->
            v.context.startActivity(
                Intent(
                    v.context,
                    MyPreferenceActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }
    }

    @SuppressLint("MissingPermission") // It is already added
    fun loadAd(adRequest: AdRequest) {
        adView.loadAd(adRequest)
    }

}