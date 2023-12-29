package com.ndhunju.dailyjournal.controller.journal

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ndhunju.dailyjournal.R
import com.ndhunju.dailyjournal.controller.BaseActivity
import com.ndhunju.dailyjournal.service.Constants

/**
 * This activity hosts only one instance of [JournalFragment].
 * Use this for showing only one fragment.
 */
class JournalActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (supportFragmentManager.findFragmentByTag(JournalFragment.TAG) == null) {
            supportFragmentManager.beginTransaction().add(
                R.id.content,
                JournalFragment.newInstance(
                    intent.getLongExtra(Constants.KEY_JOURNAL_ID, -1),
                    intent.getIntExtra(Constants.KEY_JOURNAL_POS, -1)
                ),
                JournalFragment.TAG
            ).commit()
        }
    }

}