package com.ndhunju.dailyjournal.controller.party;

import android.content.Context;
import android.support.v4.content.ContextCompat;
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
import java.util.List;

/**
 * Created by dhunju on 9/26/2015.
 * This adapter provides card view for Ledger
 */
public class LedgerCardAdapter extends LedgerAdapter {

    public LedgerCardAdapter(Context context, Party party) {
        super(context, party);
        setLayoutId(R.layout.ledger_card);
    }

    public LedgerCardAdapter(Context context, List<Journal> journals) {
        super(context, journals);
        setLayoutId(R.layout.ledger_card);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LedgerCardViewHolder(LayoutInflater.from(getContext()).inflate(mLayoutResId, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ((LedgerCardViewHolder) holder).bind(mJournals.get(position));
    }

    private class LedgerCardViewHolder extends LedgerVH implements ObservableField.Observer {
        TextView number;
        TextView partyTV;
        TextView dateTV;
        TextView noteTV;
        TextView amountTV;
        TextView drCrTV;
        TextView attchTV;
        TextView balanceTV;

        public LedgerCardViewHolder(View itemView) {
            super(itemView);
            number = (TextView) itemView.findViewById(R.id.ledger_card_number);
            partyTV = (TextView) itemView.findViewById(R.id.ledger_card_party);
            dateTV = (TextView)  itemView.findViewById(R.id.ledger_card_date_tv);
            noteTV = (TextView)  itemView.findViewById(R.id.ledger_card_note_tv);
            amountTV = (TextView)itemView.findViewById(R.id.ledger_card_amount_tv);
            drCrTV = (TextView)  itemView.findViewById(R.id.ledger_card_drcr_tv);
            attchTV = (TextView) itemView.findViewById(R.id.ledger_card_attch_no_tv);
            balanceTV = (TextView) itemView.findViewById(R.id.ledger_card_balance);
            showBalance.addObserver(this);
        }

        public void bind(Journal journal) {
            super.bind(journal);
            if (number != null) {
                number.setText(String.valueOf(getAdapterPosition() + 1));
            }

            if (partyTV != null) {
                partyTV.setText(mServices.getParty(journal.getPartyId()).getName());
            }

            //Log.d("journals " + getCount(), " pos: " + position + "date : " +  getContext().getString(R.string.dateFormat));
            dateTV.setText(UtilsFormat.formatDate(new Date(journal.getDate()), getContext()));
            if (!TextUtils.isEmpty(journal.getNote())) {
                noteTV.setText(journal.getNote());
                noteTV.setVisibility(View.VISIBLE);
            } else {
                noteTV.setVisibility(View.GONE);
            }

            amountTV.setText(UtilsFormat.formatCurrency(journal.getAmount(), getContext()));
            if(journal.getType().equals(Journal.Type.Credit)){
                amountTV.setTextColor(ContextCompat.getColor(getContext(), R.color.red_medium));
                drCrTV.setText(R.string.str_cr);
            }else{
                amountTV.setTextColor(ContextCompat.getColor(getContext(), R.color.green_medium));
                drCrTV.setText( R.string.str_dr);
            }

            if (journal.getBalance() != null && showBalance.get()) {
                balanceTV.setText(getContext().getString(R.string.msg_bal, UtilsFormat.formatCurrency(journal.getBalance(), getContext())));
                balanceTV.setVisibility(View.VISIBLE);
            } else {
                balanceTV.setVisibility(View.GONE);
            }

            attchTV.setText(String.valueOf(mServices.getAttachments(journal.getId()).size()));
        }

        @Override
        public void onChanged(ObservableField observableField) {
            if (observableField.equals(showBalance) && balanceTV != null) {
                balanceTV.setVisibility((boolean) observableField.get() ? View.VISIBLE : View.GONE);
            }
        }
    }
}
