package com.ndhunju.dailyjournal.viewPager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.journal.StoreImageAsync;
import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;
import com.ndhunju.dailyjournal.util.UtilsZip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Lock/Unlock button is added to the ActionBar. Use it to temporarily disable
 * ViewPager navigation in order to correctly interact with ImageView by
 * gestures. Lock/Unlock state of ViewPager is saved and restored on
 * configuration changes.
 *
 * Julia Zudikova
 */
public class AttachmentViewPagerActivity extends AppCompatActivity {

	private static final String TAG = AttachmentViewPagerActivity.class.getSimpleName();
	private static final int REQUEST_TAKE_PHOTO= 2646;
	private static final int REQUEST_IMAGE  = 4646;


	private static final String ISLOCKED_ARG = "isLocked";
	private AttachmentPagerAdapter attachmentPagerAdapter;
	private ViewPager mViewPager;
	private MenuItem menuLockItem;
	private Services mServices;
	long journalId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_pager);
		mViewPager = (HackyViewPager) findViewById(R.id.view_pager);

		journalId = getIntent().getLongExtra(Constants.KEY_JOURNAL_ID, Constants.ID_NEW_JOURNAL);
		mServices = Services.getInstance(AttachmentViewPagerActivity.this);
		attachmentPagerAdapter = new AttachmentPagerAdapter(mServices.getAttachments(journalId));
		mViewPager.setAdapter(attachmentPagerAdapter);

		if (savedInstanceState != null) {
			boolean isLocked = savedInstanceState.getBoolean(ISLOCKED_ARG,false);
			((HackyViewPager) mViewPager).setLocked(isLocked);
		}

		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		if(toolbar != null) {
			toolbar.setTitle(UtilsFormat.getPartyFromPref(this));
			setSupportActionBar(toolbar);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_viewpager, menu);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menuLockItem = menu.findItem(R.id.viewpager_menu_lock);
		toggleLockBtnTitle();
		menuLockItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				toggleViewPagerScrolling();
				toggleLockBtnTitle();
				return true;
			}
		});
		return super.onPrepareOptionsMenu(menu);
	}




	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()){
			case R.id.viewpager_delete_picture:
				if (attachmentPagerAdapter.getCount() > 0) {
					String msg = String.format(getString(R.string.msg_delete_confirm), getString(R.string.str_attachment));
					UtilsView.alert(AttachmentViewPagerActivity.this, msg, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							int currentItemPos = mViewPager.getCurrentItem();
							UtilsView.toast(AttachmentViewPagerActivity.this, getString(R.string.str_attch_delete));
							mServices.deleteAttachment(attachmentPagerAdapter.getItem(currentItemPos));
							attachmentPagerAdapter.deleteItem(currentItemPos);
							attachmentPagerAdapter.notifyDataSetChanged();
						}
					}, null);
				} else {
					UtilsView.alert(getActivity(), getString(R.string.msg_attachment_count_zero));
				}
				break;

			case R.id.viewpager_new_picture:
				getAttachmentAlertDialogOption().show();
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode){
			case REQUEST_TAKE_PHOTO: //Picture was taken from the Camera App

				if(!UtilsView.showResult(getActivity(), resultCode)){
					return;
				}

				//Since camera cannot save picture in file created inside app's folder
				//1. Create a file in external mServices
				//2. Provide that file's path to camera where it will stream picture data
				//3. Copy the file into internal mServices
				//4. Delete file in external mServices

				File tempPicFile = UtilsFile.createExternalStoragePublicPicture();
				File internalPicFile = UtilsFile.createImageFile(getActivity());

				try{
					FileInputStream picFileIS  = new FileInputStream(tempPicFile);
					FileOutputStream internalFileOS = new FileOutputStream(internalPicFile);
					UtilsZip.copy(picFileIS, internalFileOS);
					picFileIS.close();
					internalFileOS.close();
					//deleting this file works fine. May be files in public folder can be deleted
					String log = tempPicFile.delete()? "Temp pic file deleted" : "Temp file NOT deleted";
					Log.d(TAG, log);

				} catch (IOException e) {
					e.printStackTrace();
				}

				Attachment tempAttch = new Attachment(journalId);
				tempAttch.setPath(internalPicFile.getAbsolutePath());

				attachmentPagerAdapter.addItem(tempAttch);
				mServices.addAttachment(tempAttch);
				attachmentPagerAdapter.notifyDataSetChanged();
				break;

			case REQUEST_IMAGE:  //Image was picked from the storage

				//if not image is selected data is null even tho result code is OK
				if(!UtilsView.showResult(getActivity(), resultCode)){
					return;
				}


				Uri selectedImage = data.getData();
				Bitmap bitmap;
				try {
					bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
				} catch (Exception e) { Log.d(TAG, "couldn't load selected image");
					return;
				}

				new StoreImageAsync(getActivity(), new StoreImageAsync.Callback() {
					@Override
					public void onFinished(File[] newPicFiles) {
						Attachment tempAttch2 = new Attachment(journalId);
						tempAttch2.setPath(newPicFiles[0].getAbsolutePath());
						attachmentPagerAdapter.addItem(tempAttch2);
						mServices.addAttachment(tempAttch2);
						attachmentPagerAdapter.notifyDataSetChanged();
					}
				}).execute(bitmap);

				break;
		}

	}

	private void toggleViewPagerScrolling() {
		if (isViewPagerActive()) {
			((HackyViewPager) mViewPager).toggleLock();
		}
	}

	private void toggleLockBtnTitle() {
		boolean isLocked = false;
		if (isViewPagerActive()) {
			isLocked = ((HackyViewPager) mViewPager).isLocked();
		}
		String title;
		int drawableResId;
		if(isLocked){
			title = getString(R.string.str_lock);
			drawableResId =  R.drawable.ic_lock_black_vector;
		}else{
			title = getString(R.string.str_unlock);
			drawableResId = R.drawable.ic_lock_open_black_vector;
		}
		if (menuLockItem != null) {
			menuLockItem.setTitle(title);
			menuLockItem.setIcon(drawableResId);
		}
	}

	private boolean isViewPagerActive() {
		return (mViewPager != null && mViewPager instanceof HackyViewPager);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (isViewPagerActive()) {
			outState.putBoolean(ISLOCKED_ARG,
					((HackyViewPager) mViewPager).isLocked());
		}
		super.onSaveInstanceState(outState);
	}

	private AlertDialog getAttachmentAlertDialogOption(){
		CharSequence[] options = getResources().getStringArray(R.array.options_attch);
		return new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.str_choose))
				.setItems(options, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent i = null;
						switch (which) {
							case 0: //Take Picture using installed camera app
								Intent takePictureIntent = UtilsFile.getPictureFromCam(getActivity());
								startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
								break;

							case 1: //Select image from the storage
								i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
								startActivityForResult(i, REQUEST_IMAGE);
								break;

						}
					}
				}).create();
	}

	public Activity getActivity(){
		return AttachmentViewPagerActivity.this;
	}
}