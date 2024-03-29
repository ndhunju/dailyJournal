package com.ndhunju.dailyjournal.controller.tools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.ndhunju.dailyjournal.OnDatePickerDialogBtnClickedListener;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.BaseActivity;
import com.ndhunju.dailyjournal.controller.fragment.DatePickerFragment;
import com.ndhunju.dailyjournal.controller.party.LedgerAdapter;
import com.ndhunju.dailyjournal.controller.party.LedgerCardAdapter;
import com.ndhunju.dailyjournal.controller.party.PartyListDialog;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.Utils;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by ndhunju on 9/12/17.
 * This activity is responsible for showing list of journals for a selected day for a selected party.
 */

public class DailyReportActivity
        extends BaseActivity
        implements LedgerAdapter.OnItemClickListener, OnDatePickerDialogBtnClickedListener {

    private static final int REQUEST_DATE = 6656;
    private static final int REQUEST_CHGD_PARTY = 456;

    Party mParty;
    Services mServices;
    Calendar mSelectedDate;

    Button dateBtn;
    Button partyBtn;
    Button findJournalBtn;
    RecyclerView recyclerView;
    LedgerAdapter ledgerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_report);

        mSelectedDate = Calendar.getInstance();
        Utils.removeValuesBelowHours(mSelectedDate);
        mServices = Services.getInstance(this);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_daily_report);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dateBtn = (Button) findViewById(R.id.activity_daily_report_date_btn);
        findJournalBtn = (Button) findViewById(R.id.activity_daily_report_find);
        recyclerView = (RecyclerView) findViewById(R.id.activity_daily_report_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        dateBtn.setText(UtilsFormat.formatDate(mSelectedDate.getTime(), getApplicationContext()));
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment dpf = DatePickerFragment.newInstance(mSelectedDate.getTime(), REQUEST_DATE);
                dpf.show(getSupportFragmentManager(), DatePickerFragment.TAG);
                //the result is delivered to OnDialoguePressedOk()
            }
        });

        partyBtn = (Button) findViewById(R.id.activity_daily_report_party_btn);
        partyBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PartyListDialog partylistdialog = PartyListDialog.newInstance(REQUEST_CHGD_PARTY);
                partylistdialog.show(getSupportFragmentManager(), PartyListDialog.TAG);
                //the result is delivered to onDialogBtnClicked()
            }
        });

        findJournalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ledgerAdapter.setJournals(mServices.findByPartyAndDate(mParty == null ? Constants.NO_PARTY : mParty.getId(), mSelectedDate.getTimeInMillis()));
            }
        });

        ledgerAdapter = new LedgerCardAdapter(this, new ArrayList<Journal>());
        ledgerAdapter.setLayoutId(R.layout.spanned_ledger_card);
        ledgerAdapter.setOnItemClickListener(this);
        Services.getInstance(this).registerJournalObserver(ledgerAdapter);
        recyclerView.setAdapter(ledgerAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsService.INSTANCE.logScreenViewEvent("DailyReport");
    }

    @Override
    public void onDialogBtnClicked(
            Intent data,
            @OnDatePickerDialogBtnClickedListener.ButtonType int whichBtn,
            int result,
            int requestCode
    ) {

        if (result != Activity.RESULT_OK) return;

        switch (requestCode) {

            case REQUEST_DATE:
                Calendar calendar = (Calendar) data.getSerializableExtra(DatePickerFragment.EXTRA_CAL);
                Utils.removeValuesBelowHours(calendar);

                mSelectedDate = calendar;
                dateBtn.setText(UtilsFormat.formatDate(mSelectedDate.getTime(), getApplicationContext()));
                break;

            case REQUEST_CHGD_PARTY: //A Party is selected
                long partyId = data.getLongExtra(Constants.KEY_PARTY_ID, 0);
                mParty = mServices.getParty(partyId);
                partyBtn.setText(mParty.getName());
                PartyListDialog partylistdialog = (PartyListDialog) getSupportFragmentManager().findFragmentByTag(PartyListDialog.TAG);
                if (partylistdialog != null) {
                    partylistdialog.dismiss();
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position, List<Journal> journalList) {
        long[] ids = new long[journalList.size()];
        for (int i = 0; i < journalList.size(); i++)
            ids[i] = journalList.get(i).getId();

        LedgerAdapter.createJournalIntent(this, ids, position);
    }

    @Override
    public void onContextItemClick(View view, int position, long id) {
        LedgerAdapter.onContextItemClick(this, ledgerAdapter, view, position, id);
    }

    @Override
    protected void onDestroy() {
        Services.getInstance(this).unregisterJournalObserver(ledgerAdapter);
        super.onDestroy();
    }
}
