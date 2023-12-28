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
import com.ndhunju.dailyjournal.controller.DailyJournalApplication;
import com.ndhunju.dailyjournal.controller.ItemDescriptionAdapter;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.report.AttachmentsReportGenerator;
import com.ndhunju.dailyjournal.service.report.CsvReportGenerator;
import com.ndhunju.dailyjournal.service.report.PdfReportGenerator;
import com.ndhunju.dailyjournal.service.report.ReportGenerator;
import com.ndhunju.dailyjournal.service.report.TextFileReportGenerator;
import com.ndhunju.dailyjournal.util.UtilDownloadManager;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;
import com.ndhunju.dailyjournal.util.UtilsZip;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/** Created by ndhunju on 2/4/18.*/
public class SharePartiesReportAsync  extends AsyncTask<List<Party>, Integer, Boolean> {

    public static final String END_FILE_TYPE = "application/zip";
    private ProgressDialog pd;
    private Context mContext;
    private Type mType;

    public enum Type {
        ZIPPED_TEXT_FILEs,
        ZIPPED_PDFs,
        ZIPPED_PDFs_WITH_ATTACHMENTS,
        ZIPPED_CSVs,
        ZIPPED_IMAGE_ATTACHMENTS
    }

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
        // We want to delete this folder once we zip it.
        // But OS is blocking delete operation on Downloads folder.
        // So use app folder to store the attachments and to zip.
        File toBeZippedFolder = new File(
                UtilsFile.getAppFolder(mContext),
                mContext.getString(R.string.str_share_report)
                        + "-"
                        + UtilsFormat.formatDate(new Date(), UtilsFormat.DATE_FORMAT_FOR_FILE)
        );

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

        File zipFile = new File(
                UtilsFile.getPublicDownloadDir(),
                mContext.getString(R.string.str_report)
                        + "-"
                        + UtilsFile.getZipFileName()
        );

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
                    break;
                case ZIPPED_PDFs:
                    rg = new PdfReportGenerator(mContext, partyList.get(i));
                    success &= rg.getReport(toBeZippedFolder) != null;
                    break;
                case ZIPPED_PDFs_WITH_ATTACHMENTS:
                    rg = new PdfReportGenerator(mContext, partyList.get(i));
                    rg.setShouldAppendAttachments(true);
                    success &= rg.getReport(toBeZippedFolder) != null;
                    break;
                case ZIPPED_CSVs:
                    rg = new CsvReportGenerator(mContext, partyList.get(i));
                    success &= rg.getReport(toBeZippedFolder) != null;
                    break;
                case ZIPPED_IMAGE_ATTACHMENTS:
                    rg = new AttachmentsReportGenerator(mContext, partyList.get(i));
                    // Create Attachments folder
                    String attachmentFolderName = mContext.getString(R.string.str_attachments);
                    File rootAttachmentFolder = new File(toBeZippedFolder, attachmentFolderName);
                    if (!rootAttachmentFolder.exists()) {
                        success &= rootAttachmentFolder.mkdir();
                    }

                    success &= rg.getReport(rootAttachmentFolder) != null;
                    break;
            }

            int finishedPercentage = ((i+1) * 100) / partyList.size();
            publishProgress(finishedPercentage);
        }

        try {
            DailyJournalApplication.postOnUiThread(() -> {
                pd.dismiss();
                pd = new ProgressDialog(mContext);
                pd.setCancelable(false);
                pd.setCanceledOnTouchOutside(false);
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.setMessage(mContext.getString(R.string.msg_zipping));
                pd.show();
            });

            UtilsZip.zip(toBeZippedFolder, zipFile);
            // Turns out we need to empty the directory before calling delete on the directory.
            UtilsFile.deleteDirectory(toBeZippedFolder);
            toBeZippedFolder.delete();
            // Let know that a new file has been created so that it appears in the computer
            MediaScannerConnection.scanFile(
                    mContext,
                    new String[]{zipFile.getAbsolutePath()},
                    null,
                    null
            );
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.str_share_report));
            intent.putExtra(
                    Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(
                            mContext,
                            UtilsFile.getFileSharingAuthority(mContext),
                            zipFile)
            );
            intent.setType(END_FILE_TYPE);
            mContext.startActivity(intent);

            // Notify user that we created a file
            UtilDownloadManager.INSTANCE.notifyUserAboutFileCreation(
                    mContext,
                    zipFile,
                    mContext.getString(
                            R.string.msg_reports_created_title,
                            mContext.getString(R.string.app_name)
                    ),
                    END_FILE_TYPE
            );

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
