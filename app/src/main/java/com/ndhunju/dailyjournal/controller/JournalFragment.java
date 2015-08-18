package com.ndhunju.dailyjournal.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Fragment;
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
import com.ndhunju.dailyjournal.controller.FolderPicker.OnDialogBtnClickedListener;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.model.Storage;
import com.ndhunju.dailyjournal.model.Utils;
import com.ndhunju.dailyjournal.viewPager.ViewPagerActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class JournalFragment extends Fragment implements OnDialogBtnClickedListener {

    //Constants
	private static final String TAG = JournalFragment.class.getCanonicalName();
    private static final int REQUEST_CODE_IMPORT_OLD_DATA=183;
    private static final int REQUEST_CHGD_ATTACHMENTS=762;
    private static final int REQUEST_CODE_IMPORTED=8653;
    private static final int REQUEST_CHGED_DATE = 6656;
    private static final int REQUEST_CHGD_PARTY = 456;
    private static final int REQUEST_TAKE_PHOTO=2646;
    private static final int REQUEST_IMAGE  = 4646;

	//Declaring UI Widgets variable
    PartyListDialog partylistdialog;
    EditText amountEt, noteEt;
    Button partyBtn,dateBtn;
	TextView drCrTv, idTV;

	//Declaring variables
    boolean dateChanged, journalChanged;
    Journal tempJournal;
    Storage mStorage;
    int journalId;
    Party mParty;

	/**
	 * Returns new instance of a JournalFragment Class based on passed arguments. Implements
	 * Factory Pattern.
	 * @param journalId
	 * @param partyId
	 * @return
	 */
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

		journalId = getArguments().getInt(Utils.KEY_JOURNAL_ID);


		//Check if the old directory created by older version of app (v3.1-) exists or not
		if( !mStorage.isOldDataImported() && Utils.oldAppFolderExist()){
			//Old directory exists
			Utils.alert(getActivity(), getString(R.string.msg_move_old_data), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					//Switch to ImportExportActivity to import old data to new directory
					Intent importOldDataIntent = new Intent(getActivity(), ImportExportActivity.class);
					importOldDataIntent.putExtra(Utils.KEY_IMPORT_OLD_DATA, true);
					startActivityForResult(importOldDataIntent, REQUEST_CODE_IMPORT_OLD_DATA);
				}
			});

		}


		//JournalFragment is usu called in two scenarios
		if (journalId == Utils.ID_NEW_JOURNAL) {
			//1. When a new Journal will be entered. Journal needs new Id
			tempJournal = new Journal(Journal.getCurrentId());
			getActivity().setTitle(getString(R.string.title_activity_journal));
			setHasOptionsMenu(true);	//enable menu
		} else {
			//2. When an existing journal of a mParty is opened. Journal has Id
			int partyId = getArguments().getInt(Utils.KEY_PARTY_ID);
			mParty = mStorage.getParty(partyId);
			tempJournal = mStorage.getJournal(mParty, journalId).clone();
			getActivity().setTitle(mParty.getName());
			setHasOptionsMenu(false);
		}


		//Wire Views and Widgets
		View v = inflater.inflate(R.layout.fragment_journal, new LinearLayout(getActivity()));

		idTV = (TextView) v.findViewById(R.id.fragment_journal_id);
		idTV.setText(Utils.getStringId(tempJournal.getId(), Utils.NUM_OF_DIGITS));

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
		dateBtn.setText(Utils.formatDate(new Date(tempJournal.getDate()), Utils.DATE_FORMAT)) ;
		dateBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerFragment dpf = DatePickerFragment.newInstance(new Date(tempJournal.getDate()), REQUEST_CHGED_DATE);
				dpf.setTargetFragment(JournalFragment.this, REQUEST_CHGED_DATE);
				dpf.show(getActivity().getFragmentManager(), DatePickerFragment.TAG);
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
				partylistdialog = PartyListDialog.newInstance(REQUEST_CHGD_PARTY);
				partylistdialog.setTargetFragment(JournalFragment.this, REQUEST_CHGD_PARTY);
				partylistdialog.show(getActivity().getFragmentManager(), Utils.KEY_PARTY_ID);
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

				// if mParty is not selected warn user
				if (tempJournal.getPartyId() == (Utils.NO_PARTY)) {
					Utils.alert(getActivity(), getString(R.string.warning_select_party));
					return;
				}

				CharSequence[] options = getResources().getStringArray(R.array.options_attch);

				new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.str_choose))
						.setItems(options, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent i = null;
								switch (which) {
								case 0: //Take Picture using installed camera app
									Intent takePictureIntent = Utils.getPictureFromCam(getActivity(),tempJournal);
									startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
									break;

								case 1: //Select image from the mStorage
									i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
									startActivityForResult(i, REQUEST_IMAGE);
									break;

								case 2: //See the attachments
									if(journalId == Utils.ID_NEW_JOURNAL){
										//If it is a new journal, warn user
										Utils.alert(getActivity(), getString(R.string.warning_see_attachment));
										return;
									}

									if (tempJournal.getAttachmentPaths().size() == 0) {
										//No attachments => Notify user
										Utils.alert(getActivity(), getString(R.string.warning_no_attch));
										return;
									}

									//open ViewPagerActivity to view all attachments
									i = new Intent(getActivity(), ViewPagerActivity.class);
									i.putExtra(Utils.KEY_ATTACHMENTS, tempJournal.getAttachmentPaths());
									startActivityForResult(i, REQUEST_CHGD_ATTACHMENTS);
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
				// if mParty is not selected warn user
				if (mParty == null) {
					Utils.alert(getActivity(), getString(R.string.warning_select_party));
					return;
				}

				switch(journalId){
					case Utils.ID_NEW_JOURNAL:
						//If the journal is new, add it to the mParty
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
							i.putExtra(Utils.KEY_JOURNAL_CHGD, true);
							Journal journal = mStorage.getJournal(mParty, tempJournal.getId());
							journal.deepCopyFrom(tempJournal);
							tempJournal = null;
						}

						Utils.toast(getActivity(), String.format(getString(R.string.msg_saved),
								getString(R.string.str_journal)));
						getActivity().setResult(Activity.RESULT_OK, i);
						getActivity().finish(); //Finish the Activity once user is done changing
						break;
				}
			}
		});

		Button deleteBtn = (Button) v.findViewById(R.id.fragment_home_delete_btn);
		if (!(journalId == Utils.ID_NEW_JOURNAL)) {
            //Show this button only for already created Journals
			deleteBtn.setVisibility(View.VISIBLE);
			deleteBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    //Prepare warning msg
					String msg = String.format(getString(R.string.msg_delete_confirm), getString(R.string.str_journal));
                    //Alert user before deleting the Journal
					Utils.alert(getActivity(), msg, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							journalChanged = true;
							mStorage.getParty(tempJournal.getPartyId()).deleteJournal(tempJournal.getId());
							String msg = String.format(getString(R.string.msg_deleted), getString(R.string.str_journal));
							Utils.toast(getActivity(), msg);
							Intent intent = new Intent();
							intent.putExtra(Utils.KEY_JOURNAL_CHGD, true);
							getActivity().setResult(Activity.RESULT_OK, intent);
							getActivity().finish();
						}
					}, null);
				}
			});
		}

        //Refresh values in UI
		setValues(tempJournal, mParty);

		return v;
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //If the result is not success, return
		if (resultCode != Activity.RESULT_OK){
			Utils.alert(getActivity(), String.format(getString(R.string.msg_failed), getString(R.string.str_save)));
			return;
		}

		switch (requestCode) {

            case REQUEST_TAKE_PHOTO: //Picture was taken from the Camera App

				//Since camera cannot save picture in file created inside app's folder
				//1. Create a file in external mStorage
				//2. Provide that file's path to camera where it will stream picture data
				//3. Copy the file into internal mStorage
				//4. Delete file in external mStorage

				File tempPicFile = Utils.createExternalStoragePublicPicture();
				File internalPicFile = Utils.createImageFile(getActivity(), tempJournal, mParty);

				try{
					FileInputStream picFileIS  = new FileInputStream(tempPicFile);
					FileOutputStream internalFileOS = new FileOutputStream(internalPicFile);
					Utils.copy(picFileIS, internalFileOS);
					picFileIS.close();
					internalFileOS.close();
                    //deleting this file works fine. May be files in public folder can be deleted
					String log = tempPicFile.delete()? "Temp pic file deleted" : "Temp file NOT deleted";
                    Log.d(TAG, log);

				} catch (IOException e) {
					e.printStackTrace();
				}

				tempJournal.getAttachmentPaths().add(internalPicFile.getAbsolutePath());
                //tempJournal.getAttachmentPaths().add(data.getData().getPath()); //data is null here
                break;

            case REQUEST_IMAGE:  //Image was picked from the mStorage

                Uri selectedImage = data.getData();
                Bitmap bitmap;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                } catch (Exception e) { Log.d(TAG, "couldn't load selected image");
                    return;
                }

                File picFile = Utils.createImageFile(getActivity(), tempJournal, mParty);
                Utils.storeImage(bitmap, picFile, getActivity());
                tempJournal.getAttachmentPaths().add(picFile.getAbsolutePath());
                break;

            case REQUEST_CHGD_ATTACHMENTS: //Attachments were viewed and maybe changed as well
                boolean isAttachmentChanged = data.getBooleanExtra(Utils.KEY_ATTACHMENTS_IS_CHGD, false);
                if(isAttachmentChanged){
                    //If attachments were changed, update it to the Journal
                    tempJournal.setAttachmentPaths(data.getStringArrayListExtra(Utils.KEY_ATTACHMENTS));
                }

                break;

			case REQUEST_CODE_IMPORTED: //Data were imported from backup file
				boolean imported = data.getBooleanExtra(Utils.KEY_IMPORTED, false);
                //if old data were imported then null the Party. Otherwise sometimes newly added
                //mParty which is erased after restore could be stored in mParty object
				if(imported)    mParty = null;
				break;

			case REQUEST_CODE_IMPORT_OLD_DATA: //Old Data were imported to new directory
				Log.i(TAG, "Importing old data finished");
				break;

        }

	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear(); //When the app crashes, old menu items remain creating duplicates
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
            case R.id.meu_journal_merchants:
                startActivity(new Intent(getActivity(), PartyListActivity.class));
                break;

            case R.id.meu_journal_tools:
                startActivityForResult(new Intent(getActivity(), ImportExportActivity.class), REQUEST_CODE_IMPORTED);
                break;

			case R.id.meu_journal_erase:
				Utils.alert(getActivity(), getString(R.string.msg_erase_all), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						final ProgressDialog pd = new ProgressDialog(getActivity());;
						new AsyncTask<Void, Integer, Boolean>() {
							@Override
							protected void onPreExecute() {
								super.onPreExecute();
								pd.setIndeterminate(true);
								pd.setCancelable(false);
								pd.setMessage(String.format(getString(R.string.msg_deleting), getString(R.string.str_data)));
								pd.show();
								;
							}

							@Override
							protected Boolean doInBackground(Void... voids) {
								return mStorage.eraseAll(getActivity());
							}

							@Override
							protected void onPostExecute(Boolean success) {
								pd.cancel();
								String msg = String.format((success ? getString(R.string.msg_finished) : getString(R.string.msg_failed))
										, getString(R.string.str_erase_all));
								Utils.alert(getActivity(), msg, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
										getActivity().recreate(); //update the view
										getActivity().finish();
									}
								});
								super.onPostExecute(success);
							}
						}.execute();

					}
				}, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						return;
					}
				});

				break;

			case R.id.menu_journal_preference:
				Fragment pf = new MyPreferenceFragment();
				getFragmentManager().beginTransaction()
						.replace(R.id.activity_home_journal_fl, pf )
						.addToBackStack(null)
						.commit();
				break;

		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Sets value of the UI Widgets based on passed parameters
	 */
	public void setValues(Journal tempJournal, Party party) {
		mParty = party;
		idTV.setText(getString(R.string.str_id) + Utils.getStringId(tempJournal.getId(), Utils.NUM_OF_DIGITS));
        partyBtn.setText(party == null ? getString(R.string.str_select_party) : party.getName());
        amountEt.setText(tempJournal.getAmount() == 0 ? "" :
				Utils.formatCurrency(tempJournal.getAmount()));
        noteEt.setText(tempJournal.getNote());
        setTextDrCr(tempJournal.getType());
		dateBtn.setText(Utils.formatDate(new Date(tempJournal.getDate()), Utils.DATE_FORMAT));
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
				//Save data to local sql database
				mStorage.writeToDB();
			}
		});

		//update pass code time
		LockScreenActivity.updatePasscodeTime();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		//update the id and reset all
        if (journalId == Utils.ID_NEW_JOURNAL) {
            tempJournal.setId(Journal.getCurrentId());
			setValues(tempJournal, mParty);
        }
		//check pin code lock
		LockScreenActivity.checkPassCode(getActivity());
	}


	@Override
	public void onDialogBtnClicked(Intent data, int whichBtn, int result, int requestCode) {

		switch (requestCode) {

			case REQUEST_CHGD_PARTY: //A Party is selected
				int partyId = data.getIntExtra(Utils.KEY_PARTY_ID, 0);
				mParty = mStorage.getParty(partyId);
				partyBtn.setText(mParty.getName());
				tempJournal.setPartyId(partyId);
				partylistdialog.dismiss();
				break;

			case REQUEST_CHGED_DATE: //A Date is selected
				long newDate = ((Calendar) data.getSerializableExtra(DatePickerFragment.EXTRA_CAL)).getTimeInMillis();
				if(newDate != tempJournal.getDate()){
					//Set journalChanged to true is the date is changed. Journal needs to be reordered
					tempJournal.setDate(newDate);
					dateChanged = journalChanged = true;
				}

				dateBtn.setText(Utils.formatDate(new Date(tempJournal.getDate()), Utils.DATE_FORMAT));
				Log.d("Selected Date", String.valueOf(tempJournal.getDate()));
				break;

		}

	}
}
