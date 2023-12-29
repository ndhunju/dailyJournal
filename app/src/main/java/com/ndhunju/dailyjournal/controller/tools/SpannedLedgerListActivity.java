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
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.Utils;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Dhunju on 8/14/2016.
 */
public class SpannedLedgerListActivity
        extends BaseActivity
        implements OnDatePickerDialogBtnClickedListener,
        LedgerAdapter.OnItemClickListener {

    private static final int REQUEST_START_DATE = 6656;
    private static final int REQUEST_END_DATE = 5000;

    Calendar startDate;
    Calendar endDate;

    Button startDateBtn;
    Button endDateBtn;
    Button findJournalBtn;
    RecyclerView recyclerView;
    LedgerAdapter ledgerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spanned_ledger);

        startDate = Calendar.getInstance();
        endDate   = Calendar.getInstance();
        Utils.removeValuesBelowHours(startDate);
        Utils.removeValuesBelowHours(endDate);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(UtilsFormat.getJournalFromPref(this));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        startDateBtn = (Button) findViewById(R.id.activity_spanned_ledger_start_date_btn);
        endDateBtn   = (Button) findViewById(R.id.activity_spanned_ledger_end_date_btn);
        findJournalBtn = (Button) findViewById(R.id.activity_spanned_ledger_find);
        recyclerView = (RecyclerView) findViewById(R.id.activity_spanned_ledger_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        startDateBtn.setText(UtilsFormat.formatDate(startDate.getTime(), getApplicationContext()));
        startDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment dpf = DatePickerFragment.newInstance(startDate.getTime(), REQUEST_START_DATE);
                dpf.show(getSupportFragmentManager(), DatePickerFragment.TAG);
                //the result is delivered to OnDialoguePressedOk()
            }
        });

        endDateBtn.setText(UtilsFormat.formatDate(endDate.getTime(), getApplicationContext()));
        endDateBtn.setOnClickListener(v -> {
            DatePickerFragment dpf = DatePickerFragment.newInstance(
                    endDate.getTime(),
                    REQUEST_END_DATE
            );
            dpf.show(getSupportFragmentManager(), DatePickerFragment.TAG);
        });

        findJournalBtn.setOnClickListener(v -> ledgerAdapter
                .setJournals(Services.getInstance(getApplicationContext())
                        .findByDate(startDate.getTimeInMillis(), endDate.getTimeInMillis())));

        ledgerAdapter = new LedgerCardAdapter(this, new ArrayList<Journal>());
        ledgerAdapter.setLayoutId(R.layout.spanned_ledger_card);
        ledgerAdapter.setOnItemClickListener(SpannedLedgerListActivity.this);
        Services.getInstance(this).registerJournalObserver(ledgerAdapter);
        recyclerView.setAdapter(ledgerAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsService.INSTANCE.logScreenViewEvent("SpannedLedgerList");
    }

    @Override
    public void onDialogBtnClicked(Intent data, @ButtonType int whichBtn, int result, int requestCode) {
        if (result != Activity.RESULT_OK) return;

        Calendar calendar = (Calendar) data.getSerializableExtra(DatePickerFragment.EXTRA_CAL);
        Utils.removeValuesBelowHours(calendar);

        switch (requestCode) {

            case REQUEST_START_DATE:
                startDate = calendar;
                startDateBtn.setText(UtilsFormat.formatDate(startDate.getTime(), getApplicationContext()));
                break;

            case REQUEST_END_DATE:
                endDate = calendar;
                endDateBtn.setText(UtilsFormat.formatDate(endDate.getTime(), getApplicationContext()));
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
