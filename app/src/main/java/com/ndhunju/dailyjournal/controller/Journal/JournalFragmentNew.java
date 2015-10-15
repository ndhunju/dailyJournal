package com.ndhunju.dailyjournal.controller.journal;

import android.app.Fragment;
import android.content.DialogInterface;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.fragment.DatePickerFragment;
import com.ndhunju.dailyjournal.controller.preference.MyPreferenceActivity;
import com.ndhunju.dailyjournal.controller.folderPicker.OnDialogBtnClickedListener;
import com.ndhunju.dailyjournal.controller.party.PartyListActivity;
import com.ndhunju.dailyjournal.controller.party.PartyListDialog;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.KeyValPersistence;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;
import com.ndhunju.dailyjournal.viewPager.ViewPagerActivity;

import java.util.Calendar;
import java.util.Date;

public class JournalFragmentNew extends Fragment implements OnDialogBtnClickedListener {

    //Constants
    private static final String TAG = JournalFragmentNew.class.getSimpleName();
    private static final int REQUEST_CODE_IMPORT_OLD_DATA = 183;
    private static final int REQUEST_CODE_IMPORT_DATA = 254;
    private static final int REQUEST_CODE_GENERAL = 564;
    private static final int REQUEST_CHGED_DATE = 656;
    private static final int REQUEST_CHGD_PARTY = 456;


    //Declaring UI Widgets variable
    private PartyListDialog partylistdialog;
    private EditText amountEt;
    private TextView drCrTv;
    private EditText noteEt;
    private Button partyBtn;
    private Button dateBtn;
    private TextView idTV;

    //Declaring variables
    private Journal tempJournal;
    private Services mServices;
    private Party mParty;

    /**
     * Returns new instance of a JournalFragment Class based on passed arguments. Implements
     * Factory Pattern.
     *
     * @return
     */
    public static Fragment newInstance() {
        Bundle args = new Bundle();
        JournalFragmentNew newJF = new JournalFragmentNew();
        newJF.setArguments(args);
        return newJF;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mServices = Services.getInstance(getActivity());

        //Check if the old directory created by older version of app (v3.1-) exists or not
        if (!KeyValPersistence.isOldDataImported(getActivity()) && UtilsFile.oldAppFolderExist()) {
            //Old directory exists
            UtilsView.alert(getActivity(), getString(R.string.msg_move_old_data), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Switch to MyPreferenceActivity to import old data to new directory
                    Intent importOldDataIntent = new Intent(getActivity(), MyPreferenceActivity.class);
                    importOldDataIntent.putExtra(Constants.KEY_IMPORT_OLD_DATA, true);
                    startActivityForResult(importOldDataIntent, REQUEST_CODE_IMPORT_OLD_DATA);
                }
            });

        }

        tempJournal = mServices.getNewJournal();
        setHasOptionsMenu(true);    //enable menu

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //Wire Views and Widgets
        View v = inflater.inflate(R.layout.fragment_journal, new LinearLayout(getActivity()));

        idTV = (TextView) v.findViewById(R.id.fragment_journal_id);
        idTV.setText(UtilsFormat.getStringId(tempJournal.getId(), UtilsFormat.NUM_OF_DIGITS));

        drCrTv = (TextView) v.findViewById(R.id.fragment_journal_dr_cr_tv);

        amountEt = (EditText) v.findViewById(R.id.fragment_home_amount_et);
        amountEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals("")) {
                    try {
                        tempJournal.setAmount(UtilsFormat.parseCurrency(s.toString(), getActivity()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        UtilsView.alert(getActivity(), getString(R.string.warning_format));
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
        dateBtn.setText(UtilsFormat.formatDate(new Date(tempJournal.getDate()), getActivity()));
        dateBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment dpf = DatePickerFragment.newInstance(new Date(tempJournal.getDate()), REQUEST_CHGED_DATE);
                dpf.setTargetFragment(JournalFragmentNew.this, REQUEST_CHGED_DATE);
                dpf.show(getActivity().getFragmentManager(), DatePickerFragment.TAG);
                //the result is delivered to OnDialoguePressedOk()
            }
        });

        partyBtn = (Button) v.findViewById(R.id.fragment_home_merchant_btn);
        partyBtn.setText(mParty == null ? getString(R.string.str_select_party) : mParty.getName());
        partyBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                partylistdialog = PartyListDialog.newInstance(REQUEST_CHGD_PARTY);
                partylistdialog.setTargetFragment(JournalFragmentNew.this, REQUEST_CHGD_PARTY);
                partylistdialog.show(getActivity().getFragmentManager(), Constants.KEY_PARTY_ID);
                //the result is delivered to OnDialoguePressedOk()
            }
        });

        Button drBtn = (Button) v.findViewById(R.id.fragment_home_debit_btn);
        drBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                tempJournal.setType(Journal.Type.Debit);
                setTextDrCr(tempJournal.getType());
            }
        });

        Button crBtn = (Button) v.findViewById(R.id.fragment_home_credit_btn);
        crBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                tempJournal.setType(Journal.Type.Credit);
                setTextDrCr(tempJournal.getType());
            }
        });

        noteEt = (EditText) v.findViewById(R.id.fragment_home_note_et);
        noteEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tempJournal.setNote(s.toString());
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
                i.putExtra(Constants.KEY_JOURNAL_ID, tempJournal.getId());
                startActivity(i);
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

                mServices.updateNewJournal(tempJournal);
                //Save selected values so that user doesn't have to selected them again
                long selectedDate = tempJournal.getDate();
                long selectedPartyId = tempJournal.getPartyId();
                Journal.Type selectedType = tempJournal.getType();

                //Create new instance of Journal
                tempJournal = mServices.getNewJournal();
                tempJournal.setDate(selectedDate);
                tempJournal.setPartyId(selectedPartyId);
                tempJournal.setType(selectedType);
                setValues(tempJournal, mParty);

                UtilsView.toast(getActivity(), String.format(getString(R.string.msg_saved),
                        getString(R.string.str_journal)));
            }
        });

        //Refresh values in UI
        setValues(tempJournal, mParty);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getString(R.string.title_activity_journal));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case REQUEST_CODE_IMPORT_OLD_DATA: //Old Data were imported to new directory
                UtilsView.showResult(getActivity(), resultCode);
                Log.i(TAG, "Importing old data finished");
                break;

            case REQUEST_CODE_GENERAL:
            case REQUEST_CODE_IMPORT_DATA:
                mParty = null;
                tempJournal = mServices.getNewJournal();
                setValues(tempJournal, mParty);

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
            case R.id.menu_party_list:
                startActivityForResult(new Intent(getActivity(), PartyListActivity.class)
                , REQUEST_CODE_GENERAL);
                break;

            case R.id.menu_journal_preference:
                Intent preferencesIntent = new Intent(getActivity(), MyPreferenceActivity.class);
                startActivityForResult(preferencesIntent, REQUEST_CODE_GENERAL);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets value of the UI Widgets based on passed parameters
     */
    private void setValues(Journal tempJournal, Party party) {
        idTV.setText(getString(R.string.str_id) + UtilsFormat.getStringId(tempJournal.getId(), UtilsFormat.NUM_OF_DIGITS));
        amountEt.setText(tempJournal.getAmount() == 0 ? "" : UtilsFormat.formatCurrency(tempJournal.getAmount(), getActivity()));
        dateBtn.setText(UtilsFormat.formatDate(new Date(tempJournal.getDate()), getActivity()));
        partyBtn.setText(party == null ? getString(R.string.str_select_party) : party.getName());
        noteEt.setText(tempJournal.getNote());
        setTextDrCr(tempJournal.getType());
        mParty = party;


        amountEt.requestFocus();
    }

    /**
     * Sets the value and color of {@link #drCrTv} and {@link #amountEt} based on passed
     * Journal Type
     *
     * @param journalType
     */
    private void setTextDrCr(Journal.Type journalType) {
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

            case REQUEST_CHGD_PARTY: //A Party is selected
                long partyId = data.getLongExtra(Constants.KEY_PARTY_ID, 0);
                mParty = mServices.getParty(partyId);
                partyBtn.setText(mParty.getName());
                tempJournal.setPartyId(partyId);
                partylistdialog.dismiss();
                break;

            case REQUEST_CHGED_DATE: //A Date is selected
                long newDate = ((Calendar) data.getSerializableExtra(DatePickerFragment.EXTRA_CAL)).getTimeInMillis();
                if (newDate != tempJournal.getDate()) {
                    //Set journalChanged to true is the date is changed. Journal needs to be reordered
                    tempJournal.setDate(newDate);

                }
                dateBtn.setText(UtilsFormat.formatDate(new Date(tempJournal.getDate()), getActivity()));
                break;

        }

    }

}
