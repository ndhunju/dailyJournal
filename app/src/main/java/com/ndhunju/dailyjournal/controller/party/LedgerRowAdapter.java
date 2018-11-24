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
    private Client client;


    public LedgerRowAdapter(Activity activity, Client client, Party party) {
        super(activity, party);
        this.client = client;
        setLayoutId(R.layout.ledger_row);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LedgerVH(LayoutInflater.from(getContext()).inflate(mLayoutResId, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ((LedgerVH) holder).bind(getItem(position));
    }

    class LedgerVH extends LedgerAdapter.LedgerVH implements ObservableField.Observer {
        TextView numCol;
        TextView dateCol ;
        TextView noteCol ;
        TextView drCol;
        TextView crCol;
        TextView balCol;

        public LedgerVH(View convertView) {
            super(convertView);
            numCol = (TextView) convertView.findViewById(R.id.ledger_row_num);
            dateCol = (TextView) convertView.findViewById(R.id.ledger_row_date);
            noteCol = (TextView) convertView.findViewById(R.id.ledger_row_note);
            drCol = (TextView) convertView.findViewById(R.id.ledger_row_dr);
            crCol = (TextView) convertView.findViewById(R.id.ledger_row_cr);
            balCol = (TextView) convertView.findViewById(R.id.ledger_row_balance);

            //apply common settings
            PartyDetailFragment.addAttributes(TextUtils.TruncateAt.END, dateCol, noteCol);
            PartyDetailFragment.addAttributes(TextUtils.TruncateAt.START, drCol, crCol);

            // show selected columns only
            numCol.setVisibility(client.showNoCol() ? View.VISIBLE : View.GONE);
            dateCol.setVisibility(client.showDateCol() ? View.VISIBLE : View.GONE);
            noteCol.setVisibility(client.showNoteCol() ? View.VISIBLE : View.GONE);
            drCol.setVisibility(client.showDrCol() ? View.VISIBLE : View.GONE);
            crCol.setVisibility(client.showCrCol() ? View.VISIBLE : View.GONE);
            balCol.setVisibility(client.showBalanceCol() ? View.VISIBLE : View.GONE);
            showBalance.set(client.showBalanceCol());

            showBalance.addObserver(this);
        }

        public void bind(Journal journal) {
            super.bind(journal);
            numCol.setText(String.valueOf(getAdapterPosition() + 1));
            dateCol.setText(UtilsFormat.formatDate(new Date(journal.getDate()), getContext()));
            noteCol.setText(journal.getNote());

            formattedAmt = UtilsFormat.formatDecimal(journal.getAmount(), getContext());

            if (journal.getType().equals(Journal.Type.Debit)) {
                drCol.setText(formattedAmt);
                crCol.setText("");
            } else {
                crCol.setText(formattedAmt);
                drCol.setText("");
            }

            if (journal.getBalance() != null && showBalance.get()) {
                balCol.setText(UtilsFormat.formatCurrency(journal.getBalance(), getContext()));
                balCol.setVisibility(View.VISIBLE);
            } else {
                balCol.setVisibility(View.GONE);
            }
        }

        @Override
        public void onChanged(ObservableField observableField) {
            if (observableField.equals(showBalance)) {
                balCol.setVisibility((boolean) observableField.get() ? View.VISIBLE : View.GONE);
            }
        }
    }

    interface Client {

        boolean showNoCol();

        boolean showDateCol();

        boolean showNoteCol();

        boolean showDrCol();

        boolean showCrCol();

        boolean showBalanceCol();
    }
}
