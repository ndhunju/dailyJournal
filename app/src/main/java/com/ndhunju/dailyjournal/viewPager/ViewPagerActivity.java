package com.ndhunju.dailyjournal.viewPager;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Utils;

/**
 * Lock/Unlock button is added to the ActionBar. Use it to temporarily disable
 * ViewPager navigation in order to correctly interact with ImageView by
 * gestures. Lock/Unlock state of ViewPager is saved and restored on
 * configuration changes.
 *
 * Julia Zudikova
 */
public class ViewPagerActivity extends FragmentActivity {
	private static final String ISLOCKED_ARG = "isLocked";
	private ViewPager mViewPager;
	private MenuItem menuLockItem;
	ArrayList<String> filePaths; 
	int merchantId, journalId;
	boolean attachmentChanged;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_pager);
		
		attachmentChanged = false;
		filePaths = getIntent().getStringArrayListExtra(Utils.KEY_ATTACHMENTS);
		
		/*merchantId = getIntent().getIntExtra(PartyListDialog.KEY_PARTY_ID, Utils.NO_PARTY);
		journalId = getIntent().getIntExtra(Utils.KEY_JOURNAL_ID, 0);
		filePaths = Storage.getInstance(ViewPagerActivity.this).getJournal(merchantId, journalId).getAttachmentPaths();
*/		
		mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
		setContentView(mViewPager);
		mViewPager.setAdapter(new SamplePagerAdapter(filePaths));
		if (savedInstanceState != null) {
			boolean isLocked = savedInstanceState.getBoolean(ISLOCKED_ARG,false);
			((HackyViewPager) mViewPager).setLocked(isLocked);
		}
	}

	static class SamplePagerAdapter extends PagerAdapter {
		
		private static ArrayList<String> sDrawables = null;
		
		public SamplePagerAdapter(ArrayList<String> imageFilePaths) {
			sDrawables = imageFilePaths;
		}

		@Override
		public int getCount() {
			return sDrawables.size();
		}
		
		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());
			photoView.setImageDrawable(Drawable.createFromPath(sDrawables.get(position)));
			// Now just add PhotoView to ViewPager and return it
			container.addView(photoView, LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_viewpager, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menuLockItem = menu.findItem(R.id.menu_lock);
		toggleLockBtnTitle();
		menuLockItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				toggleViewPagerScrolling();
				toggleLockBtnTitle();
				return true;
			}
		});
		
		MenuItem menuDeletePic = menu.findItem(R.id.delete_picture);
		menuDeletePic.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				String msg = String.format(getString(R.string.msg_delete_confirm), getString(R.string.str_attachment));
				Utils.alert(ViewPagerActivity.this, msg, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						int currentItemPos = mViewPager.getCurrentItem();
						if(new File(filePaths.get(currentItemPos)).delete())
							filePaths.remove(currentItemPos);//doesn't delete from the journal
						Utils.toast(ViewPagerActivity.this, getString(R.string.str_attch_delete));
						((SamplePagerAdapter)mViewPager.getAdapter()).notifyDataSetChanged();
						attachmentChanged = true;

					}
				}, null);
				return true;
			}
		});
		return super.onPrepareOptionsMenu(menu);
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
		String title = (isLocked) ? "Unlock"
				: "Lock";
		if (menuLockItem != null) {
			menuLockItem.setTitle(title);
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
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent();
		i.putExtra(Utils.KEY_ATTACHMENTS_IS_CHGD, attachmentChanged);
		if(attachmentChanged) i.putStringArrayListExtra(Utils.KEY_ATTACHMENTS, filePaths);
		setResult(Activity.RESULT_OK, i);
		super.onBackPressed();
	}
}