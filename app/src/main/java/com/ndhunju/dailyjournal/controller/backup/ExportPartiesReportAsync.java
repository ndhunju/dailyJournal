package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.ReportGenerator;
import com.ndhunju.dailyjournal.util.UtilsView;

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

    public ExportPartiesReportAsync(Activity activity, String path){
        mActivity = activity;
        mPath = path;
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
        for(int i = 0; i < partyList.size() ; i++){
            ReportGenerator rg = new ReportGenerator(mActivity, partyList.get(i));
            rg.storeReportFile(mPath);
            publishProgress(i / partyList.size());
        }
        return true;

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
}
