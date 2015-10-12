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
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.util.Date;
import java.util.List;

/**
 * Created by dhunju on 9/26/2015.
 * This adapter provides view for row of a Ledger
 */
class LedgerAdapter extends ArrayAdapter<Journal> {

    private String formattedAmt;
    private boolean showNoteCol;

    public LedgerAdapter(Context context, List<Journal> journals) {
        super(context, R.layout.ledger_row, journals);
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.ledger_row, null);
            holder.idCol = (TextView) convertView.findViewById(R.id.ledger_row_col0);
            holder.dateCol = (TextView) convertView.findViewById(R.id.ledger_row_col1);
            holder.noteCol = (TextView) convertView.findViewById(R.id.ledger_row_col2);
            holder.drCol = (TextView) convertView.findViewById(R.id.ledger_row_col3);
            holder.crCol = (TextView) convertView.findViewById(R.id.ledger_row_col4);

            //apply common settings
            PartyDetailFragment.addAttributes(TextUtils.TruncateAt.END, holder.dateCol,
                                                                        holder.noteCol);
            PartyDetailFragment.addAttributes(TextUtils.TruncateAt.START,holder.idCol,
                                                            holder.drCol, holder.crCol);

            //tag the holder to the view to retrieve it later
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }



        //Log.d("journals " + getCount(), " pos: " + position + "date : " +  getContext().getString(R.string.dateFormat));

        Journal journal = getItem(position);
        holder.idCol.setText(String.valueOf(getPosition(journal) + 1));
        holder.dateCol.setText(UtilsFormat.formatDate(
                new Date(journal.getDate()), getContext()));

        //showNoteCol will be false for smaller devices
        showNoteCol = getContext().getResources().getBoolean(R.bool.note_col);
        if(showNoteCol) {holder.noteCol.setText(journal.getNote());}
        else{holder.noteCol.setVisibility(View.GONE); }

        formattedAmt = UtilsFormat.formatDecimal(journal.getAmount(), getContext());

        if (journal.getType().equals(Journal.Type.Debit)) {
            holder.drCol.setText(formattedAmt);
            holder.crCol.setText("");
        } else {
            holder.crCol.setText(formattedAmt);
            holder.drCol.setText("");
        }

        return convertView;
    }

    static class ViewHolder{
        TextView idCol ;
        TextView dateCol ;
        TextView noteCol ;
        TextView drCol;
        TextView crCol;
    }
}
