package com.ndhunju.dailyjournal.controller.Journal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
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
import com.ndhunju.dailyjournal.controller.DatePickerFragment;
import com.ndhunju.dailyjournal.controller.FolderPicker.OnDialogBtnClickedListener;
import com.ndhunju.dailyjournal.controller.ImportExport.EraseAllAsyncTask;
import com.ndhunju.dailyjournal.controller.ImportExport.ImportExportActivity;
import com.ndhunju.dailyjournal.controller.LockScreenActivity;
import com.ndhunju.dailyjournal.controller.MyPreferenceFragment;
import com.ndhunju.dailyjournal.controller.Party.PartyListActivity;
import com.ndhunju.dailyjournal.controller.Party.PartyListDialog;
import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.service.UtilsFile;
import com.ndhunju.dailyjournal.service.UtilsFormat;
import com.ndhunju.dailyjournal.service.UtilsView;
import com.ndhunju.dailyjournal.service.UtilsZip;
import com.ndhunju.dailyjournal.viewPager.ViewPagerActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class JournalFragment extends Fragment implements OnDialogBtnClickedListener {

    //Constants
	private static final String TAG = JournalFragment.class.getSimpleName();
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
    Services mServices;
    long journalId;
    Party mParty;

	/**
	 * Returns new instance of a JournalFragment Class based on passed arguments. Implements
	 * Factory Pattern.
	 * @param journalId
	 * @param partyId
	 * @return
	 */
	public static Fragment newInstance(long journalId, long partyId) {
		Bundle args = new Bundle();
		args.putLong(Constants.KEY_JOURNAL_ID, journalId);
		args.putLong(Constants.KEY_PARTY_ID, partyId);
		JournalFragment newJF = new JournalFragment();
		newJF.setArguments(args);
		return newJF;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mServices = Services.getInstance(getActivity());

		journalId = getArguments().getLong(Constants.KEY_JOURNAL_ID);


		//Check if the old directory created by older version of app (v3.1-) exists or not
		if( !mServices.isOldDataImported() && UtilsFile.oldAppFolderExist()){
			//Old directory exists
			UtilsView.alert(getActivity(), getString(R.string.msg_move_old_data), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					//Switch to ImportExportActivity to import old data to new directory
					Intent importOldDataIntent = new Intent(getActivity(), ImportExportActivity.class);
					importOldDataIntent.putExtra(Constants.KEY_IMPORT_OLD_DATA, true);
					startActivityForResult(importOldDataIntent, REQUEST_CODE_IMPORT_OLD_DATA);
				}
			});

		}


		//JournalFragment is usu called in two scenarios
		if (journalId == Constants.ID_NEW_JOURNAL) {
			//1. When a new Journal will be entered. Journal needs new Id
			tempJournal = new Journal(Constants.NO_PARTY);
			getActivity().setTitle(getString(R.string.title_activity_journal));
			setHasOptionsMenu(true);	//enable menu
		} else {
			//2. When an existing journal of a mParty is opened. Journal has Id
			long partyId = getArguments().getLong(Constants.KEY_PARTY_ID);
			mParty = mServices.getParty(partyId);
			tempJournal = mServices.getJournal(journalId).clone();
			getActivity().setTitle(mParty.getName());
			setHasOptionsMenu(false);
		}


		//Wire Views and Widgets
		View v = inflater.inflate(R.layout.fragment_journal, new LinearLayout(getActivity()));

		idTV = (TextView) v.findViewById(R.id.fragment_journal_id);
		idTV.setText(UtilsFormat.getStringId(tempJournal.getId(), UtilsFormat.NUM_OF_DIGITS));

		drCrTv = (TextView) v.findViewById(R.id.fragment_journal_dr_cr_tv);

		amountEt = (EditText) v.findViewById(R.id.fragment_home_amount_et);
		amountEt.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if( !s.toString().equals("")){
					try{
						tempJournal.setAmount( UtilsFormat.parseCurrency(s.toString()));
						journalChanged = true;
					}catch(Exception e){
						e.printStackTrace();
						UtilsView.alert(getActivity(), getString(R.string.warning_format));
					}
				  }
				}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});

		dateBtn = (Button) v.findViewById(R.id.activity_home_date_btn);
		dateBtn.setText(UtilsFormat.formatDate(new Date(tempJournal.getDate()), UtilsFormat.DATE_FORMAT)) ;
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
				if (journalId != Constants.ID_NEW_JOURNAL) {
					UtilsView.alert(getActivity(), getString(R.string.warning_cant_change_party));
					return;
				}
				partylistdialog = PartyListDialog.newInstance(REQUEST_CHGD_PARTY);
				partylistdialog.setTargetFragment(JournalFragment.this, REQUEST_CHGD_PARTY);
				partylistdialog.show(getActivity().getFragmentManager(), Constants.KEY_PARTY_ID);
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
				if (tempJournal.getPartyId() == (Constants.NO_PARTY)) {
					UtilsView.alert(getActivity(), getString(R.string.warning_select_party));
					return;
				}
				getAttachmetnAlertDialogOption().show();
			}
		});

		Button saveJournalBtn = (Button) v.findViewById(R.id.fragment_home_save_btn);
		saveJournalBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// if mParty is not selected warn user
				if (mParty == null) {
					UtilsView.alert(getActivity(), getString(R.string.warning_select_party));
					return;
				}

				if(journalId == Constants.ID_NEW_JOURNAL) {
					//If the journal is new, add it to the mParty
					mServices.addJournal(tempJournal);

					//Save selected values so that user doesn't have to selected them again
					long selectedDate = tempJournal.getDate();
					long selectedPartyId = tempJournal.getPartyId();
					Journal.Type selectedType = tempJournal.getType();

					//Create new instance of Journal
					tempJournal = new Journal(selectedDate);
					tempJournal.setPartyId(selectedPartyId);
					tempJournal.setType(selectedType);
					setValues(tempJournal, mParty);

				}else{
						Intent i = new Intent();
						if(journalChanged){
							i.putExtra(Constants.KEY_JOURNAL_CHGD, true);
							mServices.updateJournal(tempJournal);
							tempJournal = null;
						}


						getActivity().setResult(Activity.RESULT_OK, i);
						getActivity().finish(); //Finish the Activity once user is done changing
				}

				UtilsView.toast(getActivity(), String.format(getString(R.string.msg_saved),
						getString(R.string.str_journal)));
			}
		});

		Button deleteBtn = (Button) v.findViewById(R.id.fragment_home_delete_btn);
		if (!(journalId == Constants.ID_NEW_JOURNAL)) {
            //Show this button only for already created Journals
			deleteBtn.setVisibility(View.VISIBLE);
			deleteBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    //Prepare warning msg
					String msg = String.format(getString(R.string.msg_delete_confirm), getString(R.string.str_journal));
                    //Alert user before deleting the Journal
					UtilsView.alert(getActivity(), msg, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							journalChanged = true;
							mServices.deleteJournal(tempJournal);
							String msg = String.format(getString(R.string.msg_deleted), getString(R.string.str_journal));
							UtilsView.toast(getActivity(), msg);
							Intent intent = new Intent();
							intent.putExtra(Constants.KEY_JOURNAL_CHGD, true);
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
			UtilsView.alert(getActivity(), String.format(getString(R.string.msg_failed), getString(R.string.str_save)));
			return;
		}

		switch (requestCode) {

            case REQUEST_TAKE_PHOTO: //Picture was taken from the Camera App

				//Since camera cannot save picture in file created inside app's folder
				//1. Create a file in external mServices
				//2. Provide that file's path to camera where it will stream picture data
				//3. Copy the file into internal mServices
				//4. Delete file in external mServices

				File tempPicFile = UtilsFile.createExternalStoragePublicPicture();
				File internalPicFile = UtilsFile.createImageFile(getActivity(), tempJournal, mParty);

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

				Attachment tempAttch = new Attachment(tempJournal.getId());
				tempAttch.setPath(internalPicFile.getAbsolutePath());
				mServices.addAttachment(tempAttch);
                //tempJournal.getAttachmentPaths().add(data.getData().getPath()); //data is null here
                break;

            case REQUEST_IMAGE:  //Image was picked from the mServices

                Uri selectedImage = data.getData();
                Bitmap bitmap;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                } catch (Exception e) { Log.d(TAG, "couldn't load selected image");
                    return;
                }

                File picFile = UtilsFile.createImageFile(getActivity(), tempJournal, mParty);
                UtilsFile.storeImage(bitmap, picFile, getActivity());
				Attachment tempAttch2 = new Attachment(tempJournal.getId());
				tempAttch2.setPath(picFile.getAbsolutePath());
				mServices.addAttachment(tempAttch2);

                break;

            case REQUEST_CHGD_ATTACHMENTS: //Attachments were viewed and maybe changed as well
                boolean isAttachmentChanged = data.getBooleanExtra(Constants.KEY_ATTACHMENTS_IS_CHGD, false);
                if(isAttachmentChanged){
                    //If attachments were changed, update it
					ArrayList<String> filePath = data.getStringArrayListExtra(Constants.KEY_ATTACHMENTS);
					mServices.deleteAllAttachments(tempJournal.getId());
					Attachment attachment;
					for(String s : filePath){
						attachment = new Attachment(tempJournal.getId());
						attachment.setPath(s);
						mServices.addAttachment(attachment);
					}
                }

                break;

			case REQUEST_CODE_IMPORTED: //Data were imported from backup file
                //if old data were imported then null the Party. Otherwise sometimes newly added
                //mParty which is erased after restore could be stored in mParty object
				mParty = null;
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
				UtilsView.alert(getActivity(), getString(R.string.msg_erase_all), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						new EraseAllAsyncTask(getActivity()).execute();
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
		idTV.setText(getString(R.string.str_id) + UtilsFormat.getStringId(tempJournal.getId(), UtilsFormat.NUM_OF_DIGITS));
		amountEt.setText(tempJournal.getAmount() == 0 ? "" :UtilsFormat.formatCurrency(tempJournal.getAmount()));
		dateBtn.setText(UtilsFormat.formatDate(new Date(tempJournal.getDate()), UtilsFormat.DATE_FORMAT));
        partyBtn.setText(party == null ? getString(R.string.str_select_party) : party.getName());
        noteEt.setText(tempJournal.getNote());
        setTextDrCr(tempJournal.getType());
		mParty = party;


        amountEt.requestFocus();
	}

    /**
     * Sets the value and color of {@link #drCrTv} and {@link #amountEt} based on passed
     * Journal Type
     * @param journalType
     */
    public void setTextDrCr(Journal.Type journalType){
        if (journalType.equals(Journal.Type.Debit)) {
			amountEt.setTextColor(Color.parseColor(Constants.GREEN));
			drCrTv.setTextColor(Color.parseColor(Constants.GREEN));
			drCrTv.setText(getString(R.string.str_dr));
        } else {
			amountEt.setTextColor(Color.parseColor(Constants.RED));
			drCrTv.setTextColor(Color.parseColor(Constants.RED));
			drCrTv.setText(getString(R.string.str_cr));
        }
    }
	
	@Override
	public void onPause() {
		//update pass code time
		LockScreenActivity.updatePasscodeTime();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		//update the id and reset all
        if (journalId == Constants.ID_NEW_JOURNAL) {
			setValues(tempJournal, mParty);
        }
		//check pin code lock
		LockScreenActivity.checkPassCode(getActivity());
	}


	@Override
	public void onDialogBtnClicked(Intent data, int whichBtn, int result, int requestCode) {

		switch (requestCode) {

			case REQUEST_CHGD_PARTY: //A Party is selected
				long partyId = data.getLongExtra(Constants.KEY_PARTY_ID, 0);
				mParty = mServices.getParty(partyId);
				partyBtn.setText(mParty.getName());
				tempJournal.setPartyId(partyId);
				partylistdialog.dismiss();
				break;

			case REQUEST_CHGED_DATE: //A Date is selected
				long newDate = ((Calendar) data.getSerializableExtra(DatePickerFragment.EXTRA_CAL)).getTimeInMillis();
				if(newDate != tempJournal.getDate()){
					//Set journalChanged to true is the date is changed. Journal needs to be reordered
					dateChanged = journalChanged = true;
					tempJournal.setDate(newDate);

				}

				dateBtn.setText(UtilsFormat.formatDate(new Date(tempJournal.getDate()), UtilsFormat.DATE_FORMAT));
				Log.d("Selected Date", String.valueOf(tempJournal.getDate()));
				break;

		}

	}

	private AlertDialog getAttachmetnAlertDialogOption(){
		CharSequence[] options = getResources().getStringArray(R.array.options_attch);
		return new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.str_choose))
				.setItems(options, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent i = null;
						switch (which) {
							case 0: //Take Picture using installed camera app
								Intent takePictureIntent = UtilsFile.getPictureFromCam(getActivity(), tempJournal);
								startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
								break;

							case 1: //Select image from the mServices
								i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
								startActivityForResult(i, REQUEST_IMAGE);
								break;

							case 2: //See the attachments
								if (journalId == Constants.ID_NEW_JOURNAL) {
									//If it is a new journal, warn user
									UtilsView.alert(getActivity(), getString(R.string.warning_see_attachment));
									return;
								}

								if (mServices.getAttachments(tempJournal.getId()).size() == 0) {
									//No attachments => Notify user
									UtilsView.alert(getActivity(), getString(R.string.warning_no_attch));
									return;
								}

								//open ViewPagerActivity to view all attachments
								i = new Intent(getActivity(), ViewPagerActivity.class);
								i.putExtra(Constants.KEY_ATTACHMENTS, mServices.getAttachmentPaths(tempJournal.getId()));
								startActivityForResult(i, REQUEST_CHGD_ATTACHMENTS);
								break;
						}
					}
				}).create();
	}
}
