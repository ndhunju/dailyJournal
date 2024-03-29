package com.ndhunju.dailyjournal.controller.journal;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ndhunju.dailyjournal.OnDatePickerDialogBtnClickedListener;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.fragment.DatePickerFragment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.AdManager;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;
import com.ndhunju.dailyjournal.viewPager.AttachmentViewPagerActivity;

import java.util.Calendar;
import java.util.Date;

public class JournalFragment extends JournalFragmentBase implements OnDatePickerDialogBtnClickedListener {

    //Constants
	public static final String TAG = JournalFragment.class.getSimpleName();
    private static final int REQUEST_CHGED_DATE = 6656;

	private EditText amountEt;
	private EditText noteEt;
    private Button partyBtn;
	private Button dateBtn;
	private TextView drTv;
    private TextView crTv;
	private TextView idTV;
	Button drBtn, crBtn;

	//Declaring variables
	private boolean journalChanged;
    private Services mServices;
    private Journal mJournal;
    private Party mParty;

	/**
	 * Returns new instance of a JournalFragment Class based on passed arguments. Implements
	 * Factory Pattern.
	 */
	public static Fragment newInstance(long journalId, int pos) {
		Bundle args = new Bundle();
		args.putLong(Constants.KEY_JOURNAL_ID, journalId);
		args.putInt(Constants.KEY_POS, pos);
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
		int position = getArguments().getInt(Constants.KEY_POS);

        mJournal = mServices.getJournal(journalId);
        mParty = mServices.getParty(mJournal.getPartyId());
        mJournal = mServices.getJournal(journalId);
		mJournal.setTag(position);

		setHasOptionsMenu(true);
		getActivity().setTitle(getString(R.string.str_journal));
		ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

		if (actionBar != null ) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//Wire Views and Widgets
		View v = inflater.inflate(R.layout.fragment_journal, new LinearLayout(getActivity()));

		idTV = (TextView) v.findViewById(R.id.fragment_journal_id);
		drTv = (TextView) v.findViewById(R.id.fragment_journal_dr_tv);
        crTv = (TextView) v.findViewById(R.id.fragment_journal_cr_tv);

		amountEt = (EditText) v.findViewById(R.id.fragment_home_amount_et);
		amountEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals("")) {
                    try {
                        mJournal.setAmount(UtilsFormat.parseCurrency(s.toString(), getActivity()));
                        journalChanged = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

		dateBtn = (Button) v.findViewById(R.id.activity_home_date_btn);
		dateBtn.setOnClickListener(v13 -> {
			if (getActivity() == null) {
				return;
			}

			DatePickerFragment dpf = DatePickerFragment.newInstance(
					new Date(mJournal.getDate()),
					REQUEST_CHGED_DATE
			);

			dpf.setTargetFragment(JournalFragment.this, REQUEST_CHGED_DATE);
			dpf.show(getActivity().getSupportFragmentManager(), DatePickerFragment.TAG);
			//the result is delivered to OnDialoguePressedOk()
		});

		partyBtn = (Button) v.findViewById(R.id.fragment_home_party_btn);
		partyBtn.setOnClickListener(v14 -> UtilsView.alert(
				getActivity(),
				getString(R.string.warning_cant_change_party)
		));

		Button party2Btn = v.findViewById(R.id.fragment_home_party_2_btn);
		party2Btn.setVisibility(View.GONE);

		drBtn = (Button) v.findViewById(R.id.fragment_home_debit_btn);
		drBtn.setOnClickListener(v15 -> {
			journalChanged = true;
			mJournal.setType(Journal.Type.Debit);
			UtilsView.performTransition(R.id.fragment_home_dr_cr_ll, getActivity());
			setTextDrCr(mJournal.getType());
		});

		crBtn = (Button) v.findViewById(R.id.fragment_home_credit_btn);
		crBtn.setOnClickListener(v16 -> {
			journalChanged = true;
			mJournal.setType(Journal.Type.Credit);
			UtilsView.performTransition(R.id.fragment_home_dr_cr_ll, getActivity());
			setTextDrCr(mJournal.getType());
		});

		noteEt = (EditText) v.findViewById(R.id.fragment_home_note_et);
		noteEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mJournal.setNote(s.toString());
                journalChanged = true;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

		Button attachBtn = (Button) v.findViewById(R.id.fragment_home_attach_btn);
		attachBtn.setOnClickListener(v12 -> {
			// Open AttachmentViewPagerActivity to view attachments
			Intent i = new Intent(getActivity(), AttachmentViewPagerActivity.class);
			i.putExtra(Constants.KEY_JOURNAL_ID, mJournal.getId());
			getAttachmentViewPagerActivityOnResult().launch(i);
		});

		setTextForAttachmentButton(attachBtn);

		Button saveJournalBtn = (Button) v.findViewById(R.id.fragment_home_save_btn);
		saveJournalBtn.setOnClickListener(v1 -> {

			// Check if the selected date is allowed
			if (!mServices.isAllowedDateForJournal(mJournal.getDate())) {
				UtilsView.showAlertDialogForInvalidJournalDate(getContext(), mJournal.getDate());
				return;
			} else if (mServices.shouldShowAlertForPassingFinancialYearDate(
					mJournal.getDate()
			)) {
				UtilsView.showAlertDialogForFutureJournalDate(
						getContext(),
						mJournal.getDate()
				);
				return;
			}

			// Check if user has used comma in notes
			if (showAlertIfNoteHasComma(noteEt)) {
				// User has used comma in notes
				return;
			}

			try {
				UtilsFormat.parseCurrency(amountEt.getText().toString(), getActivity());
				if (journalChanged) {
					mServices.updateJournal(mJournal);
					mJournal = null;
				}

				// Finish the Activity once user is done changing
				if (getActivity() != null) {
					getActivity().finish();
				}

				UtilsView.toast(
						getActivity(),
						String.format(getString(R.string.msg_saved),
						getString(R.string.str_journal))
				);
			} catch (NumberFormatException nfe) {
				UtilsView.alert(
						getActivity(),
						getString(R.string.msg_is_not_valid, getString(R.string.str_amount))
				);
			}

		});

		v.findViewById(R.id.fragment_home_mic_btn).setVisibility(View.INVISIBLE);

        // Refresh values in UI
		setValues(mJournal, mParty);

		return v;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Show ads only after the view has fully loaded to prevent slowing down of views
		AdManager.INSTANCE.loadAdIfAllowed(
				view.findViewById(R.id.fragment_journal_ads_layout),
				getString(R.string.admob_journal_ad_unit_id),
				"JournalScreen"
		);
	}

	@NonNull
	@Override
	public Journal provideJournal() {
		return mJournal;
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
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
				UtilsView.alert(getActivity(), msg, (dialogInterface, i) -> {
					journalChanged = true;
					mServices.deleteJournal(mJournal);
					String msg1 = String.format(getString(R.string.msg_deleted), getString(R.string.str_journal));
					UtilsView.toast(getActivity(), msg1);
					if (getActivity() != null) {
						getActivity().finish();
					}
				}, null);
				break;
			case android.R.id.home:
				if (getActivity() != null) {
					getActivity().onBackPressed();
				}
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		AnalyticsService.INSTANCE.logScreenViewEvent("Journal");
	}

	/**
	 * Sets value of the UI Widgets based on passed parameters
	 */
	private void setValues(Journal tempJournal, Party party) {
		setTextDrCr(tempJournal.getType());
		noteEt.setText(tempJournal.getNote());
		drBtn.setText(UtilsFormat.getDrFromPref(getActivity()));
		crBtn.setText(UtilsFormat.getCrFromPref(getActivity()));
		dateBtn.setText(UtilsFormat.formatDate(new Date(tempJournal.getDate()), getActivity()));
		amountEt.setText(tempJournal.getAmount() == 0 ? "" : UtilsFormat.formatCurrency(tempJournal.getAmount(), getActivity()));
		idTV.setText(getString(R.string.str_id) + UtilsFormat.getStringId(tempJournal.getId(), UtilsFormat.NUM_OF_DIGITS));
		partyBtn.setText(mParty.getName());

		mParty = party;
	}

    /**
     * Sets the value and color of {@link #drTv} and {@link #amountEt} based on passed
     * Journal Type
     */
	private void setTextDrCr(Journal.Type journalType){

		if (getActivity() == null) {
			return;
		}

		int red   = ContextCompat.getColor(getActivity(), R.color.red_light_pressed);
		int green = ContextCompat.getColor(getActivity(), R.color.green);

        if (journalType.equals(Journal.Type.Debit)) {
			amountEt.setTextColor(green);
            //drTv.setTextColor(green);
            //drTv.setText(getString(R.string.str_dr));
            drTv.setVisibility(View.VISIBLE);
            crTv.setVisibility(View.INVISIBLE);
        } else {
            amountEt.setTextColor(red);
            //drTv.setTextColor(red);
            //drTv.setText(getString(R.string.str_cr));
            drTv.setVisibility(View.INVISIBLE);
            crTv.setVisibility(View.VISIBLE);
        }
    }

	@Override
	public void onDialogBtnClicked(Intent data, int whichBtn, int result, int requestCode) {

		switch (requestCode) {

			case REQUEST_CHGED_DATE:
				// A Date is selected
				long newDate = ((Calendar) data.getSerializableExtra(
						DatePickerFragment.EXTRA_CAL
				)).getTimeInMillis();
				if(newDate != mJournal.getDate()){
					// Set journalChanged to true is the date is changed.
					// Journal needs to be reordered
					journalChanged = true;
					mJournal.setDate(newDate);
				}
				dateBtn.setText(UtilsFormat.formatDate(
						new Date(mJournal.getDate()),
						getActivity()
				));
				Log.d("Selected Date", String.valueOf(mJournal.getDate()));
				break;

		}

	}

}
