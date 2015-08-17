package com.ndhunju.dailyjournal.controller;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.LinearLayout;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Utils;

public class DatePickerFragment extends DialogFragment {
	
	public static final String TAG = DialogFragment.class.getCanonicalName();
	protected static final String EXTRA_CAL = "com.ndhunju.dailyJournal.datePickerFragment.extraCal";
	
	static Calendar mCal;
	
	public static DatePickerFragment newInstance(Date d, int requestCode){
		
		//Create a calendar to get the year, month, and day
		mCal = Calendar.getInstance();
		mCal.setTime(d);
		
		DatePickerFragment dateFragment = new DatePickerFragment();
		Bundle args = new Bundle();
		args.putInt(Utils.KEY_REQUEST_CODE, requestCode);
		dateFragment.setArguments(args);

		return dateFragment;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		int year = mCal.get(Calendar.YEAR);
		int month = mCal.get(Calendar.MONTH);
		int day = mCal.get(Calendar.DAY_OF_MONTH);
		//int hour = mCal.get(Calendar.HOUR_OF_DAY);
		//int min = mCal.get(Calendar.MINUTE);
		
		LinearLayout v = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.dialog_date_picker, new LinearLayout(getActivity()));
		DatePicker dp = (DatePicker)v.findViewById(R.id.dialog_edit_date); 
		dp.init(year, month, day, new OnDateChangedListener() {
			@Override
			public void onDateChanged (DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				//Translate year, month, day into a Date object using calendar	
				mCal.set(Calendar.YEAR, year);
				mCal.set(Calendar.MONTH, monthOfYear);
				mCal.set(Calendar.DAY_OF_MONTH	, dayOfMonth);
				
			}
		}
		);
		
		
		/* Disabling TimePicker as Time is not so much relevant for a Journal
		TimePicker tp = (TimePicker)v.findViewById(R.id.dialogTimePicker);
		tp.setCurrentHour(hour);
		tp.setCurrentMinute(min);
		tp.setOnTimeChangedListener(new OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				mCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
				mCal.set(Calendar.MINUTE	, minute);
				//update arguments to preserve selecrted value on rotation
				getArguments().putSerializable(EXTRA_CAL, mCal);	
			}
		});*/
		
		return new AlertDialog.Builder(getActivity())
			.setTitle(getString(R.string.str_pick_a_date))
			.setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent();
					i.putExtra(EXTRA_CAL, mCal);
					//So far, DatePickerFragment is called from JournalFragment only
					((JournalFragment)getTargetFragment())
							.onDialogPositiveBtnClicked(i, Activity.RESULT_OK, getArguments().getInt(Utils.KEY_REQUEST_CODE));
				}
			})
			.setView(v)
			.create();
	}

}
