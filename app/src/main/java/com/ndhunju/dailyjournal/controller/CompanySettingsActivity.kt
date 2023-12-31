package com.ndhunju.dailyjournal.controller

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.ndhunju.dailyjournal.OnDatePickerDialogBtnClickedListener
import com.ndhunju.dailyjournal.R
import com.ndhunju.dailyjournal.controller.backup.BackupActivity
import com.ndhunju.dailyjournal.controller.backup.BackupPreferenceFragment
import com.ndhunju.dailyjournal.controller.fragment.DatePickerFragment
import com.ndhunju.dailyjournal.service.AnalyticsService.logScreenViewEvent
import com.ndhunju.dailyjournal.service.Services
import com.ndhunju.dailyjournal.util.UtilsFormat
import com.ndhunju.dailyjournal.util.UtilsView
import java.util.Calendar
import java.util.Date

class CompanySettingsActivity : BaseActivity(), OnDatePickerDialogBtnClickedListener {
    var companyNameEt: EditText? = null
    var dateBtn: Button? = null
    var doneBtn: Button? = null
    var services: Services? = null
    var financialYear: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_company_settings)
        setSupportActionBar(findViewById(R.id.toolbar))
        services = Services.getInstance(context)

        val welcomeTextView = findViewById<TextView>(R.id.activity_company_settings_welcome_msg)
        welcomeTextView.text = getString(
            R.string.company_settings_welcome_msg,
            getString(R.string.app_name)
        )

        findViewById<View>(R.id.activity_company_settings_create_new)
            .setOnClickListener {
                // Hide screen 1
                findViewById<View>(R.id.activity_company_settings_screen2).visibility = View.GONE

                // Show screen 2
                findViewById<View>(R.id.activity_company_settings_screen2).visibility = View.VISIBLE
            }

        findViewById<View>(R.id.activity_company_settings_restore_old)
            .setOnClickListener {
                startActivity(
                    Intent(this, BackupActivity::class.java)
                        .putExtra(
                            BackupPreferenceFragment.KEY_MODE,
                            BackupPreferenceFragment.MODE_RESTORE
                        ).putExtra(
                            BackupPreferenceFragment.KEY_FINISH_ON_RESTORE_SUCCESS,
                            true
                        )
                )
            }

        companyNameEt = findViewById(R.id.activity_company_settings_company_name_et)

        dateBtn = findViewById(R.id.activity_company_settings_date_btn)
        dateBtn?.setOnClickListener {
            val dpf = DatePickerFragment.newInstance(Date(), REQUEST_CHGED_DATE)
            dpf.show(supportFragmentManager, DatePickerFragment.TAG)
        }

        doneBtn = findViewById(R.id.activity_company_settings_done_btn)
        doneBtn?.setOnClickListener {

            if (TextUtils.isEmpty(companyNameEt?.text)) {
                companyNameEt?.error = getString(R.string.company_settings_company_name_empty_error)
                //return@setOnClickListener
            }
            services?.companyName = companyNameEt?.text.toString()

            try {
                services?.financialYear = financialYear
            } catch (ex: IllegalStateException) {
                UtilsView.alert(
                    context,
                    getString(R.string.msg_financial_year_set, services?.financialYear)
                )
                //return@setOnPreferenceClickListener
            } catch (ex: Exception) {
                UtilsView.alert(
                    context,
                    getString(R.string.msg_is_not_valid, getString(R.string.str_date))
                )
                //return@setOnClickListener
            }
            finish()
        }

        companyNameEt?.setText(services?.companyName)

        if (services?.financialYear != null) {
            services?.financialYear?.time?.let { time ->
                financialYear = Date(time)
            }
        } else {
            financialYear = Date()
        }

        dateBtn?.text = UtilsFormat.formatDate(financialYear, this)
    }

    override fun onResume() {
        super.onResume()
        logScreenViewEvent("CompanySettings")
    }

    override fun onDialogBtnClicked(data: Intent, whichBtn: Int, result: Int, requestCode: Int) {
        when (requestCode) {
            REQUEST_CHGED_DATE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    data.getSerializableExtra(DatePickerFragment.EXTRA_CAL, Calendar.getInstance().javaClass)?.timeInMillis?.let {
                        financialYear?.time = it
                    }
                } else {
                    @Suppress("DEPRECATION")
                    (data.getSerializableExtra(DatePickerFragment.EXTRA_CAL) as Calendar?)?.timeInMillis?.let {
                        financialYear?.time = it
                    }
                }
                dateBtn?.text = UtilsFormat.formatDate(financialYear, this)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // don't let user exit this screen until complete
        //super.onBackPressed();
    }

    private val context: Context
        get() = this

    companion object {

        private const val REQUEST_CHGED_DATE = 656

        @JvmStatic
        fun startActivity(callingActivity: Activity, requestCode: Int) {
            val intent = Intent(
                callingActivity,
                CompanySettingsActivity::class.java
            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (requestCode > 0) {
                callingActivity.startActivityForResult(intent, requestCode)
            } else {
                callingActivity.startActivity(intent)
            }
        }
    }
}