package com.ndhunju.dailyjournal.service.report;

import android.app.DownloadManager;
import android.content.Context;
import android.media.MediaScannerConnection;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.util.UtilsFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by ndhunju on 12/27/16.
 * This class generates a report in text file
 */

public class TextFileReportGenerator extends ReportGenerator<File> {

    protected static final String FILE_EXT = ".txt";

    public TextFileReportGenerator(Context context, long partyId) {
        super(context, partyId);
    }

    public TextFileReportGenerator(Context context, Party party) {
        super(context, party);
    }


    @Override
    public File getReport(File folder) {
        super.getReport(folder);

        // build the report as needed
        Builder builder = new Builder();
        makeAppBanner(builder);
        makePartySummary(builder);
        builder.appendText(getString(R.string.msg_text_format_issue))
                .writeTextLn()
                .newLine();
        makeReport(builder);

        // write the report in a file
        try{
            // create a new unique file inside the folder if exists. otherwise in public download folder
            File textFile = new File(folder != null && folder.exists()
                    ? folder.getAbsolutePath() : UtilsFile.getPublicDownloadDir(),
                    getSubject() + "-" + String.valueOf(System.currentTimeMillis()).substring(8,12) + FILE_EXT);
            textFile.createNewFile();

            // write to file
            OutputStream os = new FileOutputStream(textFile);
            os.write(builder.asString().getBytes("UTF-8"));
            os.close();

            // notify user
            DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            downloadManager.addCompletedDownload(mContext.getString(R.string.msg_report_created_title, getString(R.string.app_name)),
                    mContext.getString(R.string.msg_report_created_desc, mParty.getName()), true, "text/plain", textFile.getAbsolutePath(), textFile.length(), true);

            //to let know that a new file has been created so that it appears in the computer
            MediaScannerConnection.scanFile(mContext, new String[]{textFile.getAbsolutePath()}, null, null);
            return textFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
