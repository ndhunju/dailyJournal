package com.ndhunju.dailyjournal.controller.party;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    }

    public LedgerCardAdapter(Context context, List<Journal> journals) {
        super(context, journals);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LedgerCardViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.ledger_card, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ((LedgerCardViewHolder) holder).bind(mJournals.get(position));
    }

    private class LedgerCardViewHolder extends LedgerVH {
        TextView dateTV;
        TextView noteTV;
        TextView amountTV;
        TextView drCrTV;
        TextView attchTV;

        public LedgerCardViewHolder(View itemView) {
            super(itemView);
            dateTV = (TextView)  itemView.findViewById(R.id.ledger_card_date_tv);
            noteTV = (TextView)  itemView.findViewById(R.id.ledger_card_note_tv);
            amountTV = (TextView)itemView.findViewById(R.id.ledger_card_amount_tv);
            drCrTV = (TextView)  itemView.findViewById(R.id.ledger_card_drcr_tv);
            attchTV = (TextView) itemView.findViewById(R.id.ledger_card_attch_no_tv);
        }

        public void bind(Journal journal) {
            super.bind(journal);
            //Log.d("journals " + getCount(), " pos: " + position + "date : " +  getContext().getString(R.string.dateFormat));
            dateTV.setText(UtilsFormat.formatDate(new Date(journal.getDate()), getContext()));
            noteTV.setText(journal.getNote());
            amountTV.setText(UtilsFormat.formatCurrency(journal.getAmount(), getContext()));
            if(journal.getType().equals(Journal.Type.Credit)){
                amountTV.setTextColor(ContextCompat.getColor(getContext(), R.color.red_medium));
                drCrTV.setText(R.string.str_cr);
            }else{
                amountTV.setTextColor(ContextCompat.getColor(getContext(), R.color.green_medium));
                drCrTV.setText( R.string.str_dr);
            }

            attchTV.setText(String.valueOf(mServices.getAttachments(journal.getId()).size()));
        }
    }
}
