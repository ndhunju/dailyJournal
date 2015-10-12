package com.ndhunju.dailyjournal.service;

import android.content.Context;
import android.media.MediaScannerConnection;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by dhunju on 9/27/2015.
 * This class generates report for respective party
 */
public class ReportGenerator{

    private static final int TOTAL_SIZE = 15;
    private static final String FILE_EXT = ".txt";
    private static final String COL_SEPARATOR = "|";
    private static final String GAP_CHAR = " " ;

    //variables
    private Party mParty;
    private Context mContext;
    private List<Journal> mJournals;

    //Constructors
    public ReportGenerator(Context context, long partyId){
        Services services = Services.getInstance(context);
        mJournals = services.getJournals(partyId);
        mParty = services.getParty(partyId);
        mContext = context;
    }

    public ReportGenerator(Context context, Party party){
        mJournals = Services.getInstance(context).getJournals(party.getId());
        mContext = context;
        mParty = party;
    }

    public ReportGenerator(Context context, Party party, List<Journal> journals){
        mJournals = journals;
        mContext = context;
        mParty = party;
    }

    public String getSubject(){
        return getString(R.string.str_report) + " " + mParty.getName();
    }

    /**
     * Returns report header (like banner)
     * @return
     */
    public String getReportHeader(){
        StringBuilder sb = new StringBuilder();
        String ad= String.format(getString(R.string.msg_generated_by), getString(R.string.app_name));
        sb.append(ad).append(newLine()).append(getString(R.string.link_app));
        sb.append(newLine()).append(newLine());
        sb.append(getString(R.string.str_party)).append(" : ");
        sb.append(mParty.getName());
        sb.append(newLine());
        sb.append(getString(R.string.str_balance));
        sb.append(UtilsFormat.formatCurrency(mParty.getDebitTotal() - mParty.getCreditTotal(), mContext));
        sb.append(newLine());
        return sb.toString();
    }

    /**
     * Returns Ledger in a String
     * @return
     */
    public String getReportBody(){
        StringBuilder sb = new StringBuilder();

        /**Header Row **/
        sb.append(addGap(R.string.str_no, 3));
        sb.append(addGap(R.string.str_date));
        sb.append(addGap(R.string.str_dr));
        sb.append(addGap(R.string.str_cr));
        sb.append(addGap(R.string.str_balance));

        sb.append(newLine());

        /**Body Rows **/
        double balance = 0;
        int no = 1;
        for (Journal journal : mJournals) {
            sb.append(addGap(String.valueOf(no++), 3));
            sb.append(addGap(UtilsFormat.formatDate(new Date(journal.getDate()), mContext)));
            if (journal.getType() == Journal.Type.Debit) {
                sb.append(addGapLeft(UtilsFormat.formatDecimal(journal.getAmount(), mContext)));
                sb.append(addGapLeft(""));
                balance += journal.getAmount();
                sb.append(addGapLeft(UtilsFormat.formatDecimal(balance, mContext)));
            } else {
                sb.append(addGapLeft(""));
                sb.append(addGapLeft(UtilsFormat.formatDecimal(journal.getAmount(), mContext)));
                balance -= journal.getAmount();
                sb.append(addGapLeft(UtilsFormat.formatDecimal(balance, mContext)));
            }
            sb.append(newLine());
        }


        /**Footer Row **/
        sb.append(addGap("", 3));
        sb.append(addGap(R.string.str_total));
        sb.append(addGapLeft(UtilsFormat.formatCurrency(mParty.getDebitTotal(), mContext)));
        sb.append(addGapLeft(UtilsFormat.formatCurrency(mParty.getCreditTotal(), mContext)));
        sb.append(addGapLeft(UtilsFormat.formatCurrency(balance, mContext)));

        sb.append(newLine());


        return sb.toString();
    }

    public String getSimpleReportBody(){
        StringBuilder sb = new StringBuilder(newLine());
        /**Body Rows **/
        int no = 1;
        for (Journal journal : mJournals) {
            sb.append(String.valueOf(no++)).append(". ");
            sb.append(UtilsFormat.formatDate(new Date(journal.getDate()), mContext)).append("  ");
            sb.append(UtilsFormat.formatCurrency(journal.getAmount(), mContext)).append(" ");
            if (journal.getType() == Journal.Type.Debit) {
                sb.append(getString(R.string.str_dr)).append(" ");
            } else {
                sb.append(getString(R.string.str_cr)).append(" ");
            }
            sb.append(newLine());
        }

        sb.append(newLine());
        sb.append(getString(R.string.str_total)).append(" ");
        sb.append(getString(R.string.str_dr)).append(" ");
        sb.append(UtilsFormat.formatCurrency(mParty.getDebitTotal(), mContext));
        sb.append(newLine());

        sb.append(getString(R.string.str_total)).append(" ");
        sb.append(getString(R.string.str_cr)).append(" ");
        sb.append(UtilsFormat.formatCurrency(mParty.getCreditTotal(), mContext));

        return sb.toString();
    }

    /**
     * Stores the report in the specified path
     * @param path
     * @return
     */
    public File storeReportFile(String path){

        File reportFile = new File(path, getSubject() + FILE_EXT);
        if (reportFile.exists()) reportFile.delete();

        try{
            reportFile.createNewFile();

            BufferedWriter bw = new BufferedWriter(new FileWriter(reportFile));

            /**Banner **/
            String ad = String.format(getString(R.string.msg_generated_by), getString(R.string.app_name));
            bw.append(ad);

            bw.append(newLine());
            bw.append(getString(R.string.str_party)).append(" : ");
            bw.append(mParty.getName());
            bw.append(newLine());
            bw.append(getString(R.string.str_balance));
            bw.append(UtilsFormat.formatCurrency(mParty.getDebitTotal() - mParty.getCreditTotal(),mContext ));
            bw.append(newLine());
            bw.append(newLine());

            /**Header Row **/
            bw.append(addGap(R.string.str_no, 3));
            bw.append(addGap(R.string.str_date));
            bw.append(addGap(R.string.str_dr));
            bw.append(addGap(R.string.str_cr));
            bw.append(addGap(R.string.str_balance));

            bw.append(newLine());

            /**Body Rows **/
            double balance = 0;
            int no = 1;
            for (Journal journal : mJournals) {
                bw.append(addGap(String.valueOf(no++), 3));
                bw.append(addGap(UtilsFormat.formatDate(new Date(journal.getDate()), mContext)));
                if (journal.getType() == Journal.Type.Debit) {
                    bw.append(addGapLeft(UtilsFormat.formatDecimal(journal.getAmount(), mContext)));
                    bw.append(addGapLeft(""));
                    balance += journal.getAmount();
                    bw.append(addGapLeft(UtilsFormat.formatDecimal(balance, mContext)));
                } else {
                    bw.append(addGapLeft(""));
                    bw.append(addGapLeft(UtilsFormat.formatDecimal(journal.getAmount(), mContext)));
                    balance -= journal.getAmount();
                    bw.append(addGapLeft(UtilsFormat.formatDecimal(balance, mContext)));
                }
                bw.append(newLine());
            }


            /**Footer Row **/
            bw.append(addGap("", 3));
            bw.append(addGap(R.string.str_total));
            bw.append(addGapLeft(UtilsFormat.formatCurrency(mParty.getDebitTotal(), mContext)));
            bw.append(addGapLeft(UtilsFormat.formatCurrency(mParty.getCreditTotal(), mContext)));
            bw.append(addGapLeft(UtilsFormat.formatCurrency(balance, mContext)));

            bw.append(newLine());

            bw.flush();
            bw.close();

            //to let know that a new file has been created so that it appears in the computer
            MediaScannerConnection.scanFile(mContext, new String[]{reportFile.getAbsolutePath()}, null, null);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return reportFile;
    }


    /**
     * Create report file in public document folder
     * and returns the created file
     * @return
     */
    public File getReportFile(){
        String path = UtilsFile.getPublicDocumentDir();
        return storeReportFile(path);
    }

    private static String newLine(){
        return System.getProperty("line.separator");
    }

    /**Deletes the report file stored in public folder**/
    public boolean deleteReportFile(){
        File reportFile = UtilsFile.createFileInDocumentFolder(getSubject()+ FILE_EXT);
        if(reportFile.exists()) return reportFile.delete();
        else return true;
    }

    //helper method to get string by resource id
    private String getString(int resId){
        return mContext.getString(resId);
    }

    private String addGap(int resId){
        return addGap(getString(resId));
    }

    private String addGap(int resId, int spaces){
        return addGap(getString(resId), spaces);
    }

    private static String addGap(String str){
        return addGap(str, TOTAL_SIZE);
    }

    private static String addGapLeft(String str){
        return addGapLeft(str, TOTAL_SIZE);
    }

    /**
     * This method adds spaces to a string so that
     * all the string in the ledger are of equal
     * length.
     * @param str
     * @param spaces
     * @return
     */
    private static String addGap(String str, int spaces){
        StringBuilder sb = new StringBuilder(str);
        int len = str.length();
        int add =0;
        if(spaces > len ) add = spaces - len;

        for(int i = add; i > 0; i--)
            sb.append(GAP_CHAR);

        sb.append(COL_SEPARATOR);
        return (sb.toString());
    }

    private static String addGapLeft(String str, int spaces){
        StringBuilder sb = new StringBuilder();
        int len = str.length();
        int add =0;
        if(spaces > len ) add = spaces - len;

        for(int i = add; i > 0; i--)
            sb.append(GAP_CHAR);
        sb.append(str);
        sb.append(COL_SEPARATOR);
        return (sb.toString());
    }
}
