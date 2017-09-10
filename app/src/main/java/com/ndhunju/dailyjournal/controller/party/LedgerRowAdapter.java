package com.ndhunju.dailyjournal.controller.party;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ndhunju.dailyjournal.ObservableField;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.util.Date;

/**
 * Created by dhunju on 9/26/2015.
 * This adapter provides view for row of a Ledger
 */
class LedgerRowAdapter extends LedgerAdapter {

    // variable.
    private String formattedAmt; // stores formatted amount. Declared here to prevent memory alloc and dealloc on every bind call
    private boolean showNoteCol;

    public LedgerRowAdapter(Activity activity, Party party) {
        super(activity, party);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LedgerVH(LayoutInflater.from(getContext()).inflate(R.layout.ledger_row, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ((LedgerVH) holder).bind(getItem(position));
    }

    class LedgerVH extends LedgerAdapter.LedgerVH implements ObservableField.Observer {
        TextView dateCol ;
        TextView noteCol ;
        TextView drCol;
        TextView crCol;
        TextView balCol;

        public LedgerVH(View convertView) {
            super(convertView);
            dateCol = (TextView) convertView.findViewById(R.id.ledger_row_col1);
            noteCol = (TextView) convertView.findViewById(R.id.ledger_row_col2);
            drCol = (TextView) convertView.findViewById(R.id.ledger_row_col3);
            crCol = (TextView) convertView.findViewById(R.id.ledger_row_col4);
            balCol = (TextView) convertView.findViewById(R.id.ledger_row_col5);
            showBalance.addObserver(this);
            //apply common settings
            PartyDetailFragment.addAttributes(TextUtils.TruncateAt.END, dateCol, noteCol);
            PartyDetailFragment.addAttributes(TextUtils.TruncateAt.START, drCol, crCol);
        }

        public void bind(Journal journal) {
            super.bind(journal);
            dateCol.setText(UtilsFormat.formatDate(new Date(journal.getDate()), getContext()));

            //showNoteCol will be false for smaller devices
            showNoteCol = getContext().getResources().getBoolean(R.bool.note_col);
            if(showNoteCol) {noteCol.setText(journal.getNote());}
            else{noteCol.setVisibility(View.GONE); }

            formattedAmt = UtilsFormat.formatDecimal(journal.getAmount(), getContext());

            if (journal.getType().equals(Journal.Type.Debit)) {
                drCol.setText(formattedAmt);
                crCol.setText("");
            } else {
                crCol.setText(formattedAmt);
                drCol.setText("");
            }

            if (balCol != null) {
                if (journal.getBalance() != null && showBalance.get()) {
                    balCol.setText(UtilsFormat.formatCurrency(journal.getBalance(), getContext()));
                    balCol.setVisibility(View.VISIBLE);
                } else {
                    balCol.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onChanged(ObservableField observableField) {
            if (observableField.equals(showBalance) && balCol != null) {
                balCol.setVisibility((boolean) observableField.get() ? View.VISIBLE : View.GONE);
            }
        }
    }
}
