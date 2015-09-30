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
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.ledger_row, null);



        TextView idCol = (TextView) convertView.findViewById(R.id.ledger_row_col0);
        TextView dateCol = (TextView) convertView.findViewById(R.id.ledger_row_col1);
        TextView noteCol = (TextView) convertView.findViewById(R.id.ledger_row_col2);
        TextView drCol = (TextView) convertView.findViewById(R.id.ledger_row_col3);
        TextView crCol = (TextView) convertView.findViewById(R.id.ledger_row_col4);

        //Log.d("journals " + getCount(), " pos: " + position + "date : " +  getContext().getString(R.string.dateFormat));

        Journal journal = getItem(position);
        idCol.setText(String.valueOf(getPosition(journal) + 1));
        dateCol.setText(UtilsFormat.formatDate(new Date(journal.getDate()),
                getContext().getString(R.string.dateFormat)));

        boolean showNoteCol = getContext().getResources().getBoolean(R.bool.noteCol);
        if(showNoteCol) {noteCol.setText(journal.getNote());}
        else{noteCol.setVisibility(View.GONE); }

        if (journal.getType().equals(Journal.Type.Debit)) {
            drCol.setText(UtilsFormat.formatCurrency(journal.getAmount()));
            crCol.setText("");
        } else {
            crCol.setText(UtilsFormat.formatCurrency(journal.getAmount()));
            drCol.setText("");
        }

        return convertView;
    }
}
