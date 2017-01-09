package com.ndhunju.dailyjournal.service.report;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.support.annotation.Nullable;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.util.UtilsFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by dhunju on 9/27/2015.
 * This class can be used to Generate CVS Report files
 */
public class CsvReportGenerator extends ReportGenerator<File>{

    // variables
    protected static final String FILE_EXT = ".csv";

    public CsvReportGenerator(Context context, long partyId) {
        super(context, partyId);
        mColumnSeparator = ",";
        mGapChar = "";
    }

    public CsvReportGenerator(Context context, Party party) {
        super(context, party);
    }

    public CsvReportGenerator(Context context, Party party, List<Journal> journals) {
        super(context, party, journals);
    }

    @Override
    public String getReportType() {
        return "text/csv";
    }

    @Override
    public void onAppendHeader(Builder builder) {
        builder.appendText(addGap(R.string.str_num, 3));
        builder.appendText(addGap(R.string.str_date));
        builder.appendText(addGap(R.string.str_dr));
        builder.appendText(addGap(R.string.str_cr));
        builder.appendText(addGap(R.string.str_balance));
        builder.appendText(addGap(R.string.str_note));
        builder.writeTextLn();
    }

    @Override
    public void onAppendNote(Builder builder, Journal journal) {
        builder.appendText(journal.getNote());
    }

    @Override
    public File getReport(@Nullable File folder) {
        super.getReport(folder);

        // build the report as needed
        Builder builder = new Builder();
        makeReport(builder);

        // write the report in a file
        try{
            // create a new unique file inside the folder if exists. otherwise in public download folder
            File cvsFile = new File(folder != null && folder.exists()
                    ? folder.getAbsolutePath() : UtilsFile.getPublicDownloadDir(),
                    getSubject() + "-" + String.valueOf(System.currentTimeMillis()).substring(8,12) + FILE_EXT);
            cvsFile.createNewFile();

            // write to file
            OutputStream os = new FileOutputStream(cvsFile);
            os.write(builder.asString().getBytes("UTF-8"));
            os.close();

            //to let know that a new file has been created so that it appears in the computer
            MediaScannerConnection.scanFile(mContext, new String[]{cvsFile.getAbsolutePath()}, null, null);
            return cvsFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String formatDecimal(double amount) {
        // csv file shouldn't have comma between numbers
        return String.valueOf(amount);
    }

    @Override
    public String formatCurrency(double amount) {
        // csv file shouldn't have comma between numbers
        return String.valueOf(amount);
    }
}
