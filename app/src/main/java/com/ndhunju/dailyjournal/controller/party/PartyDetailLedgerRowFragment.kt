package com.ndhunju.dailyjournal.controller.party

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.ndhunju.dailyjournal.ObservableField
import com.ndhunju.dailyjournal.R
import com.ndhunju.dailyjournal.database.IPartyDAO
import com.ndhunju.dailyjournal.util.UtilsFormat

/**
 * @see PartyDetailFragment
 */
class PartyDetailLedgerRowFragment : 
    PartyDetailFragment(), 
    IPartyDAO.Observer,
    LedgerAdapter.OnItemClickListener, 
    LedgerRowAdapter.Client {

    companion object {
        val TAG: String = PartyDetailLedgerRowFragment::class.java.name
        /**
        * Change width's value by this much per click
        */
        const val WIDTH_DELTA: Int = 10
    }
    
    // Width for each column that can be observed for changes
    private val _numColWidth = ObservableField(0F)
    private val _dateColWidth = ObservableField(0F)
    private val _noteColWidth = ObservableField(0F)
    private val _drColWidth = ObservableField(0F)
    private val _crColWidth = ObservableField(0F)
    private val _balColWidth = ObservableField(0F)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val rootView = super.onCreateView(inflater, container, savedInstanceState)

        // Set initial column's width from the resource file
        _numColWidth.set(resources.getDimension(R.dimen.ledger_row_no_width))
        _dateColWidth.set(resources.getDimension(R.dimen.ledger_row_width))
        _noteColWidth.set(resources.getDimension(R.dimen.ledger_row_width))
        _drColWidth.set(resources.getDimension(R.dimen.ledger_row_width))
        _crColWidth.set(resources.getDimension(R.dimen.ledger_row_width))
        _balColWidth.set(resources.getDimension(R.dimen.ledger_row_balance_width))

        setControlsView(rootView)
        setHeaderView(rootView)

        return rootView
    }

    private fun setControlsView(rootView: View) {

        val controlsHeaderRow = rootView.findViewById<TableRow>(R.id.ledger_row_controls)
        // Get references to columns
        val numCol = controlsHeaderRow.findViewById<LinearLayoutCompat>(R.id.ledger_row_num)
        val dateCol = controlsHeaderRow.findViewById<LinearLayoutCompat>(R.id.ledger_row_date)
        val noteCol = controlsHeaderRow.findViewById<LinearLayoutCompat>(R.id.ledger_row_note)
        val drCol = controlsHeaderRow.findViewById<LinearLayoutCompat>(R.id.ledger_row_dr)
        val crCol = controlsHeaderRow.findViewById<LinearLayoutCompat>(R.id.ledger_row_cr)
        val balCol = controlsHeaderRow.findViewById<LinearLayoutCompat>(R.id.ledger_row_balance)

        // Get references to controls
        val numColR = controlsHeaderRow.findViewById<View>(R.id.ledger_row_num_right)
        val dateColL = controlsHeaderRow.findViewById<View>(R.id.ledger_row_date_left)
        val dateColR = controlsHeaderRow.findViewById<View>(R.id.ledger_row_date_right)
        val noteColL = controlsHeaderRow.findViewById<View>(R.id.ledger_row_note_left)
        val noteColR = controlsHeaderRow.findViewById<View>(R.id.ledger_row_note_right)
        val drColL = controlsHeaderRow.findViewById<View>(R.id.ledger_row_dr_left)
        val drColR = controlsHeaderRow.findViewById<View>(R.id.ledger_row_dr_right)
        val crColL = controlsHeaderRow.findViewById<View>(R.id.ledger_row_cr_left)
        val crColR = controlsHeaderRow.findViewById<View>(R.id.ledger_row_cr_right)
        val balColL = controlsHeaderRow.findViewById<View>(R.id.ledger_row_balance_left)
        val balColR = controlsHeaderRow.findViewById<View>(R.id.ledger_row_balance_right)
        val balColIncrease = controlsHeaderRow.findViewById<View>(R.id.ledger_row_balance_increase)

        // Increase or decrease the width value of respective column
        // based on which control is clicked
        // Controls for Number Column
        numColR.setOnClickListener{ _numColWidth.set(_numColWidth.get() - WIDTH_DELTA) }
        dateColL.setOnClickListener{ _numColWidth.set(_numColWidth.get() + WIDTH_DELTA) }

        // Controls for Date Column
        dateColR.setOnClickListener { _dateColWidth.set(_dateColWidth.get() - WIDTH_DELTA) }
        noteColL.setOnClickListener { _dateColWidth.set(_dateColWidth.get() + WIDTH_DELTA) }

        // Controls for Note Column
        noteColR.setOnClickListener { _noteColWidth.set(_noteColWidth.get() - WIDTH_DELTA) }
        drColL.setOnClickListener { _noteColWidth.set(_noteColWidth.get() + WIDTH_DELTA) }

        // Controls for Dr Column
        drColR.setOnClickListener { _drColWidth.set(_drColWidth.get() - WIDTH_DELTA) }
        crColL.setOnClickListener { _drColWidth.set(_drColWidth.get() + WIDTH_DELTA) }

        // Controls for Cr Column
        crColR.setOnClickListener { _crColWidth.set(_crColWidth.get() - WIDTH_DELTA) }
        balColL.setOnClickListener { _crColWidth.set(_crColWidth.get() + WIDTH_DELTA) }

        // Controls for Balance Column
        balColR.setOnClickListener { _balColWidth.set(_balColWidth.get() - WIDTH_DELTA) }
        balColIncrease.setOnClickListener { _balColWidth.set(_balColWidth.get() + WIDTH_DELTA) }

        numCol.visibility = if (showNoCol()) View.VISIBLE else View.GONE
        dateCol.visibility = if (showDateCol()) View.VISIBLE else View.GONE
        noteCol.visibility = if (showNoteCol()) View.VISIBLE else View.GONE
        drCol.visibility = if (showDrCol()) View.VISIBLE else View.GONE
        crCol.visibility = if (showCrCol()) View.VISIBLE else View.GONE
        balCol.visibility = if (showBalanceCol()) View.VISIBLE else View.GONE

        LedgerRowAdapter.setWidth(numCol, _numColWidth)
        LedgerRowAdapter.setWidth(dateCol, _dateColWidth)
        LedgerRowAdapter.setWidth(noteCol, _noteColWidth)
        LedgerRowAdapter.setWidth(drCol, _drColWidth)
        LedgerRowAdapter.setWidth(crCol, _crColWidth)
        LedgerRowAdapter.setWidth(balCol, _balColWidth)

        _numColWidth.addObserver { LedgerRowAdapter.setWidth(numCol, _numColWidth) }
        _dateColWidth.addObserver { LedgerRowAdapter.setWidth(dateCol, _dateColWidth) }
        _noteColWidth.addObserver { LedgerRowAdapter.setWidth(noteCol, _noteColWidth) }
        _drColWidth.addObserver { LedgerRowAdapter.setWidth(drCol, _drColWidth) }
        _crColWidth.addObserver { LedgerRowAdapter.setWidth(crCol, _crColWidth) }
        _balColWidth.addObserver { LedgerRowAdapter.setWidth(balCol, _balColWidth) }
    }

    private fun setHeaderView(rootView: View): View {
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

        LedgerRowAdapter.setWidth(numCol, _numColWidth)
        LedgerRowAdapter.setWidth(dateCol, _dateColWidth)
        LedgerRowAdapter.setWidth(noteCol, _noteColWidth)
        LedgerRowAdapter.setWidth(drCol, _drColWidth)
        LedgerRowAdapter.setWidth(crCol, _crColWidth)
        LedgerRowAdapter.setWidth(balCol, _balColWidth)

        _numColWidth.addObserver { LedgerRowAdapter.setWidth(numCol, _numColWidth) }
        _dateColWidth.addObserver { LedgerRowAdapter.setWidth(dateCol, _dateColWidth) }
        _noteColWidth.addObserver { LedgerRowAdapter.setWidth(noteCol, _noteColWidth) }
        _drColWidth.addObserver { LedgerRowAdapter.setWidth(drCol, _drColWidth) }
        _crColWidth.addObserver { LedgerRowAdapter.setWidth(crCol, _crColWidth) }
        _balColWidth.addObserver { LedgerRowAdapter.setWidth(balCol, _balColWidth) }

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

        LedgerRowAdapter.setWidth(numCol, _numColWidth)
        LedgerRowAdapter.setWidth(dateCol, _dateColWidth)
        LedgerRowAdapter.setWidth(noteCol, _noteColWidth)
        LedgerRowAdapter.setWidth(drCol, _drColWidth)
        LedgerRowAdapter.setWidth(crCol, _crColWidth)
        LedgerRowAdapter.setWidth(balCol, _balColWidth)

        _numColWidth.addObserver { LedgerRowAdapter.setWidth(numCol, _numColWidth) }
        _dateColWidth.addObserver { LedgerRowAdapter.setWidth(dateCol, _dateColWidth) }
        _noteColWidth.addObserver { LedgerRowAdapter.setWidth(noteCol, _noteColWidth) }
        _drColWidth.addObserver { LedgerRowAdapter.setWidth(drCol, _drColWidth) }
        _crColWidth.addObserver { LedgerRowAdapter.setWidth(crCol, _crColWidth) }
        _balColWidth.addObserver { LedgerRowAdapter.setWidth(balCol, _balColWidth) }
    }

    override fun showNoCol(): Boolean {
        return true
    }

    override fun showDateCol(): Boolean {
        return true
    }

    override fun showNoteCol(): Boolean {
        return true
    }

    override fun showDrCol(): Boolean {
        return true
    }

    override fun showCrCol(): Boolean {
        return true
    }

    override fun showBalanceCol(): Boolean {
        return true
    }

    override fun getNumColWidth(): ObservableField<Float> {
        return _numColWidth
    }

    override fun getDateColWidth(): ObservableField<Float> {
        return _dateColWidth
    }

    override fun getNoteColWidth(): ObservableField<Float> {
        return _noteColWidth
    }

    override fun getDrColWidth(): ObservableField<Float> {
        return _drColWidth
    }

    override fun getCrColWidth(): ObservableField<Float> {
        return _crColWidth
    }

    override fun getBalColWidth(): ObservableField<Float> {
        return _balColWidth
    }

}