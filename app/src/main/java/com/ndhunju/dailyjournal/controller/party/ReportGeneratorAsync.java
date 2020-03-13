package com.ndhunju.dailyjournal.controller.party;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.ItemDescriptionAdapter;
import com.ndhunju.dailyjournal.service.report.CsvReportGenerator;
import com.ndhunju.dailyjournal.service.report.PdfReportGenerator;
import com.ndhunju.dailyjournal.service.report.PlainTextReportGenerator;
import com.ndhunju.dailyjournal.service.report.ReportGenerator;
import com.ndhunju.dailyjournal.service.report.TextFileReportGenerator;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.io.File;

/**
 * Created by dhunju on 9/27/2015.
 * This class is a child class of {@link AsyncTask} that generates
 * report for a party by calling {@link ReportGenerator#getReport(File)} ()}
 * This class displays Progress dialog before starting the operation
 * and notifies user once the operation is completed
 */
public class ReportGeneratorAsync extends AsyncTask<Long, Integer, Boolean> {

    private Type mType;
    private Intent intent;
    private ProgressDialog pd;
    private Activity mActivity;
    private String mAction;

    private ReportGenerator rg;
    private File report;

    enum Type{FILE, PDF, PDF_WITH_ATTACHMENTS, CSV, TEXT}


    public ReportGeneratorAsync(Activity activity, Type type, String action){
        mActivity = activity;
        mAction = action;
        mType = type;
    }

    @Override
    protected void onPreExecute() {
        String msg = String.format(mActivity.getString(R.string.msg_creating), mActivity.getString(R.string.str_report));
        pd= new ProgressDialog(mActivity);
        pd.setIndeterminate(true);
        pd.setMessage(msg);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
    }

    @Override
    protected Boolean doInBackground(Long... longs) {
        long partyId = longs[0];
        StringBuilder sb;

        intent = new Intent(mAction);
        boolean shouldAddAttachments = false;

        switch (mType) {
            case FILE:
                rg = new TextFileReportGenerator(mActivity, partyId);
                report = (File) rg.getReport(null);
                // check if report was successfully generated
                if (report == null) return false;
                sb = new StringBuilder();
                rg.fillAppBanner(sb);
                rg.fillPartyInfo(sb);
                intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
                intent.putExtra(Intent.EXTRA_SUBJECT, rg.getSubject());
                intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(mActivity, UtilsFile.getFileSharingAuthority(mActivity), report));
                break;
            case PDF_WITH_ATTACHMENTS:
                shouldAddAttachments = true;
            case PDF:
                rg = new PdfReportGenerator(mActivity, partyId);
                rg.setShouldAppendAttachments(shouldAddAttachments);
                report = (File) rg.getReport(null);
                // check if report was successfully generated
                if (report == null) return false;

                sb = new StringBuilder();
                rg.fillAppBanner(sb);
                rg.fillPartyInfo(sb);
                intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
                intent.putExtra(Intent.EXTRA_SUBJECT, rg.getSubject());
                intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(mActivity, UtilsFile.getFileSharingAuthority(mActivity), report));
                break;
            case CSV:
                rg = new CsvReportGenerator(mActivity, partyId);
                report = (File) rg.getReport(null);
                // check if report was successfully generated
                if (report == null) return false;

                sb = new StringBuilder();
                rg.fillAppBanner(sb);
                rg.fillPartyInfo(sb);
                intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
                intent.putExtra(Intent.EXTRA_SUBJECT, rg.getSubject());
                intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(mActivity, UtilsFile.getFileSharingAuthority(mActivity), report));
                break;
            case TEXT:
                rg = new PlainTextReportGenerator(mActivity, partyId);
                intent.putExtra(Intent.EXTRA_SUBJECT, rg.getSubject());
                intent.putExtra(Intent.EXTRA_TEXT, (String) rg.getReport(null));
                return true;

        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        pd.cancel();
        if(!success){
            UtilsView.alert(mActivity, String.format(mActivity.getString(R.string.msg_failed), mActivity.getString(R.string.str_report)));
            return;
        }

        if (mType != Type.TEXT) {
            // notify user that we created a file
            DownloadManager downloadManager = (DownloadManager) mActivity.getSystemService(Context.DOWNLOAD_SERVICE);
            downloadManager.addCompletedDownload(mActivity.getString(R.string.msg_report_created_title, mActivity.getString(R.string.app_name)),
                    mActivity.getString(R.string.msg_report_created_desc, rg.getParty().getName()), true, rg.getReportType(), report.getAbsolutePath(), report.length(), true);

            // let know that a new file has been created so that it appears in the computer
            MediaScannerConnection.scanFile(mActivity, new String[]{report.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String s, Uri uri) {
                    if (Intent.ACTION_VIEW.equalsIgnoreCase(mAction)) {
                        intent.setDataAndType(uri, rg.getReportType());
                    } else {
                        intent.setType(rg.getReportType());
                    }

                    mActivity.startActivity(Intent.createChooser(intent, mActivity.getString(R.string.str_choose)));
                }
            });
        } else {
            intent.setType(rg.getReportType());
            mActivity.startActivity(Intent.createChooser(intent, mActivity.getString(R.string.str_choose)));
        }
    }

    // Helper Method
    public static ItemDescriptionAdapter.Item[] getStrTypes(Context context) {
        ReportGeneratorAsync.Type types[] = ReportGeneratorAsync.Type.values();

        String[] strTypes = context.getResources()
                .getStringArray(R.array.party_detail_share_types);
        String[] strTypesDescriptions = context.getResources()
                .getStringArray(R.array.party_detail_share_types_description);

        if (types.length != strTypes.length) {
            Log.e(
                    ReportGeneratorAsync.class.getSimpleName(),
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
