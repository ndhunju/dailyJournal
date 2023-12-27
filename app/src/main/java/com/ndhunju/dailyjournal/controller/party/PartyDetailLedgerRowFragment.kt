package com.ndhunju.dailyjournal.controller.party

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import com.ndhunju.dailyjournal.R
import com.ndhunju.dailyjournal.controller.party.PartyDetailLedgerRowFragment
import com.ndhunju.dailyjournal.database.IPartyDAO
import com.ndhunju.dailyjournal.service.PreferenceService
import com.ndhunju.dailyjournal.util.UtilsFormat

/**
 * @see PartyDetailFragment
 */
class PartyDetailLedgerRowFragment : 
    PartyDetailFragment(), 
    IPartyDAO.Observer,
    LedgerAdapter.OnItemClickListener, 
    LedgerRowAdapter.Client {
    
    var colsToShow: Set<String> = hashSetOf()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = context ?: return
        val ps = PreferenceService.from(context)
        colsToShow = ps.getVal(R.string.key_pref_ledger_row_cols, HashSet())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = super.onCreateView(inflater, container, savedInstanceState)
        
        val headerRow = rootView.findViewById<TableRow>(R.id.fragment_party_detail_ledger_row_header)
        val numCol = headerRow.findViewById<TextView>(R.id.ledger_row_num)
        val dateCol = headerRow.findViewById<TextView>(R.id.ledger_row_date)
        val noteCol = headerRow.findViewById<TextView>(R.id.ledger_row_note)
        val drCol = headerRow.findViewById<TextView>(R.id.ledger_row_dr)
        val crCol = headerRow.findViewById<TextView>(R.id.ledger_row_cr)
        val balCol = headerRow.findViewById<TextView>(R.id.ledger_row_balance)

        addDrawables(activity, numCol, dateCol, noteCol, drCol, crCol, balCol)
        // Add common attributes
        addAttributes(TextUtils.TruncateAt.MARQUEE, drCol, crCol, balCol)

        // show selected columns only
        numCol.visibility = if (showNoCol()) View.VISIBLE else View.GONE
        dateCol.visibility = if (showDateCol()) View.VISIBLE else View.GONE
        noteCol.visibility = if (showNoteCol()) View.VISIBLE else View.GONE
        drCol.visibility = if (showDrCol()) View.VISIBLE else View.GONE
        crCol.visibility = if (showCrCol()) View.VISIBLE else View.GONE
        balCol.visibility = if (showBalanceCol()) View.VISIBLE else View.GONE

        return rootView
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_party_detail_ledger_row
    }

    override fun setLedgerListView(container: View) {
        super.setLedgerListView(container)
        ledgerAdapter = LedgerRowAdapter(activity, this, party)
        ledgerListView.isHorizontalScrollBarEnabled = true
        ledgerListView.isNestedScrollingEnabled = true
    }

    // Add Totals in the footer row
    override fun setFooterView(root: ViewGroup) {
        val footerRow = root.findViewById<TableRow>(R.id.ledger_row_footer)
        val numCol = footerRow.findViewById<TextView>(R.id.ledger_row_num)
        val dateCol = footerRow.findViewById<TextView>(R.id.ledger_row_date)
        val noteCol = footerRow.findViewById<TextView>(R.id.ledger_row_note)
        val drCol = footerRow.findViewById<TextView>(R.id.ledger_row_dr)
        val crCol = footerRow.findViewById<TextView>(R.id.ledger_row_cr)
        val balCol = footerRow.findViewById<TextView>(R.id.ledger_row_balance)

        // Show selected columns only
        numCol.visibility = if (showNoCol()) View.VISIBLE else View.GONE
        dateCol.visibility = if (showDateCol()) View.VISIBLE else View.GONE
        noteCol.visibility = if (showNoteCol()) View.VISIBLE else View.GONE
        drCol.visibility = if (showDrCol()) View.VISIBLE else View.GONE
        crCol.visibility = if (showCrCol()) View.VISIBLE else View.GONE
        balCol.visibility = if (showBalanceCol()) View.VISIBLE else View.GONE
        drCol.text = UtilsFormat.formatDecimal(party.debitTotal, activity)
        crCol.text = UtilsFormat.formatDecimal(party.creditTotal, activity)
        balCol.text = UtilsFormat.formatCurrency(party.calculateBalances(), context)

        addDrawables(activity, numCol, dateCol, noteCol, drCol, crCol, balCol)
        //add common attributes
        addAttributes(TextUtils.TruncateAt.MARQUEE, drCol, crCol, balCol)
    }

    override fun showNoCol(): Boolean {
        return colsToShow.contains(getString(R.string.str_num))
    }

    override fun showDateCol(): Boolean {
        return colsToShow.contains(getString(R.string.str_date))
    }

    override fun showNoteCol(): Boolean {
        return colsToShow.contains(getString(R.string.str_note))
    }

    override fun showDrCol(): Boolean {
        return colsToShow.contains(getString(R.string.str_dr))
    }

    override fun showCrCol(): Boolean {
        return colsToShow.contains(getString(R.string.str_cr))
    }

    override fun showBalanceCol(): Boolean {
        return colsToShow.contains(getString(R.string.str_balance))
    }

    companion object {
        val TAG: String = PartyDetailLedgerRowFragment::class.java.name
    }
}