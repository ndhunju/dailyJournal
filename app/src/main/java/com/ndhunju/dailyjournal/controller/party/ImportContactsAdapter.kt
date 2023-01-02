package com.ndhunju.dailyjournal.controller.party

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View.OnLongClickListener
import android.text.TextUtils
import android.view.View
import android.widget.CheckedTextView
import androidx.appcompat.app.AlertDialog
import com.ndhunju.dailyjournal.service.ImportContacts
import com.ndhunju.dailyjournal.service.ImportContacts.Contact
import java.util.*

/**
 * Adapter for [ImportContacts.Contact]
 */
class ImportContactsAdapter(
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Member variables
    private var itemClickListener: OnItemClickListener? = null
    private val filteredContacts: MutableList<Contact>
    val selectedContacts: MutableSet<Contact>
    private var activatedItemPos: Int
    private var inFilterMode: Boolean

    var contacts: MutableList<Contact> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
    }


    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int, id: Long)

        /**
         * For one of the items, context menu was opened and a menu item was clicked
         * @param view : view associated with the item ( not context menu item)
         * @param position : position of the item ( not context menu item)
         * @param id : id of the context menu item
         */
        fun onContextItemClick(view: View?, position: Int, id: Long)
    }

    init {
        filteredContacts = mutableListOf()
        selectedContacts = mutableSetOf()
        activatedItemPos = -1
        setHasStableIds(true)
        inFilterMode = false
    }

    /**
     * Filters the list with passed keyword
     */
    fun filter(keyword: CharSequence) {

        if (TextUtils.isEmpty(keyword)) {
            inFilterMode = false
            notifyDataSetChanged()
            return
        }

        inFilterMode = true
        filteredContacts.clear()
        for (contact: Contact in contacts) {
            if (contact.name.lowercase(Locale.getDefault())
                    .contains(keyword.toString().lowercase(Locale.getDefault()))) {
                filteredContacts.add(contact)
            }
        }

        notifyDataSetChanged()
    }

    /**
     * Selects all filtered items.
     */
    fun selectAllFilteredItems() {
        if (inFilterMode) {
            selectedContacts.addAll(filteredContacts)
        } else {
            selectedContacts.addAll(contacts)
        }

        notifyDataSetChanged()
    }

    fun setActivatedItemPos(pos: Int) {
        activatedItemPos = pos
        notifyItemChanged(pos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ContactsVH(
            LayoutInflater.from(context).inflate(
                android.R.layout.simple_list_item_multiple_choice,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ContactsVH).bind(getItem(position), position)
    }

    fun isInBound(index: Int): Boolean {
        return if (inFilterMode) {
            filteredContacts.size < index && index > -1
        } else {
            contacts.size < index && index > -1
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return if (inFilterMode) filteredContacts.size else contacts.size
    }

    fun getItem(pos: Int): Contact {
        return if (inFilterMode) filteredContacts[pos] else contacts[pos]
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        itemClickListener = listener
    }

    private inner class ContactsVH(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener,
        OnLongClickListener
    {
        var contact: Contact? = null
        var nameTextView: CheckedTextView

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            nameTextView = itemView.findViewById(android.R.id.text1)
        }

        fun bind(contact: Contact, position: Int) {
            this.contact = contact
            nameTextView.text = contact.name
            nameTextView.isChecked = selectedContacts.contains(contact);

        }

        override fun onClick(v: View) {

            if (itemClickListener != null) {
                itemClickListener?.onItemClick(
                    v,
                    absoluteAdapterPosition,
                    itemId
                )
            }

            // Update the checked state
            if (nameTextView.isChecked) {
                selectedContacts.remove(contact)
                nameTextView.isChecked = false
            } else {
                contact?.let { selectedContacts.add(it) }
                nameTextView.isChecked = true
            }
        }

        override fun onLongClick(view: View): Boolean {
            // Don't show context menu for now
            //val builder = AlertDialog.Builder(
            //    context
            //)
            //val adapter: IconTextAdapter
            //builder.setAdapter(IconTextAdapter.newInstance(context)
            //    .add(R.string.str_edit, android.R.drawable.ic_menu_edit)
            //    .add(R.string.str_delete, android.R.drawable.ic_menu_delete)
            //    .setIconTint(-1).also { adapter = it }
            //) { _, i ->
            //    if (itemClickListener != null) itemClickListener?.onContextItemClick(
            //        view,
            //        absoluteAdapterPosition,
            //        adapter.getItemId(i)
            //    )
            //}
            //builder.create().show()
            return true
        }
    }
}