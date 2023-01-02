package com.ndhunju.dailyjournal.controller.party

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ndhunju.dailyjournal.R
import com.ndhunju.dailyjournal.service.ImportContacts.Contact

class ImportContactsView : LinearLayout {

    // Member Variables
    private lateinit var contactsRv: RecyclerView
    private lateinit var  searchContactsET: EditText
    private lateinit var  importContactsAdapter: ImportContactsAdapter

    var contacts: MutableList<Contact>
        get() {
            return importContactsAdapter.contacts
        }
        set(value) {
            importContactsAdapter.contacts = value
        }

    val selectedContacts: List<Contact>
        get() {
            return importContactsAdapter.selectedContacts.toList()
        }

    constructor(context: Context) : super(context) {
        initView(context, null, 0);
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) : super(context, attrs, defStyleAttr) {
        initView(context, attrs, defStyleAttr)
    }

    private fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {

        val view: View = LayoutInflater.from(context).inflate(
            R.layout.fragment_import_contacts_list,
            this
        )

        searchContactsET = view.findViewById(R.id.layout_import_contacts_search_et)
        searchContactsET.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                importContactsAdapter.filter(s) //filter the list below
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })

        contactsRv = view.findViewById(R.id.layout_import_contacts_list)
        contactsRv.layoutManager = LinearLayoutManager(getContext())

        importContactsAdapter = ImportContactsAdapter(view.context)
        contactsRv.adapter = importContactsAdapter

        // Select All button
        view.findViewById<View>(R.id.layout_import_contacts_select_all_btn)
            .setOnClickListener { v: View? ->
                // TODO
            }
    }


    companion object {
        //Variables
        val TAG = ImportContactsView::class.java.simpleName
    }
}