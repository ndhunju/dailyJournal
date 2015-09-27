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

        TextView col0 = (TextView) convertView.findViewById(R.id.ledger_row_col0);
        TextView col1 = (TextView) convertView.findViewById(R.id.ledger_row_col1);
        TextView col2 = (TextView) convertView.findViewById(R.id.ledger_row_col2);
        TextView col3 = (TextView) convertView.findViewById(R.id.ledger_row_col3);
        TextView col4 = (TextView) convertView.findViewById(R.id.ledger_row_col4);

        //Log.d("journals " + getCount(), " pos: " + position);

        Journal journal = getItem(position);
        col0.setText(String.valueOf(getPosition(journal)+1));
        col1.setText(UtilsFormat.formatDate(new Date(journal.getDate()), UtilsFormat.DATE_FORMAT));
        col2.setText(journal.getNote());

        if (journal.getType().equals(Journal.Type.Debit)) {
            col3.setText(UtilsFormat.formatCurrency(journal.getAmount()));
            col4.setText("");
        } else {
            col4.setText(UtilsFormat.formatCurrency(journal.getAmount()));
            col3.setText("");
        }
        return convertView;
    }
}
