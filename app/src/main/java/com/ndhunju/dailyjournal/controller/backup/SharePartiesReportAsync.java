package com.ndhunju.dailyjournal.controller.backup;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.ItemDescriptionAdapter;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.report.CsvReportGenerator;
import com.ndhunju.dailyjournal.service.report.PdfReportGenerator;
import com.ndhunju.dailyjournal.service.report.ReportGenerator;
import com.ndhunju.dailyjournal.service.report.TextFileReportGenerator;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsView;
import com.ndhunju.dailyjournal.util.UtilsZip;

import java.io.File;
import java.io.IOException;
import java.util.List;

/** Created by ndhunju on 2/4/18.*/
public class SharePartiesReportAsync  extends AsyncTask<List<Party>, Integer, Boolean> {

    public static final String END_FILE_TYPE = "application/zip";
    private ProgressDialog pd;
    private Context mContext;
    private Type mType;

    public enum Type {ZIPPED_TEXT_FILEs, ZIPPED_PDFs, ZIPPED_PDFs_WITH_ATTACHMENTS, ZIPPED_CSVs}

    public SharePartiesReportAsync(Context con, Type type){
        mContext = con;
        mType = type;
    }

    @Override
    protected void onPreExecute() {
        String msg = String.format(mContext.getString(R.string.msg_creating), mContext.getString(R.string.str_share_report));
        pd= new ProgressDialog(mContext);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage(msg);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
    }

    @Override
    protected Boolean doInBackground(List<Party>... parties) {
        if (parties.length < 1) {
            return false;
        }
        List<Party> partyList = parties[0];
        ReportGenerator rg;
        boolean success = true;
        File destinationFolder = new File(UtilsFile.getPublicDownloadDir());
        File toBeZippedFolder = new File(destinationFolder, mContext.getString(R.string.str_share_report));

        try {
            if (!toBeZippedFolder.exists()) {
                toBeZippedFolder.mkdirs();
            } else if (!toBeZippedFolder.isDirectory()) {
                toBeZippedFolder.delete();
                toBeZippedFolder.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        File zipFile = new File(destinationFolder, mContext.getString(R.string.str_share_report) + UtilsFile.getZipFileName());

        try {
            zipFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < partyList.size() ; i++) {
            switch (mType) {
                default:
                case ZIPPED_TEXT_FILEs:
                    rg = new TextFileReportGenerator(mContext, partyList.get(i));
                    success &= rg.getReport(toBeZippedFolder) != null;
                case ZIPPED_PDFs:
                    rg = new PdfReportGenerator(mContext, partyList.get(i));
                    success &= rg.getReport(toBeZippedFolder) != null;
                    break;
                case ZIPPED_CSVs:
                    rg = new CsvReportGenerator(mContext, partyList.get(i));
                    success &= rg.getReport(toBeZippedFolder) != null;
                    break;
            }

            int finishedPercentage = ((i+1) * 100) / partyList.size();
            publishProgress(finishedPercentage);
        }

        try {
            UtilsZip.zip(toBeZippedFolder, zipFile);
            toBeZippedFolder.delete();
            // let know that a new file has been created so that it appears in the computer
            MediaScannerConnection.scanFile(mContext, new String[]{zipFile.getAbsolutePath()}, null, null);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.str_share_report));
            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(mContext, UtilsFile.getFileSharingAuthority(mContext), zipFile));
            intent.setType(END_FILE_TYPE);
            mContext.startActivity(intent);

            // notify user that we created a file
            DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            downloadManager.addCompletedDownload(mContext.getString(R.string.msg_reports_created_title, mContext.getString(R.string.app_name)),
                    mContext.getString(R.string.msg_reports_created_title, mContext.getString(R.string.app_name)), true, END_FILE_TYPE, zipFile.getAbsolutePath(), zipFile.length(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return success;
    }



    @Override
    protected void onProgressUpdate(final Integer... values) {
        pd.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        pd.cancel();
        String resultMsg =  success ?
                String.format(mContext.getString(R.string.msg_finished), mContext.getString(R.string.str_export_printable))
                : String.format(mContext.getString(R.string.msg_failed), mContext.getString(R.string.str_export_printable));

        UtilsView.alert(mContext, resultMsg);
    }

    //helper
    public static ItemDescriptionAdapter.Item[] getStrTypes(Context context) {
        SharePartiesReportAsync.Type types[] = SharePartiesReportAsync.Type.values();

        String[] strTypes = context.getResources()
                .getStringArray(R.array.options_share_file_types);
        String[] strTypesDescriptions = context.getResources()
                .getStringArray(R.array.options_share_file_types_description);

        if (types.length != strTypes.length) {
            Log.e(
                    SharePartiesReportAsync.class.getSimpleName(),
                    "The length of share parties options does not match with string resource."
            );
        }

        ItemDescriptionAdapter.Item[] items = new ItemDescriptionAdapter.Item[types.length];

        for(int index=0; index < types.length; index++) {
            items[index] = new ItemDescriptionAdapter.Item(
                    strTypes[index],
                    strTypesDescriptions[index]
            );
        }

        return items;
    }
}
