package com.ndhunju.dailyjournal.service.report;

import android.content.Context;
import android.support.annotation.Nullable;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created by ndhunju on 12/27/16.
 * This class generates report as a String in plain text.
 */

public class PlainTextReportGenerator extends ReportGenerator<String> {

    public PlainTextReportGenerator(Context context, long partyId) {
        super(context, partyId);
    }

    public PlainTextReportGenerator(Context context, Party party) {
        super(context, party);
    }

    public PlainTextReportGenerator(Context context, Party party, List<Journal> journals) {
        super(context, party, journals);
    }

    @Override
    public void makeReport(Builder builder) {
        builder.newLine();

        /**Body Rows **/
        int no = 1;
        for (Journal journal : mJournals) {
            builder.appendText(String.valueOf(no++)).appendText(". ");
            builder.appendText(UtilsFormat.formatDate(new Date(journal.getDate()), mContext)).appendText("  ");
            builder.appendText(UtilsFormat.formatCurrency(journal.getAmount(), mContext)).appendText(" ");
            if (journal.getType() == Journal.Type.Debit) {
                builder.appendText(getString(R.string.str_dr)).appendText(" ");
            } else {
                builder.appendText(getString(R.string.str_cr)).appendText(" ");
            }
            builder.appendText(getString(R.string.str_note)).appendText(": ").appendText(journal.getNote());
            builder.appendText(newLine());
        }

        builder.appendText(newLine());
        builder.appendText(getString(R.string.str_total)).appendText(" ");
        builder.appendText(getString(R.string.str_dr)).appendText(" ");
        builder.appendText(UtilsFormat.formatCurrency(mParty.getDebitTotal(), mContext));
        builder.appendText(newLine());

        builder.appendText(getString(R.string.str_total)).appendText(" ");
        builder.appendText(getString(R.string.str_cr)).appendText(" ");
        builder.appendText(UtilsFormat.formatCurrency(mParty.getCreditTotal(), mContext));

    }

    @Override
    public String getReport(@Nullable File folder) {
        super.getReport(folder);

        // build the report as needed
        Builder builder = new Builder();
        makeAppBanner(builder);
        makePartySummary(builder);
        makeReport(builder);

        return builder.asString();
    }

    @Override
    public String getReportType() {
        return "text/plain";
    }
}
