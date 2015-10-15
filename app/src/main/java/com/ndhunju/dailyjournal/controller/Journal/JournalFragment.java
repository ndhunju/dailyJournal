package com.ndhunju.dailyjournal.controller.journal;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.ndhunju.dailyjournal.controller.fragment.DatePickerFragment;
import com.ndhunju.dailyjournal.controller.folderPicker.OnDialogBtnClickedListener;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;
import com.ndhunju.dailyjournal.viewPager.ViewPagerActivity;

import java.util.Calendar;
import java.util.Date;

public class JournalFragment extends Fragment implements OnDialogBtnClickedListener {

    //Constants
	private static final String TAG = JournalFragment.class.getSimpleName();
    private static final int REQUEST_CHGED_DATE = 6656;

    private EditText amountEt;
	private EditText noteEt;
    private Button partyBtn;
	private Button dateBtn;
	private TextView drCrTv;
	private TextView idTV;

	//Declaring variables
	private boolean journalChanged;
    private Services mServices;
    private Journal mJournal;
    private Party mParty;

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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		mServices = Services.getInstance(getActivity());
		long journalId = getArguments().getLong(Constants.KEY_JOURNAL_ID);
        long partyId = getArguments().getLong(Constants.KEY_PARTY_ID);

        mJournal = mServices.getJournal(journalId);
        mParty = mServices.getParty(partyId);
        mJournal = mServices.getJournal(journalId);
        getActivity().setTitle(mParty.getName());
        setHasOptionsMenu(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//Wire Views and Widgets
		View v = inflater.inflate(R.layout.fragment_journal, new LinearLayout(getActivity()));

		idTV = (TextView) v.findViewById(R.id.fragment_journal_id);
		idTV.setText(UtilsFormat.getStringId(mJournal.getId(), UtilsFormat.NUM_OF_DIGITS));

		drCrTv = (TextView) v.findViewById(R.id.fragment_journal_dr_cr_tv);

		amountEt = (EditText) v.findViewById(R.id.fragment_home_amount_et);
		amountEt.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if( !s.toString().equals("")){
					try{
						mJournal.setAmount( UtilsFormat.parseCurrency(s.toString(), getActivity()));
						journalChanged = true;
					}catch(Exception e){
						e.printStackTrace();
					}
				  }
				}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});

		dateBtn = (Button) v.findViewById(R.id.activity_home_date_btn);
		dateBtn.setText(UtilsFormat.formatDate(new Date(mJournal.getDate()), getActivity())) ;
		dateBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerFragment dpf = DatePickerFragment.newInstance(new Date(mJournal.getDate()), REQUEST_CHGED_DATE);
				dpf.setTargetFragment(JournalFragment.this, REQUEST_CHGED_DATE);
				dpf.show(getActivity().getFragmentManager(), DatePickerFragment.TAG);
				//the result is delivered to OnDialoguePressedOk()
			}
		});

		partyBtn = (Button) v.findViewById(R.id.fragment_home_merchant_btn);
		partyBtn.setText(mParty == null ? getString(R.string.str_select_party) : mParty.getName());
		partyBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                UtilsView.alert(getActivity(), getString(R.string.warning_cant_change_party));
			}
		});

		Button drBtn = (Button) v.findViewById(R.id.fragment_home_debit_btn);
		drBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				journalChanged = true;
				mJournal.setType(Journal.Type.Debit);
                setTextDrCr(mJournal.getType());
			}
		});

		Button crBtn = (Button) v.findViewById(R.id.fragment_home_credit_btn);
		crBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				journalChanged = true;
				mJournal.setType(Journal.Type.Credit);
                setTextDrCr(mJournal.getType());
			}
		});

		noteEt = (EditText) v.findViewById(R.id.fragment_home_note_et);
		noteEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mJournal.setNote(s.toString());
                journalChanged = true;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

		Button attachBtn = (Button) v.findViewById(R.id.fragment_home_attach_btn);
		attachBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                //open ViewPagerActivity to view attachments
                Intent i = new Intent(getActivity(), ViewPagerActivity.class);
                i.putExtra(Constants.KEY_JOURNAL_ID, mJournal.getId());
                startActivity(i);
			}
		});

		Button saveJournalBtn = (Button) v.findViewById(R.id.fragment_home_save_btn);
		saveJournalBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
					UtilsFormat.parseCurrency(amountEt.getText().toString(), getActivity());
					Intent i = new Intent();
					if (journalChanged) {
						i.putExtra(Constants.KEY_JOURNAL_CHGD, true);
						mServices.updateJournal(mJournal);
						mJournal = null;
					}
					getActivity().setResult(Activity.RESULT_OK, i);
					getActivity().finish(); //Finish the Activity once user is done changing

					UtilsView.toast(getActivity(), String.format(getString(R.string.msg_saved),
							getString(R.string.str_journal)));
				}catch (NumberFormatException nfe){
					UtilsView.alert(getActivity(), getString(R.string.msg_is_not_valid
					,getString(R.string.str_amount)));
				}

            }
        });

        //Refresh values in UI
		setValues(mJournal, mParty);

		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment_journal, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.menu_fragment_journal_delete_picture:
				//Prepare warning msg
				String msg = String.format(getString(R.string.msg_delete_confirm), getString(R.string.str_journal));
				//Alert user before deleting the Journal
				UtilsView.alert(getActivity(), msg, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						journalChanged = true;
						mServices.deleteJournal(mJournal);
						String msg = String.format(getString(R.string.msg_deleted), getString(R.string.str_journal));
						UtilsView.toast(getActivity(), msg);
						Intent intent = new Intent();
						intent.putExtra(Constants.KEY_JOURNAL_CHGD, true);
						getActivity().setResult(Activity.RESULT_OK, intent);
						getActivity().finish();
					}
				}, null);
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Sets value of the UI Widgets based on passed parameters
	 */
	private void setValues(Journal tempJournal, Party party) {
		idTV.setText(getString(R.string.str_id) + UtilsFormat.getStringId(tempJournal.getId(), UtilsFormat.NUM_OF_DIGITS));
		amountEt.setText(tempJournal.getAmount() == 0 ? "" :UtilsFormat.formatCurrency(tempJournal.getAmount(),getActivity() ));
		dateBtn.setText(UtilsFormat.formatDate(new Date(tempJournal.getDate()), getActivity()));
        partyBtn.setText(party == null ? getString(R.string.str_select_party) : party.getName());
        noteEt.setText(tempJournal.getNote());
        setTextDrCr(tempJournal.getType());
		mParty = party;
	}

    /**
     * Sets the value and color of {@link #drCrTv} and {@link #amountEt} based on passed
     * Journal Type
     * @param journalType
     */
	private void setTextDrCr(Journal.Type journalType){
		int red = getResources().getColor(R.color.red_light_pressed);
		int green = getResources().getColor(R.color.green);

        if (journalType.equals(Journal.Type.Debit)) {
			amountEt.setTextColor(green);
			drCrTv.setTextColor(green);
			drCrTv.setText(getString(R.string.str_dr));
        } else {
			amountEt.setTextColor(red);
			drCrTv.setTextColor(red);
			drCrTv.setText(getString(R.string.str_cr));
        }
    }

	@Override
	public void onDialogBtnClicked(Intent data, int whichBtn, int result, int requestCode) {

		switch (requestCode) {

			case REQUEST_CHGED_DATE: //A Date is selected
				long newDate = ((Calendar) data.getSerializableExtra(DatePickerFragment.EXTRA_CAL)).getTimeInMillis();
				if(newDate != mJournal.getDate()){
					//Set journalChanged to true is the date is changed. Journal needs to be reordered
					journalChanged = true;
					mJournal.setDate(newDate);
				}
				dateBtn.setText(UtilsFormat.formatDate(new Date(mJournal.getDate()), getActivity()));
				Log.d("Selected Date", String.valueOf(mJournal.getDate()));
				break;

		}

	}

}
