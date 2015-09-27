package com.ndhunju.dailyjournal.controller.Party;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.ReportGenerator;
import com.ndhunju.dailyjournal.service.UtilsFile;
import com.ndhunju.dailyjournal.service.UtilsView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by dhunju on 9/27/2015.
 */
public class ReportGeneratorAsync extends AsyncTask<Long, Integer, Boolean> {

    ProgressDialog pd;
    Activity mActivity;
    Intent intent;

    public ReportGeneratorAsync(Activity activity){
        mActivity = activity;
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
        ReportGenerator rg = new ReportGenerator(mActivity, longs[0]);
        File report = rg.getReportFile();

        intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, rg.getSubject());
        intent.putExtra(Intent.EXTRA_TEXT, rg.getReportHeader());
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(report));
        intent.setType("text/plain");

        return true;

    }

    @Override
    protected void onPostExecute(Boolean success) {
        pd.cancel();
        if(!success){
            UtilsView.alert(mActivity, String.format(mActivity.getString(R.string.msg_failed), mActivity.getString(R.string.str_report)));
        }

        mActivity.startActivity(Intent.createChooser(intent, mActivity.getString(R.string.str_choose)));
    }
}
