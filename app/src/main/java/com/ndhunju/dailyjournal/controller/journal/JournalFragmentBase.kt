package com.ndhunju.dailyjournal.controller.journal

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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

    protected fun showAlertIfNoteHasComma(noteEt: EditText): Boolean {

        if (noteEt.text.toString().contains(",")) {
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.str_alert))
                .setMessage(getString(R.string.msg_journal_note_replace_comma))
                .setPositiveButton(getString(R.string.str_yes)) { _: DialogInterface?, _: Int ->
                    noteEt.setText(
                        noteEt.text.toString().replace(",", ";")
                    )
                }
                .setNegativeButton(getString(R.string.str_no), null)
                .create()
                .show()

            return true
        }

        return false
    }

    /**
     * Child class must provide an instance of [Journal]
     */
    abstract fun provideJournal(): Journal?

}