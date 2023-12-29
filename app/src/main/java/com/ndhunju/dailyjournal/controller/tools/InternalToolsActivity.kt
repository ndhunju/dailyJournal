package com.ndhunju.dailyjournal.controller.tools

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.ndhunju.dailyjournal.R
import com.ndhunju.dailyjournal.controller.NavDrawerActivity
import com.ndhunju.dailyjournal.service.AnalyticsService.logScreenViewEvent
import com.ndhunju.dailyjournal.service.Services

/**
 * Created by ndhunju on 1/7/17.
 * This activity groups together all the tools this app
 * has to offer.
 */
class InternalToolsActivity : NavDrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addContentFrame(R.layout.activity_internal_tools)

        // wire up and set up
        val listView = findViewById<ListView>(R.id.activity_internal_tools_list_view)

        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
        adapter.add("Convert English Date To Nepali and Prefix it to Notes")
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener {
                _: AdapterView<*>?, _: View?, position: Int, id: Long ->
            if (position == 0) {

                // Show progress to user
                val pd = ProgressDialog(context)
                pd.setMessage(getString(R.string.str_start))
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                pd.setCanceledOnTouchOutside(false)
                pd.setCancelable(false)
                pd.setMax(100)
                pd.show()
                pd.setProgress(10)

                Services.getInstance(context).convertEnglishDateToNepaliAndPrefixToNotes {
                        _, percentage, message, _ ->

                    run {
                        runOnUiThread {
                            pd.progress = percentage
                            pd.setMessage(message)

                            if (percentage == 100) {
                                pd.dismiss()
                                AlertDialog.Builder(context)
                                    .setMessage(R.string.str_finished)
                                    .show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        logScreenViewEvent("InternalTools")
    }

}