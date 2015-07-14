package com.ndhunju.dailyjournal.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
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

import java.io.File;
import java.util.Calendar;
import java.util.Date;

public class JournalFragment extends Fragment {

	public static final String TAG = JournalFragment.class.getCanonicalName();

	//UI Widgets
	TextView drCrTv, idTV;
	Button partyBtn, dateBtn;
	EditText amountEt, noteEt;
	PartyListDialog partylistdialog;


	Storage mStorage;
	Journal tempJournal;
	Party mParty;
	int journalId;
	boolean dateChanged, journalChanged;


	public static Fragment newInstance(int journalId, int partyId) {
		Bundle args = new Bundle();
		args.putInt(Utils.KEY_JOURNAL_ID, journalId);
		args.putInt(Utils.KEY_PARTY_ID, partyId);
		JournalFragment newJF = new JournalFragment();
		newJF.setArguments(args);
		return newJF;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mStorage = Storage.getInstance(getActivity());
		//Load data from the local database
		mStorage.readPartiesFromDB();
		journalId = getArguments().getInt(Utils.KEY_JOURNAL_ID);


		//JournalFragment are usu called in two scenario
		if (journalId == Utils.ID_NEW_JOURNAL) {
			//1. When a new Journal will be entered. Journal needs new Id
			tempJournal = new Journal(Journal.getCurrentId());
			getActivity().setTitle(getString(R.string.str_new_journal));
			//enable menu
			setHasOptionsMenu(true);
		} else {
			//2. When a existing journal of a party is opened. Journal has Id
			int partyId = getArguments().getInt(Utils.KEY_PARTY_ID);
			mParty = mStorage.getParty(partyId);
			tempJournal = mStorage.getJournal(mParty, journalId).getDeepCopy();
			getActivity().setTitle(mParty.getName());
			setHasOptionsMenu(false);
		}

		//Wire Views and Widgets
		View v = inflater.inflate(R.layout.fragment_journal, new LinearLayout(getActivity()));
		idTV = (TextView) v.findViewById(R.id.fragment_journal_id);
		idTV.setText(Utils.getStringId(tempJournal.getId()));

		drCrTv = (TextView) v.findViewById(R.id.fragment_journal_dr_cr_tv);

		amountEt = (EditText) v.findViewById(R.id.fragment_home_amount_et);
		amountEt.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if( !s.toString().equals("")){
					try{
						tempJournal.setAmount( Utils.parseCurrency(s.toString()));
						journalChanged = true;
					}catch(Exception e){
						e.printStackTrace();
						Utils.alert(getActivity(), getString(R.string.warning_format));
					}
				  }
				}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});

		dateBtn = (Button) v.findViewById(R.id.activity_home_date_btn);
		dateBtn.setText(Utils.parseDate(new Date(tempJournal.getDate()), Utils.DATE_FORMAT)) ;
		dateBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerFragment.newInstance(new Date(tempJournal.getDate()))
						.show(getActivity().getSupportFragmentManager(), DatePickerFragment.TAG);
				//the result is delivered to OnDialoguePressedOk()
			}
		});

		partyBtn = (Button) v.findViewById(R.id.fragment_home_merchant_btn);
		partyBtn.setText(mParty == null ? getString(R.string.str_select_party): mParty.getName());
		partyBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (journalId != Utils.ID_NEW_JOURNAL) {
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
                setTextDrCr(tempJournal.getType());
			}
		});

		Button crBtn = (Button) v.findViewById(R.id.fragment_home_credit_btn);
		crBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				journalChanged = true;
				tempJournal.setType(Journal.Type.Credit);
                setTextDrCr(tempJournal.getType());
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
				if (mParty == null) {
					Utils.alert(getActivity(), getString(R.string.warning_select_party));
					return;
				}

				switch(journalId){
					case Utils.ID_NEW_JOURNAL:
						//If the journal is new, add it to the party
						mParty.addJournal(tempJournal);

                        //Save selected values so that user doesn't have to selected them again
                        long selectedDate = tempJournal.getDate();
                        int selectedPartyId = tempJournal.getPartyId();
                        Journal.Type selectedType = tempJournal.getType();

                        //Create new instance of Journal
						tempJournal = new Journal(selectedDate, Journal.incrementCurrentId());
                        tempJournal.setPartyId(selectedPartyId);
                        tempJournal.setType(selectedType);
						setValues(tempJournal, mParty);
						break;

					default:
						//if the date is changed, delete the old Journal and add it again with changed date
						//in order for the Journal to be in right place in the array. takes more processing power
						if(dateChanged){
							mParty.deleteJournal(tempJournal.getId());
							mParty.addJournal(tempJournal);
						}

						Intent i = new Intent();
						if(journalChanged){
							i.putExtra(Utils.NAME_JOURNAL_CHGD, true);
							Journal journal = mStorage.getJournal(mParty, tempJournal.getId());
							journal.deepCopyFrom(tempJournal);
							tempJournal = null;
						}
						Utils.toast(getActivity(), String.format(getString(R.string.msg_saved),
								getString(R.string.str_journal)));
						getActivity().setResult(Activity.RESULT_OK, i);
						getActivity().finish();
						break;
				}
			}
		});

		Button deleteBtn = (Button) v.findViewById(R.id.fragment_home_delete_btn);
		if (!(journalId == Utils.ID_NEW_JOURNAL)) {
			deleteBtn.setVisibility(View.VISIBLE);
			deleteBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String msg = String.format(getString(R.string.msg_delete_confirm), getString(R.string.str_journal));
					Utils.alert(getActivity(), msg, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							journalChanged = true;
							mStorage.getParty(tempJournal.getPartyId()).deleteJournal(tempJournal.getId());
							String msg = String.format(getString(R.string.msg_deleted), getString(R.string.str_journal));
							Utils.toast(getActivity(), msg);
							Intent intent = new Intent();
							intent.putExtra(Utils.NAME_JOURNAL_CHGD, true);
							getActivity().setResult(Activity.RESULT_OK, intent);
							getActivity().finish();
						}
					});
				}
			});
		}
		
		setValues(tempJournal, mParty);

		return v;
	}

	public void OnDialogPressedOk(Intent data, int requestCode) {
		switch (requestCode) {

            case PartyListDialog.SELECTED_PARTY_CODE:
                int partyId = data.getIntExtra(PartyListDialog.KEY_PARTY_ID, 0);
                mParty = mStorage.getParty(partyId);
                partyBtn.setText(mParty.getName());
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
                } catch (Exception e) { Log.d(TAG, "couldn't load selected image");
                    return;
                }

                File picFile = Utils.createImageFile(getActivity(), tempJournal);
                PictureUtils.storeImage(bitmap, picFile, getActivity());
                tempJournal.getAttachmentPaths().add(picFile.getAbsolutePath());
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
		inflater.inflate(R.menu.menu_home, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id) {
            case R.id.Merchants:
                startActivity(new Intent(getActivity(), PartyListActivity.class));
                break;

            case R.id.Tools:
                startActivity(new Intent(getActivity(), ImportExportActivity.class));
                break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Sets value of the UI Widgets based on passed parameters
	 */
	public void setValues(Journal tempJournal, Party party) {
		mParty = party;
		idTV.setText(getString(R.string.str_id) + Utils.getStringId(tempJournal.getId()));
        partyBtn.setText(party == null ? getString(R.string.str_select_party) : party.getName());
        amountEt.setText(tempJournal.getAmount() == 0 ? "" :
				Utils.formatCurrency(tempJournal.getAmount()));
        noteEt.setText(tempJournal.getNote());
        setTextDrCr(tempJournal.getType());
		dateBtn.setText(Utils.parseDate(new Date(tempJournal.getDate()), Utils.DATE_FORMAT));
        amountEt.requestFocus();
	}

    /**
     * Sets the value and color of {@link #drCrTv} and {@link #amountEt} based on passed
     * Journal Type
     * @param journalType
     */
    public void setTextDrCr(Journal.Type journalType){
        if (journalType.equals(Journal.Type.Debit)) {
            drCrTv.setText(getString(R.string.str_dr));
            drCrTv.setTextColor(Color.parseColor(Utils.GREEN));
            amountEt.setTextColor(Color.parseColor(Utils.GREEN));
        } else {
            drCrTv.setText(getString(R.string.str_cr));
            drCrTv.setTextColor(Color.parseColor(Utils.RED));
            amountEt.setTextColor(Color.parseColor(Utils.RED));
        }
    }
	
	@Override
	public void onPause() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mStorage.writeToDB();
            }
        });
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		//update the id and reset all
        if (journalId == Utils.ID_NEW_JOURNAL) {
            tempJournal.setId(Journal.getCurrentId());
			setValues(tempJournal, null);
        }

	}
}
