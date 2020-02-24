package com.ndhunju.dailyjournal.service.report;

import android.content.Context;
import androidx.annotation.Nullable;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created by dhunju on 9/27/2015.
 * This class generates report for respective {@link Party}
 */
public abstract class ReportGenerator<T>{

    // Constants
    protected static final int  DEFAULT_COL_SIZE = 15;
    protected static final String DEFAULT_COL_SEPARATOR = "|";
    protected static final String DEFAULT_GAP_CHAR = " " ;

    // format variables
    protected int mColumnSize = DEFAULT_COL_SIZE; //abstract size for a column
    protected String mColumnSeparator = DEFAULT_COL_SEPARATOR; //column divider
    protected String mGapChar = DEFAULT_GAP_CHAR ; //similar to padding concept

    // model variables
    protected Party mParty;
    protected Context mContext;
    protected List<Journal> mJournals;
    private boolean shouldAppendAttachments;

    /**
     * This class provides basic building blocks/methods
     * to create a report.
     */
    public static class Builder {

        StringBuilder sb;

        public Builder() {
            sb = new StringBuilder();
        }

        public ReportGenerator.Builder newLine() {
            sb.append(ReportGenerator.newLine());
            return this;
        }

        public ReportGenerator.Builder appendText(String string) {
            sb.append(string);
            return this;
        }

        /**
         * Subclass must provide their own implementation if they
         * support adding image
         * @param bitmap : bitmap image to add
         */
        public ReportGenerator.Builder appendImage(Bitmap bitmap) {
            return this;
        }

        /**
         * Writes text so far appended with {@link Builder#appendText(String)} in
         * a line.
         */
        public ReportGenerator.Builder writeTextLn() {
            sb.append(ReportGenerator.newLine());
            return this;
        }

        public String asString() {
            return sb.toString();
        }

    }

    // Constructors
    public ReportGenerator(Context context, long partyId){
        Services services = Services.getInstance(context);
        init(context, services.getParty(partyId), services.getJournals(partyId));
    }

    public ReportGenerator(Context context, Party party){
        init(context, party, Services.getInstance(context).getJournals(party.getId()));
    }

    public ReportGenerator(Context context, Party party, List<Journal> journals){
        init(context, party, journals);
    }

    private void init(Context context, Party party, List<Journal> journals) {
        mJournals = journals;
        mContext = context;
        mParty = party;
    }

    public Party getParty() {
        return mParty;
    }

    public String getReportType() {
        return "";
    }

    public String getSubject(){
        return getString(R.string.str_report) + " " + mParty.getName();
    }

    public boolean shouldAppendAttachments() {
        return shouldAppendAttachments;
    }

    public void setShouldAppendAttachments(boolean shouldAppendAttachments) {
        this.shouldAppendAttachments = shouldAppendAttachments;
    }

    /**
     * Appends party's information in passed {@code sb}
     */
    public void fillPartyInfo(StringBuilder sb) {
        sb.append(getString(R.string.str_party)).append(" : ");
        sb.append(mParty.getName());
        sb.append(newLine());
        sb.append(getString(R.string.str_balance));
        sb.append(UtilsFormat.formatCurrency(mParty.getDebitTotal() - mParty.getCreditTotal(), mContext));
        sb.append(newLine());
    }

    /**
     * Appends app information in passed {@code stringBuilder}
     */
    public void fillAppBanner(StringBuilder stringBuilder) {
        stringBuilder.append(String.format(getString(R.string.msg_generated_by), getString(R.string.app_name)));
        stringBuilder.append(newLine()).append(getString(R.string.link_app));
        stringBuilder.append(newLine()).append(newLine());
    }

    /**
     * This method encapsulates the logic for making the app banner.
     */
    public void makeAppBanner(Builder builder) {
        builder.appendText(String.format(getString(R.string.msg_generated_by), getString(R.string.app_name))).writeTextLn();
        builder.appendText(getString(R.string.link_app)).writeTextLn();
        builder.newLine().newLine().writeTextLn();
    }
    /**
     * This method encapsulates the logic for making the summary for {@link ReportGenerator#mParty}
     */
    public void makePartySummary(Builder builder) {
        builder.appendText(getString(R.string.str_party)).appendText(" : ").appendText(mParty.getName()).writeTextLn();
        builder.appendText(getString(R.string.str_balance)).appendText(" : ")
                .appendText(UtilsFormat.formatCurrency(mParty.getDebitTotal() - mParty.getCreditTotal(), mContext))
                .writeTextLn().newLine();
    }

    /**
     * This method encapsulates teh logic for making the report/ledger for {@link ReportGenerator#mParty}
     */
    public void makeReport(Builder builder){
        /**Header Row **/
        onAppendHeader(builder);

        /**Body Rows **/
        double balance = 0;
        int no = 1;
        for (Journal journal : mJournals) {
            builder.appendText(addGap(String.valueOf(no++), 3));
            builder.appendText(addGap(UtilsFormat.formatDate(new Date(journal.getDate()), mContext)));
            if (journal.getType() == Journal.Type.Debit) {
                builder.appendText(addGapLeft(formatDecimal(journal.getAmount())));
                builder.appendText(addGapLeft(""));
                balance += journal.getAmount();
                builder.appendText(addGapLeft(formatDecimal(balance)));
            } else {
                builder.appendText(addGapLeft(""));
                builder.appendText(addGapLeft(formatDecimal(journal.getAmount())));
                balance -= journal.getAmount();
                builder.appendText(addGapLeft(formatDecimal(balance)));
            }

            onAppendNote(builder, journal);

            builder.writeTextLn();
        }

        /**Footer Row **/
        builder.appendText(addGap("", 3));
        builder.appendText(addGap(R.string.str_total));
        builder.appendText(addGapLeft(formatCurrency(mParty.getDebitTotal())));
        builder.appendText(addGapLeft(formatCurrency(mParty.getCreditTotal())));
        builder.appendText(addGapLeft(formatCurrency(balance)));
        builder.writeTextLn();
    }

    public abstract void addAttachments(Builder builder);

    /**
     * Invoked when {@link ReportGenerator#getReport(File)} is ready to
     * append header in the report.
     */
    public void onAppendHeader(Builder builder) {
        builder.appendText(addGap(R.string.str_num, 3));
        builder.appendText(addGap(R.string.str_date));
        builder.appendText(addGap(R.string.str_dr));
        builder.appendText(addGap(R.string.str_cr));
        builder.appendText(addGap(R.string.str_balance));
        builder.writeTextLn();
    }

    /**
     * Invoked when {@link ReportGenerator#getReport(File)} is ready to
     * append notes for passed {@code journal} in the report.
     */
    public void onAppendNote(Builder builder, Journal journal) {
        // if there is a note, write it on a separate line
        if (!TextUtils.isEmpty(journal.getNote())) {
            builder.writeTextLn();
            builder.appendText(getString(R.string.str_note));
            builder.appendText(" : ");
            builder.appendText(journal.getNote());
        }
    }


    /**
     * Create report file in public document folder
     * and returns the created file
     * @return
     */
    public T getReport(@Nullable File folder) {
        return null;
    }

    public String formatDecimal(double amount) {
        return UtilsFormat.formatDecimal(amount, mContext);
    }

    public String formatCurrency(double amount) {
        return UtilsFormat.formatCurrency(amount, mContext);
    }

    //helper method to get string by resource id
    protected String getString(int resId){
        return mContext.getString(resId);
    }

    public void setColumnSeparator(String columnSeparator) {
        mColumnSeparator = columnSeparator;
    }

    public void setGapChar(String gapChar) {
        mGapChar = gapChar;
    }

    public void setColumnSize(int size) {
        mColumnSize = size;
    }

    protected String addGap(int resId){
        return addGap(getString(resId));
    }

    protected String addGap(int resId, int spaces){
        return addGap(getString(resId), spaces);
    }

    protected String addGap(String str){
        return addGap(str, mColumnSize);
    }

    protected String addGapLeft(String str){
        return addGapLeft(str, mColumnSize);
    }

    /**
     * This method adds spaces to a string so that
     * all the string in the ledger are of equal
     * length.
     */
    protected String addGap(String str, int spaces){
        StringBuilder sb = new StringBuilder(str);
        int len = str.length();
        int add =0;
        if(spaces > len ) add = spaces - len;

        for(int i = add; i > 0; i--)
            sb.append(mGapChar);

        sb.append(mColumnSeparator);
        return (sb.toString());
    }

    protected String addGapLeft(String str, int spaces){
        StringBuilder sb = new StringBuilder();
        int len = str.length();
        int add =0;
        if(spaces > len ) add = spaces - len;

        for(int i = add; i > 0; i--)
            sb.append(mGapChar);
        sb.append(str);
        sb.append(mColumnSeparator);
        return (sb.toString());
    }

    // Static helper methods
    protected static String newLine(){
        return System.getProperty("line.separator");
    }
}
