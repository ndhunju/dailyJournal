package com.ndhunju.dailyjournal.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dhunju on 8/16/2015.
 * DirectoryPickerDialogFragment can be used to let the user
 * pick a folder to save a file. It returns select folder's
 * absolute path via callbacks
 */
public class DirectoryPickerDialogFragment extends DialogFragment{

    //Constants
    private static final String KEY_INIT_DIR = "initialDirKey";
    public static final String KEY_SELECTED_DIR = "selectedDir";
    private static final String KEY_REQUEST_CODE = "requestCode";

    //Member variables
    private int mRequestCode;
    private String mCurrentDir = "";
    private String mSdcardDirectory = "";
    private List<String> mSubDirs = null;
    private ControlsLayout mControlsLayout;
    private ArrayAdapter<String> mDirListAdapter = null;
    private OnDialogButtonPressedListener mOnDialogBtnPressedListener = null;

    /**
     * This method should be called to get a new instance of the class. This assures
     * that the state of the DialogFragment is retrieved by Android when there is
     * a configuration change or when the whole screen needs to be recreated
     * @param initAbsoluteDir : Initial directory to show
     * @param requestCode : Result is delivered with same requestCode
     * @return
     */
    public static DirectoryPickerDialogFragment newInstance(String initAbsoluteDir, int requestCode){
        DirectoryPickerDialogFragment instance = new DirectoryPickerDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY_INIT_DIR, initAbsoluteDir);
        args.putInt(KEY_REQUEST_CODE, requestCode);
        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //make sure the calling activity implements the listener class as result in returned to
        //same activity
        try {  mOnDialogBtnPressedListener = (OnDialogButtonPressedListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " +
                    OnDialogButtonPressedListener.class.getCanonicalName());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mSdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        //get the initial directory from intent if provided
        String initDir = getArguments().getString(KEY_INIT_DIR);
        mCurrentDir = (initDir != null) ? initDir : mSdcardDirectory;
        mRequestCode = getArguments().getInt(KEY_REQUEST_CODE);


        //If the passed dir doesn't exist or is not a directory, use the root sdcard directory
        File dirFile = new File(mCurrentDir);
        if (!dirFile.exists() || !dirFile.isDirectory())
            mCurrentDir = mSdcardDirectory;

        //Create a ViewGroup container to add child views including navigation controls
        LinearLayout containerLayout = new LinearLayout(getActivity());
        containerLayout.setOrientation(LinearLayout.VERTICAL);

        //If the ControlsLayout has not been set, use default
        if(mControlsLayout == null)
            mControlsLayout = createDefaultControlLayout(getActivity());

        //Add ControlLayout to container
        containerLayout.addView(mControlsLayout, 0);

        //get subdirectories for current directory
        mSubDirs = getSubDirectories(mCurrentDir);
        //create a ArrayAdapter to display sub directories
        mDirListAdapter = new DirListAdapter<>(getActivity(), mSubDirs);

        //Build AlertDialog to display directories
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(String.format(getString(R.string.msg_choose), getString(R.string.str_folder)))
                    .setSingleChoiceItems(mDirListAdapter, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int item) {
                            // Navigate into the sub-directory
                            mCurrentDir += "/" + ((AlertDialog) dialogInterface).getListView().getAdapter().getItem(item);
                            updateDirectory();
                        }
                    })
                    .setPositiveButton(getString(R.string.str_choose), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Current directory chosen
                            if (mOnDialogBtnPressedListener != null) {
                                // Call registered listener supplied with the chosen directory
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra(KEY_SELECTED_DIR, mCurrentDir);
                                mOnDialogBtnPressedListener.onDialogPositiveBtnClicked(resultIntent, Activity.RESULT_OK, mRequestCode);
                                dismiss();
                            }
                        }
                    })
                    .setNegativeButton(getString(android.R.string.cancel), null)
                    .setView(containerLayout);


        return dialogBuilder.create();
    }

    public ControlsLayout createDefaultControlLayout(Context context){
        ControlsLayout controlLayout = new ControlsLayout(context);
        controlLayout.setCurrentPath(mCurrentDir);
        controlLayout.setOnBackPressedListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // The very top level directory, do nothing
                if (!mCurrentDir.equals(mSdcardDirectory)) {
                    // Navigate back to an upper directory
                    mCurrentDir = new File(mCurrentDir).getParent();
                    updateDirectory();
                }
            }
        });
        controlLayout.setOnNewDirPressedListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText nameInput = new EditText(getActivity());

                // Show new folder name input dialog
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.str_new_folder_name))
                        .setView(nameInput)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String newDirName = nameInput.getText().toString();
                                // Create new directory
                                if (createSubDir(mCurrentDir + "/" + newDirName)) {
                                    // Navigate into the new directory
                                    mCurrentDir += "/" + newDirName;
                                    updateDirectory();
                                } else {
                                    Utils.toast(getActivity(), getString(R.string.str_failed));
                                }
                            }
                        }).setNegativeButton(android.R.string.cancel, null).show();
            }
        });

        return controlLayout;
    }

    /**
     * Returns an instance of {@link com.ndhunju.dailyjournal.controller.DirectoryPickerDialogFragment.ControlsLayout}
     * which can be used to change default icons for New Folder Button {@Link #ControlLayout#setNewBtnImg}
     * and likewise for Back Button. Also OnClickListener can be set for buttons
     * @return
     */
    public ControlsLayout getControlsLayout(Context context){
        if(mControlsLayout == null)
            mControlsLayout = createDefaultControlLayout(context);
        return mControlsLayout;
    }

    private boolean createSubDir(String newDir) {
        File newDirFile = new File(newDir);
        if (!newDirFile.exists()) return newDirFile.mkdir();
        return false;
    }

    private List<String> getSubDirectories(String dir) {
        List<String> dirs = new ArrayList<String>();

        try {
            File dirFile = new File(dir);
            if (!dirFile.exists() || !dirFile.isDirectory()) return dirs;

            for (File file : dirFile.listFiles()) {
                if (file.isDirectory())
                    dirs.add(file.getName());
            }
        } catch (Exception e) { e.printStackTrace(); }

        return dirs;
    }

    private void updateDirectory() {
        mSubDirs.clear();
        mSubDirs.addAll(getSubDirectories(mCurrentDir));
        mControlsLayout.setCurrentPath(mCurrentDir);
        mDirListAdapter.notifyDataSetChanged();
    }

    public class DirListAdapter<String> extends ArrayAdapter<String>{

        public DirListAdapter(Context context, List<String> items) {
            super(context,android.R.layout.select_dialog_item, android.R.id.text1, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            if (v instanceof TextView) {
                // wrap long text
                TextView tv = (TextView) v;
                tv.setBackgroundColor(Color.LTGRAY);
                tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                tv.setEllipsize(null);
            }
            return v;
        }
    }

    /**
     * ControlsLayout consist of three parts/views
     * 1. New Folder Button (By default allows users to create a new folder and has an icon)
     * 2. Current Path TextView (By default it displays absolute path of current folder)
     * 3. Back Button (By default it takes back to parent folder and has an icon)
     */
    public class ControlsLayout extends RelativeLayout{

        private ImageButton mBackImgBtn;
        private ImageButton mNewDirImgBtn;
        private TextView mPathTextView;

        public ControlsLayout(Context context){
            super(context);

            mBackImgBtn = new ImageButton(context);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            mBackImgBtn.setLayoutParams(params);
            mBackImgBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_revert));

            mPathTextView = new TextView(context);
            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params1.addRule(RelativeLayout.CENTER_IN_PARENT);
            mPathTextView.setLayoutParams(params1);
            mPathTextView.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault);
            mPathTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            mNewDirImgBtn = new ImageButton(context);
            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            mNewDirImgBtn.setLayoutParams(params2);
            mNewDirImgBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_add));

            this.addView(mBackImgBtn);
            this.addView(mPathTextView);
            this.addView(mNewDirImgBtn);

        }

        public void setOnBackPressedListener(View.OnClickListener onBackPressedListener){
            if(onBackPressedListener != null) mBackImgBtn.setOnClickListener(onBackPressedListener);
        }

        public void setOnNewDirPressedListener(View.OnClickListener onNewDirPressedListener){
            if(onNewDirPressedListener != null) mNewDirImgBtn.setOnClickListener(onNewDirPressedListener);
        }

        public void setBackBtnImg(Drawable icon){
            mBackImgBtn.setImageDrawable(icon);
        }

        public void setNewBtnImg(Drawable icon){
            mNewDirImgBtn.setImageDrawable(icon);
        }

        public TextView getPathTextView(){
            return mPathTextView;
        }

        public void setCurrentPath(String path){
            mPathTextView.setText(path);
        }

    }
}