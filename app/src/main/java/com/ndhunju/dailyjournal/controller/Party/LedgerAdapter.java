package com.ndhunju.dailyjournal.controller.Party;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableRow;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.UtilsFormat;

import java.util.Date;
import java.util.List;

/**
 * Created by dhunju on 9/26/2015.
 * This adapter provides view for row of a Ledger
 */
public class LedgerAdapter extends ArrayAdapter<Journal> {


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

            //tag the holder to the view to retrieve it later
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }



        //Log.d("journals " + getCount(), " pos: " + position + "date : " +  getContext().getString(R.string.dateFormat));

        Journal journal = getItem(position);
        holder.idCol.setText(String.valueOf(getPosition(journal) + 1));
        holder.dateCol.setText(UtilsFormat.formatDate(
                new Date(journal.getDate()), getContext().getString(R.string.dateFormat)));


        boolean showNoteCol = getContext().getResources().getBoolean(R.bool.noteCol);
        if(showNoteCol) {holder.noteCol.setText(journal.getNote());}
        else{holder.noteCol.setVisibility(View.GONE); }


        if (journal.getType().equals(Journal.Type.Debit)) {
            holder.drCol.setText(UtilsFormat.formatCurrency(journal.getAmount()));
            holder.crCol.setText("");
        } else {
            holder.crCol.setText(UtilsFormat.formatCurrency(journal.getAmount()));
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
