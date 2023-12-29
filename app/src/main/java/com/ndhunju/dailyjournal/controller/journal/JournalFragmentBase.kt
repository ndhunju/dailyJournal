package com.ndhunju.dailyjournal.controller.journal

import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.ndhunju.dailyjournal.R
import com.ndhunju.dailyjournal.model.Journal
import com.ndhunju.dailyjournal.service.Services

abstract class JournalFragmentBase: Fragment()
{
    var mServices: Services? = null

    protected var attachmentViewPagerActivityOnResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _: ActivityResult? ->
        setTextForAttachmentButton(
            null
        )
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        mServices = Services.getInstance(activity)
    }

    open fun setTextForAttachmentButton(attachBtn: Button?)
    {
        var _attachBtn = attachBtn

        if (_attachBtn == null) {
            _attachBtn = view?.findViewById(R.id.fragment_home_attach_btn)
        }

        val journal = provideJournal() ?: return

        val attachCount: Int = mServices?.getAttachments(journal.id)?.size ?: 0
        if (attachCount > 0) {
            _attachBtn?.text = getString(R.string.btn_attachment_with_count, attachCount)
        } else {
            _attachBtn?.text = getString(R.string.str_attachment)
        }
    }

    /**
     * Child class must provide an instance of [Journal]
     */
    abstract fun provideJournal(): Journal?

}