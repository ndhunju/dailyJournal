package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.report.CsvReportGenerator;
import com.ndhunju.dailyjournal.service.report.PdfReportGenerator;
import com.ndhunju.dailyjournal.service.report.ReportGenerator;
import com.ndhunju.dailyjournal.service.report.TextFileReportGenerator;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.io.File;
import java.util.List;

/**
 * Created by dhunju on 9/27/2015.
 * This class is a child class of {@link AsyncTask} that creates
 * reports for each parties in the list. Reports are stored in
 * the directory selected by the user. This
 * class displays Progress dialog before starting the operation
 * and notifies user once the operation is completed
 */
public class ExportPartiesReportAsync extends AsyncTask<List<Party>, Integer, Boolean> {

    private ProgressDialog pd;
    private Activity mActivity;
    private String mPath;           //path to save the report
    private Type mType;

    public static enum Type{FILE, PDF, CSV}

    public ExportPartiesReportAsync(Activity activity, String path, Type type){
        mActivity = activity;
        mPath = path;
        mType = type;
    }

    @Override
    protected void onPreExecute() {
        String msg = String.format(mActivity.getString(R.string.msg_creating), mActivity.getString(R.string.str_report));
        pd= new ProgressDialog(mActivity);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage(msg);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
    }

    @Override
    protected Boolean doInBackground(List<Party>... parties) {
        List<Party> partyList = parties[0];
        ReportGenerator rg;
        boolean success = true;
        for(int i = 0; i < partyList.size() ; i++) {
            switch (mType) {
                case FILE:
                    rg = new TextFileReportGenerator(mActivity, partyList.get(i));
                    success &= rg.getReport(new File(mPath)) != null;
                case PDF:
                    rg = new PdfReportGenerator(mActivity, partyList.get(i));
                    success &= rg.getReport(new File(mPath)) != null;
                    break;
                case CSV:
                    rg = new CsvReportGenerator(mActivity, partyList.get(i));
                    success &= rg.getReport(new File(mPath)) != null;
                    break;
            }

            publishProgress(i / partyList.size());
        }
        return success;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        pd.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        pd.cancel();
        String resultMsg =  success ?
                String.format(mActivity.getString(R.string.msg_finished), mActivity.getString(R.string.str_export_printable))
                : String.format(mActivity.getString(R.string.msg_failed), mActivity.getString(R.string.str_export_printable));

        UtilsView.alert(mActivity, resultMsg);
    }

    //helper
    public static String[] getStrTypes(){
        Type types[] = Type.values();
        String[] strTypes = new String[types.length];
        for(int index=0; index < types.length;index++)
            strTypes[index] = types[index].name();
        return strTypes;
    }
}
