package com.ndhunju.dailyjournal.controller.journal;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.io.File;

/**
 * Created by dhunju on 10/5/2015.
 */
public class StoreImageAsync extends AsyncTask<Bitmap, Integer, Boolean> {

    private Activity mActivity;
    private ProgressDialog pd;
    private Callback mCallback;
    private File[] newPicFiles;

    public interface Callback{
        void onFinished(File[] newPicFiles);
    }

    public StoreImageAsync(Activity activity, Callback callback){
        mActivity = activity;
        mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        pd = UtilsView.createProgressDialog(mActivity, "");
    }

    @Override
    protected Boolean doInBackground(Bitmap... bitmaps) {
        Bitmap tempBitmap;
        newPicFiles = new File[bitmaps.length];
        for(int i = 0; i < bitmaps.length; i++){
            newPicFiles[i] = UtilsFile.createImageFile(mActivity);
            UtilsFile.storeImage(bitmaps[i], newPicFiles[i] , mActivity);
            publishProgress((int)i/bitmaps.length);
        }

        return true;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        pd.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        pd.cancel();
        if(aBoolean &&( mCallback != null)){
            mCallback.onFinished(newPicFiles);
        }
    }
}
