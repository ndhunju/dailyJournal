package com.ndhunju.dailyjournal.controller.journal;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ndhunju.dailyjournal.OnDatePickerDialogBtnClickedListener;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.fragment.DatePickerFragment;
import com.ndhunju.dailyjournal.controller.party.PartyListDialog;
import com.ndhunju.dailyjournal.controller.preference.MyPreferenceActivity;
import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.AdManager;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.KeyValPersistence;
import com.ndhunju.dailyjournal.service.ParseText;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;
import com.ndhunju.dailyjournal.viewPager.AttachmentViewPagerActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.ndhunju.dailyjournal.model.Journal.Type.Credit;
import static com.ndhunju.dailyjournal.model.Journal.Type.Debit;

public class JournalFragmentNew extends JournalFragmentBase implements OnDatePickerDialogBtnClickedListener {

    //Constants
    public static final String TAG = JournalFragmentNew.class.getSimpleName();
    private static final int REQUEST_CODE_IMPORT_OLD_DATA = 183;
    private static final int REQUEST_CODE_IMPORT_DATA = 254;
    private static final int REQUEST_CODE_GENERAL = 564;
    private static final int REQUEST_CHGED_DATE = 656;
    private static final int REQUEST_CHGD_PARTY = 456;
    private static final int REQUEST_PARTY_2 = 457;
    private static final int REQUEST_CODE_SPEECH = 898;


    //Declaring UI Widgets variable
    private EditText amountEt;
    private TextView drTv;
    private TextView crTv;
    private Button attachBtn;
    private EditText noteEt;
    private Button partyBtn;
    private Button party2Btn;
    private Button dateBtn;
    private TextView idTV;
    Button drBtn, crBtn;

    //Declaring variables
    private Journal tempJournal;
    private Services mServices;
    private Party mParty;
    private Party mParty2;

    /**
     * Returns new instance of a JournalFragment Class based on passed arguments. Implements
     * Factory Pattern.
     *
     * @return
     */
    public static Fragment newInstance(String partyId) {
        Bundle args = new Bundle();
        if (partyId != null) {
            args.putString(Constants.KEY_PARTY_ID, partyId);
        }
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

        Bundle args = getArguments();
        if (args.containsKey(Constants.KEY_PARTY_ID)) {
            mParty = mServices.getParty(Long.parseLong(getArguments().getString(Constants.KEY_PARTY_ID)));
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

        drTv = (TextView) v.findViewById(R.id.fragment_journal_dr_tv);
        crTv = (TextView) v.findViewById(R.id.fragment_journal_cr_tv);

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
        dateBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment dpf = DatePickerFragment.newInstance(new Date(tempJournal.getDate()), REQUEST_CHGED_DATE);
                dpf.setTargetFragment(JournalFragmentNew.this, REQUEST_CHGED_DATE);
                dpf.show(getActivity().getSupportFragmentManager(), DatePickerFragment.TAG);
                //the result is delivered to onDialogBtnClicked()
            }
        });

        partyBtn = (Button) v.findViewById(R.id.fragment_home_party_btn);
        partyBtn.setOnClickListener(v1 -> {
            PartyListDialog partylistdialog = PartyListDialog.newInstance(REQUEST_CHGD_PARTY);
            partylistdialog.setTargetFragment(JournalFragmentNew.this, REQUEST_CHGD_PARTY);
            partylistdialog.show(getActivity().getSupportFragmentManager(), PartyListDialog.TAG);
            //the result is delivered to onDialogBtnClicked()
        });

        party2Btn = v.findViewById(R.id.fragment_home_party_2_btn);
        party2Btn.setOnClickListener(v1 -> {

            if (getActivity() == null) {
                return;
            }

            PartyListDialog partylistdialog = PartyListDialog.newInstance(REQUEST_PARTY_2);
            partylistdialog.setTargetFragment(JournalFragmentNew.this, REQUEST_PARTY_2);
            partylistdialog.show(getActivity().getSupportFragmentManager(), PartyListDialog.TAG);
            //the result is delivered to onDialogBtnClicked()
        });

        if (mParty != null) {
            partyBtn.setText(mParty.getName());
            tempJournal.setPartyId(mParty.getId());
        }

        drBtn = (Button) v.findViewById(R.id.fragment_home_debit_btn);
        drBtn.setOnClickListener(v12 -> {
            tempJournal.setType(Debit);
            UtilsView.performTransition(R.id.fragment_home_dr_cr_ll, getActivity());
            setTextDrCr(tempJournal.getType());
        });

        crBtn = (Button) v.findViewById(R.id.fragment_home_credit_btn);
        crBtn.setOnClickListener(v13 -> {
            tempJournal.setType(Credit);
            UtilsView.performTransition(R.id.fragment_home_dr_cr_ll, getActivity());
            setTextDrCr(tempJournal.getType());
        });

        noteEt = (EditText) v.findViewById(R.id.fragment_home_note_et);
        noteEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tempJournal.setNote(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        attachBtn = v.findViewById(R.id.fragment_home_attach_btn);
        attachBtn.setOnClickListener(v14 -> {
            // Open AttachmentViewPagerActivity to view attachments
            Intent i = new Intent(getActivity(), AttachmentViewPagerActivity.class);
            i.putExtra(Constants.KEY_JOURNAL_ID, tempJournal.getId());
            getAttachmentViewPagerActivityOnResult().launch(i);
        });

        setTextForAttachmentButton(attachBtn);

        Button saveJournalBtn = (Button) v.findViewById(R.id.fragment_home_save_btn);
        saveJournalBtn.setOnClickListener(v15 -> {

            // If mParty is not selected warn user
            if (mParty == null) {
                UtilsView.alert(getActivity(), getString(R.string.warning_select_party));
                return;
            }

            // Party might have been deleted. one way is from Party dialog -> context menu -> delete
            if (mServices.getParty(mParty.getId()) == null) {
                UtilsView.alert(getActivity(), getString(R.string.warning_party_not_found));
                return;
            }

            // Check if the selected date is allowed
            if (!mServices.isAllowedDateForJournal(tempJournal.getDate())) {
                UtilsView.showAlertDialogForInvalidJournalDate(
                        getContext(),
                        tempJournal.getDate()
                );
                return;
            } else if (mServices.shouldShowAlertForPassingFinancialYearDate(
                    tempJournal.getDate()
            )) {
                UtilsView.showAlertDialogForFutureJournalDate(
                        getContext(),
                        tempJournal.getDate()
                );
                return;
            }

            // Check if user has used comma in notes
            if (showAlertIfNoteHasComma(noteEt)) {
                // User has used comma in notes return
                return;
            }

            mServices.updateNewJournal(tempJournal);

            // Create and add another journal for Party 2
            if (mParty2 != null) {
                Journal journal2 = mServices.getNewJournal();
                journal2.setPartyId(mParty2.getId());
                journal2.setType(tempJournal.getType() == Credit ? Debit : Credit);

                // Copy shared data from tempJournal
                journal2.setDate(tempJournal.getDate());
                journal2.setCreatedDate(tempJournal.getCreatedDate());
                journal2.setAmount(tempJournal.getAmount());
                journal2.setNote(tempJournal.getNote());

                // Update the database
                mServices.updateNewJournal(journal2);

                // Copy attachments too
                List<Attachment> attachments = mServices.getAttachments(tempJournal.getId());
                Attachment copy;
                for (Attachment attachment: attachments) {
                    copy = new Attachment(journal2.getId());
                    copy.setPath(attachment.getPath());
                    copy.setLinkedAttachmentId(attachment.getId());
                    //copy.setId(); // Id is basically row number
                    long idFromDb = mServices.addAttachment(copy);
                    attachment.setLinkedAttachmentId(idFromDb);
                    mServices.updateAttachment(attachment);
                }
            }

            // Save selected values so that user doesn't have to select them again
            long selectedDate = tempJournal.getDate();
            long selectedPartyId = tempJournal.getPartyId();
            Journal.Type selectedType = tempJournal.getType();

            // Create new instance of Journal
            tempJournal = mServices.getNewJournal();
            tempJournal.setDate(selectedDate);
            tempJournal.setPartyId(selectedPartyId);
            tempJournal.setType(selectedType);
            // Reset mParty2 as usually its not the same
            mParty2 = null;
            setValues(tempJournal, mParty);

            UtilsView.toast(getActivity(), String.format(
                    getString(R.string.msg_saved),
                    getString(R.string.str_journal))
            );
        });

        /*ImageView settingImageView = (ImageView) v.findViewById(R.id.fragment_home_settings_btn);
        final Drawable drawable = getActivity().getResources().getDrawable(R.drawable.avd_setting_rotate);
        settingImageView.setImageDrawable(drawable);
        settingImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (drawable instanceof Animatable) {
                    ((Animatable) drawable).start();
                }
            }
        });*/

        ((FloatingActionButton)v.findViewById(R.id.fragment_home_mic_btn))
                .setOnClickListener(v16 -> {
                    // Create an intent that can start the Speech Recognizer activity
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    );
                    // Start the activity, the intent will be populated with the speech text
                    startActivityForResult(intent, REQUEST_CODE_SPEECH);
                    AnalyticsService.INSTANCE.logEvent("didClickOnMicInNewJournal");
                });

        // Refresh values in UI
        setValues(tempJournal, mParty);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Show ads only after the view has fully loaded to prevent slowing down of views
        AdManager.INSTANCE.loadAdIfAllowed(
                view.findViewById(R.id.fragment_journal_ads_layout),
                getString(R.string.admob_new_journal_ad_unit_id),
                "NewJournalScreen"
        );
    }

    @NonNull
    @Override
    public Journal provideJournal() {
        return tempJournal;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(UtilsFormat.getJournalFromPref(getActivity()));
        AnalyticsService.INSTANCE.logScreenViewEvent("NewJournal");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode != AppCompatActivity.RESULT_OK)
            return;

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
                break;

            case REQUEST_CODE_SPEECH:
                List<String> results  = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                tempJournal = ParseText.from(getActivity()).extractJournal(results);
                setValues(tempJournal, mServices.getParty(tempJournal.getPartyId()));
                noteEt.setText(tempJournal.getNote() + "\n\r" + results.get(0));
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets value of the UI Widgets based on passed parameters
     */
    private void setValues(Journal tempJournal, Party party) {
        setTextDrCr(tempJournal.getType());
        setTextForAttachmentButton(attachBtn);
        noteEt.setText(tempJournal.getNote());
        drBtn.setText(UtilsFormat.getDrFromPref(getActivity()));
        crBtn.setText(UtilsFormat.getCrFromPref(getActivity()));
        dateBtn.setText(UtilsFormat.formatDate(new Date(tempJournal.getDate()), getActivity()));
        amountEt.setText(tempJournal.getAmount() == 0 ? "" : UtilsFormat.formatCurrency(tempJournal.getAmount(), getActivity()));
        idTV.setText(getString(R.string.str_id) + UtilsFormat.getStringId(tempJournal.getId(), UtilsFormat.NUM_OF_DIGITS));
        partyBtn.setText(party == null ? getString(R.string.str_select) + " " + UtilsFormat.getPartyFromPref(getActivity())
                : party.getName());

        mParty = party;


        amountEt.requestFocus();
    }

    /**
     * Sets the value and color of {@link #drTv} and {@link #amountEt} based on passed
     * Journal Type
     *
     * @param journalType
     */
    private void setTextDrCr(Journal.Type journalType) {
        int red = getResources().getColor(R.color.red_light_pressed);
        int green = getResources().getColor(R.color.green);


        if (journalType.equals(Debit)) {
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

        // When journalType changes, text in Party2Btn also changes
        setTextForParty2Btn();
    }

    /**
     * Sets text and background of {@link this#party2Btn} based on various factors
     */
    private void setTextForParty2Btn() {

        if (getContext() == null) {
            return;
        }

        // Set the text following below logic
        // If party 2 is selected, show party 2's name
        if (mParty2 != null) {
            party2Btn.setText(mParty2.getName());
        } else if (tempJournal.getType() == Credit) {
            // If tempJournal is Credit, show "Select Debit Party"
            party2Btn.setText(getString(R.string.str_select)
                    + " "
                    + UtilsFormat.getDrFromPref(getActivity())
                    + " "
                    + UtilsFormat.getPartyFromPref(getActivity()));
        } else {
            // If tempJournal is Debit, show "Select Credit Party"
            party2Btn.setText(getString(R.string.str_select)
                    + " "
                    + UtilsFormat.getCrFromPref(getActivity())
                    + " "
                    + UtilsFormat.getPartyFromPref(getActivity()));
        }

        // If the main party is a Debtor, the second will be Creditor
        if (tempJournal.getType() == Debit) {
            party2Btn.setBackground(ContextCompat.getDrawable(
                    getContext(),
                    R.drawable.button_negative
                    ));
        } else {
            party2Btn.setBackground(ContextCompat.getDrawable(
                    getContext(),
                    R.drawable.button_positive
                    ));
        }
    }


    @Override
    public void onDialogBtnClicked(Intent data, int whichBtn, int result, int requestCode) {

        PartyListDialog partylistdialog;

        switch (requestCode) {

            case REQUEST_CHGD_PARTY: //A Party is selected
                if(data == null) return;
                long partyId = data.getLongExtra(Constants.KEY_PARTY_ID, 0);
                mParty = mServices.getParty(partyId);
                partyBtn.setText(mParty.getName());
                tempJournal.setPartyId(partyId);
                partylistdialog = (PartyListDialog) getParentFragmentManager()
                        .findFragmentByTag(PartyListDialog.TAG);
                if (partylistdialog != null) {
                    partylistdialog.dismiss();
                }
                break;

            case REQUEST_PARTY_2: // A second party is selected
                if (data == null) return;
                long drPartyId = data.getLongExtra(Constants.KEY_PARTY_ID, 0);
                mParty2 = mServices.getParty(drPartyId);
                setTextForParty2Btn();
                partylistdialog = (PartyListDialog) getParentFragmentManager()
                        .findFragmentByTag(PartyListDialog.TAG);
                if (partylistdialog != null) {
                    partylistdialog.dismiss();
                }
                break;

            case REQUEST_CHGED_DATE: //A Date is selected
                if(data == null) return;
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
