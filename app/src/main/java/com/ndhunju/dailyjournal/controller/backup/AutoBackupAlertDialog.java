package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.app.AlertDialog;

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
                .setMultiChoiceItems(
                        filesNames,
                        null,
                        (dialogInterface, i, b) -> selectedItems[i] = b
                )
                .setNegativeButton(R.string.str_delete, (dialogInterface, i) -> {
                    // Delete all selected items
                    for (int j = 0; j < backupFiles.length; j++) {
                        if (selectedItems[j]) {
                            UtilsFile.deleteFile(backupFiles[j].getAbsolutePath());
                        }

                        UtilsView.toast(mActivity, mActivity
                                .getString(
                                        R.string.msg_deleted,
                                        mActivity.getString(R.string.str_backup))
                        );
                    }
                })
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {

                    int selectedItemCount = 0;
                    int firstSelectedItemIndex = -1;

                    for (int j = 0; j < backupFiles.length; j++) {
                        if (selectedItems[j]) {
                            selectedItemCount++;
                            if (firstSelectedItemIndex == -1) {
                                firstSelectedItemIndex = j;
                            }
                        }
                    }

                    // Show error message if user selects 0 or 1+ items
                    if (selectedItemCount > 1 || selectedItemCount < 1) {
                        UtilsView.alert(
                                AutoBackupAlertDialog.this.mActivity,
                                mActivity.getString(R.string.error_msg_invalid_backup_file_count_selected)
                        );
                        return;
                    }

                    // Backup the first selected item. There should be only one at this point
                    if (selectedItems[firstSelectedItemIndex]) {
                        new RestoreBackUpAsync(mActivity).execute(
                                backupFiles[firstSelectedItemIndex].getAbsolutePath()
                        );
                        return;
                    }

                });

     return   builder.create();
    }
}
