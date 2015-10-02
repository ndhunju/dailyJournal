package com.ndhunju.dailyjournal.service;

import android.content.Context;
import android.media.MediaScannerConnection;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;

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

    //variables
    Party mParty;
    Context mContext;
    List<Journal> mJournals;

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
        sb.append(ad + newLine());
        sb.append(getString(R.string.str_party) + " : ");
        sb.append(mParty.getName());
        sb.append(newLine());
        sb.append(getString(R.string.str_balance));
        sb.append(UtilsFormat.formatCurrency(mParty.getDebitTotal() - mParty.getCreditTotal()));
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
        sb.append(addSpaces(R.string.str_no, 3) );
        sb.append(addSpaces(R.string.str_date));
        sb.append(addSpaces(R.string.str_dr));
        sb.append(addSpaces(R.string.str_cr));
        sb.append(addSpaces(R.string.str_balance));
        sb.append("\n");

        /**Body Rows **/
        double balance = 0;
        int no = 1;
        for(Journal journal: mJournals){
            sb.append(addSpaces(String.valueOf(no++),3));
            sb.append(addSpaces(UtilsFormat.formatDate(new Date(journal.getDate()), UtilsFormat.DATE_FORMAT)));
            if(journal.getType() == Journal.Type.Debit){
                sb.append(addSpaces(UtilsFormat.formatCurrency(journal.getAmount())));
                sb.append(addSpaces(""));
                balance += journal.getAmount();
                sb.append(addSpaces(UtilsFormat.formatCurrency(balance)));
            }else{
                sb.append(addSpaces(""));
                sb.append(addSpaces(UtilsFormat.formatCurrency(journal.getAmount())));
                balance -= journal.getAmount();
                sb.append(addSpaces(UtilsFormat.formatCurrency(balance)));
            }
        }

        sb.append("\n");

        /**Footer Row **/
        sb.append(addSpaces("", 3));
        sb.append(addSpaces(UtilsFormat.formatDate(new Date(), UtilsFormat.DATE_FORMAT)));
        sb.append(addSpaces(UtilsFormat.formatCurrency(mParty.getDebitTotal())));
        sb.append(addSpaces(UtilsFormat.formatCurrency(mParty.getCreditTotal())));
        sb.append(addSpaces(UtilsFormat.formatCurrency(balance)));

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
            bw.append(getString(R.string.str_party) + " : ");
            bw.append(mParty.getName());
            bw.append(newLine());
            bw.append(getString(R.string.str_balance));
            bw.append(UtilsFormat.formatCurrency(mParty.getDebitTotal() - mParty.getCreditTotal()));
            bw.append(newLine());
            bw.append(newLine());

            /**Header Row **/
            bw.append(addSpaces(R.string.str_no, 3));
            bw.append(addSpaces(R.string.str_date));
            bw.append(addSpaces(R.string.str_dr));
            bw.append(addSpaces(R.string.str_cr));
            bw.append(addSpaces(R.string.str_balance));

            bw.append(newLine());

            /**Body Rows **/
            double balance = 0;
            int no = 1;
            for (Journal journal : mJournals) {
                bw.append(addSpaces(String.valueOf(no++), 3));
                bw.append(addSpaces(UtilsFormat.formatDate(new Date(journal.getDate()), UtilsFormat.DATE_FORMAT)));
                if (journal.getType() == Journal.Type.Debit) {
                    bw.append(addSpaces(UtilsFormat.formatCurrency(journal.getAmount())));
                    bw.append(addSpaces(""));
                    balance += journal.getAmount();
                    bw.append(addSpaces(UtilsFormat.formatCurrency(balance)));
                } else {
                    bw.append(addSpaces(""));
                    bw.append(addSpaces(UtilsFormat.formatCurrency(journal.getAmount())));
                    balance -= journal.getAmount();
                    bw.append(addSpaces(UtilsFormat.formatCurrency(balance)));
                }
                bw.append(newLine());
            }


            /**Footer Row **/
            bw.append(addSpaces("", 3));
            bw.append(addSpaces(R.string.str_total));
            bw.append(addSpaces(UtilsFormat.formatCurrency(mParty.getDebitTotal())));
            bw.append(addSpaces(UtilsFormat.formatCurrency(mParty.getCreditTotal())));
            bw.append(addSpaces(UtilsFormat.formatCurrency(balance)));

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

    public static String newLine(){
        return System.getProperty("line.separator");
    }

    /**Deletes the report file stored in public folder**/
    public boolean deleteReportFile(){
        File reportFile = UtilsFile.createFileInDocumentFolder(getSubject()+ FILE_EXT);
        if(reportFile.exists()) return reportFile.delete();
        else return true;
    }

    //helper method to get string by resource id
    public String getString(int resId){
        return mContext.getString(resId);
    }

    public String addSpaces(int resId){
        return addSpaces(getString(resId));
    }

    public String addSpaces(int resId, int spaces){
        return addSpaces(getString(resId), spaces);
    }

    public static String addSpaces(String str){
        return addSpaces(str, TOTAL_SIZE);
    }

    /**
     * This method adds spaces to a string so that
     * all the string in the ledger are of equal
     * length.
     * @param str
     * @param spaces
     * @return
     */
    public static String addSpaces(String str, int spaces){
        StringBuilder sb = new StringBuilder(str);
        int len = str.length();
        int add =0;
        if(spaces > len ) add = spaces - len;

        for(int i = add; i > 0; i--)
            sb.append(" ");

        sb.append(COL_SEPARATOR);
        return (sb.toString());
    }
}
