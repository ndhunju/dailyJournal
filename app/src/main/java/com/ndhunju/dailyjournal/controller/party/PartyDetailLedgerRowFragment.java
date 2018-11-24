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
import com.ndhunju.dailyjournal.util.UtilsFormat;

/** @see PartyDetailFragment */
public class PartyDetailLedgerRowFragment extends PartyDetailFragment implements PartyDAO.Observer, LedgerAdapter.OnItemClickListener {

    public static final String TAG = PartyDetailLedgerRowFragment.class.getName();

    TableRow headerRow;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        headerRow = (TableRow) rootView.findViewById(R.id.fragment_party_detail_header_tr);
        headerRow.setVisibility(View.VISIBLE);

        ((TextView)rootView.findViewById(R.id.activity_party_col_header_dr)).setText(getString(R.string.str_dr));
        ((TextView)rootView.findViewById(R.id.activity_party_col_header_cr)).setText(getString(R.string.str_cr));

        return rootView;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_party_detail_ledger_row;
    }

    @Override
    protected void setLedgerListView(View container) {
        super.setLedgerListView(container);
        setLedgerAdapter(new LedgerRowAdapter(getActivity(), getParty()));
        getLedgerListView().setHorizontalScrollBarEnabled(true);
        getLedgerListView().setNestedScrollingEnabled(true);
    }

    //Add Totals in the footer row
    protected void setFooterView(ViewGroup root) {
        TableRow footerRow = (TableRow) root.findViewById(R.id.ledger_row_footer);
        TextView col1 = (TextView) footerRow.findViewById(R.id.ledger_row_col1);
        TextView col2 = (TextView) footerRow.findViewById(R.id.ledger_row_col2);
        TextView col3 = (TextView) footerRow.findViewById(R.id.ledger_row_col3);
        TextView col4 = (TextView) footerRow.findViewById(R.id.ledger_row_col4);
        TextView balCol = (TextView) footerRow.findViewById(R.id.ledger_row_col5);

        col1.setText(getString(R.string.str_total));
        col2.setText("");
        col3.setText(UtilsFormat.formatDecimal(getParty().getDebitTotal(), getActivity()));
        col4.setText(UtilsFormat.formatDecimal(getParty().getCreditTotal(), getActivity()));
        if (balCol != null) {
            balCol.setText(UtilsFormat.formatCurrency(getParty().calculateBalances(), getContext()));
        }
        addDrawables(getActivity(), col1, col2, col3, col4, balCol);
        //add common attributes
        addAttributes(TextUtils.TruncateAt.MARQUEE, col3, col4, balCol);
    }

}
