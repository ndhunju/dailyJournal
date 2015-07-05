package com.ndhunju.dailyjournal.controller;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.model.PictureUtils;
import com.ndhunju.dailyjournal.model.Storage;
import com.ndhunju.dailyjournal.model.Utils;
import com.ndhunju.dailyjournal.viewPager.ViewPagerActivity;

public class JournalFragment extends Fragment {

	public static final String TAG = JournalFragment.class.getCanonicalName();

	Journal tempJournal;
	TextView drCrTv, idTV;
	EditText amountEt, noteEt;
	Button partyBtn, dateBtn;
	PartyListDialog partylistdialog;

	Storage mStorage;
	
	boolean dateChanged, journalChanged = false;
	int journalId;

	public static Fragment newInstance(int journalId, int partyId) {
		Bundle args = new Bundle();
		args.putInt(Utils.KEY_JOURNAL_ID, journalId);
		args.putInt(Utils.KEY_PARTY_ID, partyId);
		JournalFragment newJF = new JournalFragment();
		newJF.setArguments(args);
		return newJF;
	}

	private JournalFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		mStorage = Storage.getInstance(getActivity());
		mStorage.readPartiesFromDB();

		journalId = getArguments().getInt(Utils.KEY_JOURNAL_ID);
		
		setHasOptionsMenu(true);

		if (journalId == Utils.ID_NEW_JOURNAL) {
			tempJournal = new Journal(Calendar.getInstance().getTimeInMillis(), mStorage.getNewJournalId());
			getActivity().setTitle(getString(R.string.str_new_journal));
		} else {
			int partyId = getArguments().getInt(Utils.KEY_PARTY_ID);
			tempJournal = mStorage.getJournal(partyId, journalId).getDeepCopy();
			getActivity().setTitle(mStorage.getParty(partyId).getName());
		}

		View v = inflater.inflate(R.layout.fragment_journal, new LinearLayout(getActivity()));

		idTV = (TextView) v.findViewById(R.id.fragment_journal_id);

		drCrTv = (TextView) v.findViewById(R.id.fragment_journal_dr_cr_tv);
		amountEt = (EditText) v.findViewById(R.id.fragment_home_amount_et);
		amountEt.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if( !s.toString().equals("")){
					tempJournal.setAmount(Double.parseDouble(s.toString()));
					journalChanged = true;
				  }
				}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});

		dateBtn = (Button) v.findViewById(R.id.activity_home_date_btn);
		dateBtn.setText(Utils.parseDate(new Date(tempJournal.getDate()), Utils.DATE_FORMAT)) ;
				//+ " ("	+ Utils.getNepaliDate(tempJournal.getDate()) + ")");
		dateBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerFragment.newInstance(new Date(tempJournal.getDate())).show(
						getActivity().getSupportFragmentManager(), DatePickerFragment.TAG);
				//the result is delivered to OnDialoguePressedOk()
			}
		});

		partyBtn = (Button) v.findViewById(R.id.fragment_home_merchant_btn);
		partyBtn.setText(tempJournal.getPartyId() == Utils.NO_PARTY ? getString(R.string.str_party)
				: mStorage.getParty(tempJournal.getPartyId()).getName());
		partyBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(journalId != Utils.ID_NEW_JOURNAL){
					Utils.alert(getActivity(), getString(R.string.warning_cant_change_party));
					return;
				}
					
				partylistdialog = new PartyListDialog();
				partylistdialog.show(getActivity().getSupportFragmentManager(), PartyListDialog.KEY_PARTY_ID);
				//the result is delivered to OnDialoguePressedOk()
			}
		});

		Button drBtn = (Button) v.findViewById(R.id.fragment_home_debit_btn);
		drBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				journalChanged = true;
				tempJournal.setType(Journal.Type.Debit);
				amountEt.setTextColor(Color.parseColor(Utils.GREEN));
				drCrTv.setTextColor(Color.parseColor(Utils.GREEN));
				drCrTv.setText(getString(R.string.str_dr));
			}
		});

		Button crBtn = (Button) v.findViewById(R.id.fragment_home_credit_btn);
		crBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				journalChanged = true;
				tempJournal.setType(Journal.Type.Credit);
				amountEt.setTextColor(Color.parseColor(Utils.RED));
				drCrTv.setText(getString(R.string.str_cr));
				drCrTv.setTextColor(Color.parseColor(Utils.RED));
			}
		});

		noteEt = (EditText) v.findViewById(R.id.fragment_home_note_et);
		noteEt.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				tempJournal.setNote(s.toString());
				journalChanged = true;
				}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});

		Button attachBtn = (Button) v.findViewById(R.id.fragment_home_attach_btn);
		attachBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CharSequence[] options = getResources().getStringArray(R.array.options_attch);

				new AlertDialog.Builder(getActivity()).setTitle("Choose")
						.setItems(options, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent i = null;
								// if party is not selected warn user
								if (tempJournal.getPartyId() == (Utils.NO_PARTY)) {
									Utils.alert(getActivity(), getString(R.string.warning_select_party));
									return;
								}

								switch (which) {
								case 0:

									Intent takePictureIntent = Utils.getPictureFromCam(getActivity(),tempJournal);
									startActivityForResult(takePictureIntent, Utils.REQUEST_TAKE_PHOTO);

									break;
								case 1:
									i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
									startActivityForResult(i, Utils.REQUEST_IMAGE);
									break;

								case 2:
									
									if(journalId == Utils.ID_NEW_JOURNAL){
										Utils.alert(getActivity(), getString(R.string.warning_see_attachment));
										return;
									}

									i = new Intent(getActivity(), ViewPagerActivity.class);
									if (tempJournal.getAttachmentPaths().size() == 0) {
										Utils.alert(getActivity(), getString(R.string.warning_no_attch));
										return;
									}
									
									i.putExtra(Utils.KEY_ATTACHMENTS, tempJournal.getAttachmentPaths());
									startActivityForResult(i, Utils.REQUEST_CHGD_ATTACHMENTS);
									break;
								}
							}
						}).create().show();
			}
		});

		Button saveJournalBtn = (Button) v.findViewById(R.id.fragment_home_save_btn);
		saveJournalBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				// if party is not selected warn user
				if (tempJournal.getPartyId() == (Utils.NO_PARTY)) {
					Utils.alert(getActivity(), getString(R.string.warning_select_party));
					return;
				}

				Party party = mStorage.getParty(tempJournal.getPartyId());
				
				if (journalId == Utils.ID_NEW_JOURNAL) {
					party.addJournal(tempJournal);
					tempJournal = new Journal(Calendar.getInstance().getTimeInMillis(), mStorage.getNextJournalId());
					setValues(tempJournal);
				}
				else{
					//if the date is changed, delete the old Journal and add it again with changed date
					//in order for the Journal to be in right place in the array. takes more processing power
					if(dateChanged){
						//tempJournal.getDate() != mStorage.getJournal(tempJournal.getPartyId(), tempJournal.getId()).getDate()){
						party.deleteJournal(tempJournal.getId());
						party.addJournal(tempJournal);
					}
					
					Intent i = new Intent();
					if(journalChanged){
						i.putExtra(Utils.NAME_JOURNAL_CHGD, true);
						Journal journal = mStorage.getJournal(tempJournal.getPartyId(), tempJournal.getId()); 
						journal.deepCopyFrom(tempJournal);
						tempJournal = null;
					}
					Utils.toast(getActivity(), "Journal saved.");
					getActivity().setResult(Activity.RESULT_OK, i);
					getActivity().finish();
				}

			}
		});

		Button deleteBtn = (Button) v.findViewById(R.id.fragment_home_delete_btn);
		if (!(journalId == Utils.ID_NEW_JOURNAL)) {
			deleteBtn.setVisibility(View.VISIBLE);
			deleteBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					journalChanged = true;
					mStorage.getParty(tempJournal.getPartyId()).deleteJournal(tempJournal.getId());
					Utils.toast(getActivity(), "Journal Deleted!");
					Intent i = new Intent();
					i.putExtra(Utils.NAME_JOURNAL_CHGD, true);
					getActivity().setResult(Activity.RESULT_OK, i);
					getActivity().finish();
				}
			});
		}
		
		setValues(tempJournal);

		return v;
	}

	public void OnDialogPressedOk(Intent data, int requestCode) {
		switch (requestCode) {
		case PartyListDialog.SELECTED_PARTY_CODE:
			int partyId = data.getIntExtra(PartyListDialog.KEY_PARTY_ID, 0);
			partyBtn.setText(mStorage.getParty(partyId).getName());
			tempJournal.setPartyId(partyId);
			partylistdialog.dismiss();
			break;

		case Utils.REQUEST_CHGED_DATE:
			long newDate = ((Calendar) data.getSerializableExtra(DatePickerFragment.EXTRA_CAL)).getTimeInMillis();
			if(newDate != tempJournal.getDate()){
				tempJournal.setDate(newDate);
				dateChanged = journalChanged = true;
			}
			dateBtn.setText(Utils.parseDate(new Date(tempJournal.getDate()), Utils.DATE_FORMAT));
					//+ " ("+ Utils.getNepaliDate(tempJournal.getDate()) + ")");
			Log.i("Selected Date", String.valueOf(tempJournal.getDate()));
			break;

		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;

		switch (requestCode) {
		case Utils.REQUEST_TAKE_PHOTO:
			// TODO: check if it works
			tempJournal.getAttachmentPaths().add(Utils.getLastPicturePath());
			//tempJournal.getAttachmentPaths().add(data.getData().getPath()); //data is null here
					//((Uri) data.getExtras().get(MediaStore.EXTRA_OUTPUT)).getPath());
			// Bundle extras = data.getExtras();
			// Bitmap imageBitmap = (Bitmap) extras.get("data");
			// mImageView.setImageBitmap(imageBitmap);

			break;
		case Utils.REQUEST_IMAGE:
			Uri selectedImage = data.getData();

			Bitmap bitmap;
			try {
				bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
			} catch (Exception e) { Log.d(this.toString(), "couldn't load selected image");
				return;
			}

			File picFile = Utils.createImageFile(getActivity(), tempJournal);

			PictureUtils.storeImage(bitmap, picFile, getActivity());
			tempJournal.getAttachmentPaths().add(picFile.getAbsolutePath());
			// partyFolder.getAbsolutePath()+ "/" + fileName + ".png");

			break;
			
		case Utils.REQUEST_PICK_JSON_CODE:
				Uri selectedJsonFile = data.getData();
				boolean success = mStorage.parseJSON((selectedJsonFile.getPath()));
				Utils.alert(getActivity(), String.format(getString(R.string.msg_finished), getString(R.string.str_backup_restore),
						success ? getString(R.string.str_finished) : getString(R.string.str_failed)));
				
				//update the id
				tempJournal.setIdFromDB(mStorage.getNextJournalId());
				idTV.setText(getString(R.string.str_id) + Utils.getStringId(tempJournal.getId()));
				
			break;
			
		case Utils.REQUEST_PICK_BACKUP_CODE:
			Uri selectedFile = data.getData();
			mStorage.restoreBackUp(selectedFile.getPath());
			
			break;
			
		case Utils.REQUEST_CHGD_ATTACHMENTS:
			boolean isAttachmentChanged = data.getBooleanExtra(Utils.KEY_ATTACHMENTS_IS_CHGD, false);
			if(isAttachmentChanged){
				tempJournal.setAttachmentPaths(data.getStringArrayListExtra(Utils.KEY_ATTACHMENTS));
			}
			
			break;
		}

	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.home, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*");
		
		switch (id) {
		case R.id.Merchants:
			startActivity(new Intent(getActivity(), PartyListActivity.class));
			break;
			
		case R.id.CreateFullBack:
			mStorage.createFullBackUp();
			break;
			
		case R.id.RestoreBackup:
			startActivityForResult(intent, Utils.REQUEST_PICK_BACKUP_CODE);
			break;
			
		case R.id.Export:
			if(mStorage.createJSONFile())
				Utils.toast(getActivity(), getString(R.string.str_export_json) + " " + getString(R.string.str_finished));
			break;
		case R.id.Import:
			startActivityForResult(intent, Utils.REQUEST_PICK_JSON_CODE);

		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * 
	 */
	public void setValues(Journal tempJournal) {
		idTV.setText("ID: " + Utils.getStringId(tempJournal.getId()));
		amountEt.requestFocus();
		amountEt.setText(tempJournal.getAmount() == 0 ? ".00" : String.valueOf(tempJournal.getAmount()));
		partyBtn.setText(tempJournal.getPartyId() == Utils.NO_PARTY ? getString(R.string.str_party):
			mStorage.getParty(tempJournal.getPartyId()).getName());
		if (tempJournal.getType().equals(Journal.Type.Debit)) {
			drCrTv.setText(getString(R.string.str_dr));
			drCrTv.setTextColor(Color.parseColor(Utils.GREEN)); // green color
			amountEt.setTextColor(Color.parseColor(Utils.GREEN));
		} else {
			drCrTv.setText(getString(R.string.str_cr));
			drCrTv.setTextColor(Color.parseColor(Utils.RED)); // red
			amountEt.setTextColor(Color.parseColor(Utils.RED));
		}
		
		noteEt.setText(tempJournal.getNote());
		
	}
	
	@Override
	public void onPause() {
		mStorage.writeToDB();
		super.onPause();
	}
}
