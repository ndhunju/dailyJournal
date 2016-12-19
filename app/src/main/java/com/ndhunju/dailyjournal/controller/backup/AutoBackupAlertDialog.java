package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.io.File;

/**
 * Created by dhunju on 10/6/2015.
 */
public class AutoBackupAlertDialog {

    private Activity mActivity;

    public AutoBackupAlertDialog(Activity activity) {
        mActivity = activity;
    }

    public AlertDialog create() {

        final File[] backupFiles = UtilsFile.getAutoBackUpFiles(mActivity);

        //get names of each file to supply it to alert dialog
        String[] filesNames = new String[backupFiles.length];
        for(int i = 0 ; i < backupFiles.length; i++){
            filesNames[i] = backupFiles[i].getName();
        }

        //to keep track of which items were checked
        final boolean[] selectedItems = new boolean[backupFiles.length];

        AlertDialog.Builder builder= new AlertDialog.Builder(mActivity)
                .setTitle(mActivity.getString(R.string.str_choose))
                .setNegativeButton(android.R.string.cancel, null)
                .setMultiChoiceItems(filesNames, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        selectedItems[i] = b;
                    }
                })
                .setNegativeButton(R.string.str_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //delete all selected items
                        for (int j = 0; j < backupFiles.length; j++) {
                            if (selectedItems[j]) {
                                UtilsFile.deleteFile(backupFiles[j].getAbsolutePath());
                            }

                            UtilsView.toast(mActivity, mActivity
                                    .getString(R.string.msg_deleted, mActivity.getString(R.string.str_backup)));
                        }
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //restore from first selected file
                        for (int j = 0; j < backupFiles.length; j++) {
                            if (selectedItems[j]) {
                                new RestoreBackUpAsync(mActivity).execute(backupFiles[j].getAbsolutePath());
                                return;
                            }
                        }

                    }
                });

     return   builder.create();
    }
}
