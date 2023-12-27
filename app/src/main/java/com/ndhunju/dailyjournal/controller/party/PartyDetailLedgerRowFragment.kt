package com.ndhunju.dailyjournal.controller.party;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.database.PartyDAO;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.util.HashSet;
import java.util.Set;

/** @see PartyDetailFragment */
public class PartyDetailLedgerRowFragment extends PartyDetailFragment implements PartyDAO.Observer, LedgerAdapter.OnItemClickListener, LedgerRowAdapter.Client {

    public static final String TAG = PartyDetailLedgerRowFragment.class.getName();

    Set<String> colsToShow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceService ps  = PreferenceService.from(getContext());
        colsToShow = ps.getVal(R.string.key_pref_ledger_row_cols, new HashSet<String>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        TableRow headerRow = (TableRow) rootView.findViewById(R.id.fragment_party_detail_ledger_row_header);
        TextView numCol = (TextView) headerRow.findViewById(R.id.ledger_row_num);
        TextView dateCol = (TextView) headerRow.findViewById(R.id.ledger_row_date);
        TextView noteCol = (TextView) headerRow.findViewById(R.id.ledger_row_note);
        TextView drCol = (TextView) headerRow.findViewById(R.id.ledger_row_dr);
        TextView crCol = (TextView) headerRow.findViewById(R.id.ledger_row_cr);
        TextView balCol = (TextView) headerRow.findViewById(R.id.ledger_row_balance);

        addDrawables(getActivity(), numCol, dateCol, noteCol, drCol, crCol, balCol);
        //add common attributes
        addAttributes(TextUtils.TruncateAt.MARQUEE, drCol, crCol, balCol);

        // show selected columns only
        numCol.setVisibility(showNoCol() ? View.VISIBLE : View.GONE);
        dateCol.setVisibility(showDateCol() ? View.VISIBLE : View.GONE);
        noteCol.setVisibility(showNoteCol() ? View.VISIBLE : View.GONE);
        drCol.setVisibility(showDrCol() ? View.VISIBLE : View.GONE);
        crCol.setVisibility(showCrCol() ? View.VISIBLE : View.GONE);
        balCol.setVisibility(showBalanceCol() ? View.VISIBLE : View.GONE);

        return rootView;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_party_detail_ledger_row;
    }

    @Override
    protected void setLedgerListView(View container) {
        super.setLedgerListView(container);
        setLedgerAdapter(new LedgerRowAdapter(getActivity(), this, getParty()));
        getLedgerListView().setHorizontalScrollBarEnabled(true);
        getLedgerListView().setNestedScrollingEnabled(true);
    }

    //Add Totals in the footer row
    protected void setFooterView(ViewGroup root) {
        TableRow footerRow = (TableRow) root.findViewById(R.id.ledger_row_footer);
        TextView numCol = (TextView) footerRow.findViewById(R.id.ledger_row_num);
        TextView dateCol = (TextView) footerRow.findViewById(R.id.ledger_row_date);
        TextView noteCol = (TextView) footerRow.findViewById(R.id.ledger_row_note);
        TextView drCol = (TextView) footerRow.findViewById(R.id.ledger_row_dr);
        TextView crCol = (TextView) footerRow.findViewById(R.id.ledger_row_cr);
        TextView balCol = (TextView) footerRow.findViewById(R.id.ledger_row_balance);

        // show selected columns only
        numCol.setVisibility(showNoCol() ? View.VISIBLE : View.GONE);
        dateCol.setVisibility(showDateCol() ? View.VISIBLE : View.GONE);
        noteCol.setVisibility(showNoteCol() ? View.VISIBLE : View.GONE);
        drCol.setVisibility(showDrCol() ? View.VISIBLE : View.GONE);
        crCol.setVisibility(showCrCol() ? View.VISIBLE : View.GONE);
        balCol.setVisibility(showBalanceCol() ? View.VISIBLE : View.GONE);

        drCol.setText(UtilsFormat.formatDecimal(getParty().getDebitTotal(), getActivity()));
        crCol.setText(UtilsFormat.formatDecimal(getParty().getCreditTotal(), getActivity()));
        balCol.setText(UtilsFormat.formatCurrency(getParty().calculateBalances(), getContext()));

        addDrawables(getActivity(), numCol, dateCol, noteCol, drCol, crCol, balCol);
        //add common attributes
        addAttributes(TextUtils.TruncateAt.MARQUEE, drCol, crCol, balCol);
    }

    @Override
    public boolean showNoCol() {
        return colsToShow.contains(getContext().getString(R.string.str_num));
    }

    @Override
    public boolean showDateCol() {
        return colsToShow.contains(getContext().getString(R.string.str_date));
    }

    @Override
    public boolean showNoteCol() {
        return colsToShow.contains(getContext().getString(R.string.str_note));
    }

    @Override
    public boolean showDrCol() {
        return colsToShow.contains(getContext().getString(R.string.str_dr));
    }

    @Override
    public boolean showCrCol() {
        return colsToShow.contains(getContext().getString(R.string.str_cr));
    }

    @Override
    public boolean showBalanceCol() {
        return colsToShow.contains(getContext().getString(R.string.str_balance));
    }

}
