package com.ndhunju.dailyjournal.controller.party;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.util.Date;
import java.util.List;

/**
 * Created by dhunju on 9/26/2015.
 * This adapter provides card view for Ledger
 */
class LedgerCardAdapter extends ArrayAdapter<Journal> {

    Services mService;

    public LedgerCardAdapter(Context context, List<Journal> journals) {
        super(context, R.layout.ledger_card, journals);
        mService = Services.getInstance(context);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.ledger_card, null);
            holder.dateTV = (TextView) convertView.findViewById(R.id.ledger_card_date_tv);
            holder.noteTV = (TextView) convertView.findViewById(R.id.ledger_card_note_tv);
            holder.amountTV = (TextView)convertView.findViewById(R.id.ledger_card_amount_tv);
            holder.drCrTV = (TextView) convertView.findViewById(R.id.ledger_card_drcr_tv);
            holder.attchTV = (TextView) convertView.findViewById(R.id.ledger_card_attch_no_tv);
            //apply common settings
            PartyDetailFragment.addAttributes(TextUtils.TruncateAt.END, holder.dateTV,
                    holder.noteTV);

            //tag the holder to the view to retrieve it later
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }



        //Log.d("journals " + getCount(), " pos: " + position + "date : " +  getContext().getString(R.string.dateFormat));

        Journal journal = getItem(position);
        holder.dateTV.setText(UtilsFormat.formatDate(new Date(journal.getDate()), getContext()));
        holder.noteTV.setText(journal.getNote());
        holder.amountTV.setText(UtilsFormat.formatCurrency(journal.getAmount(), getContext()));
        if(journal.getType().equals(Journal.Type.Credit)){
            holder.amountTV.setTextColor(getContext().getResources().getColor(R.color.red_medium));
            holder.drCrTV.setText(R.string.str_cr);
        }else{
            holder.amountTV.setTextColor(getContext().getResources().getColor(R.color.green_medium));
            holder.drCrTV.setText( R.string.str_dr);
        }

        holder.attchTV.setText(String.valueOf(mService.getAttachments(getItemId(position)).size()));

        return convertView;
    }

    static class ViewHolder{
        TextView dateTV;
        TextView noteTV;
        TextView amountTV;
        TextView drCrTV;
        TextView attchTV;
    }
}
