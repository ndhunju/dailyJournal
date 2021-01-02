package com.ndhunju.dailyjournal.controller;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.party.LedgerAdapter;
import com.ndhunju.dailyjournal.controller.party.LedgerCardAdapter;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dhunju on 8/14/2016.
 */
public class SearchNotesActivity extends BaseActivity implements LedgerAdapter.OnItemClickListener{

    EditText keywordEditText;
    Button searchJournalNotes;
    RecyclerView recyclerView;
    LedgerAdapter ledgerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_notes);


        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(UtilsFormat.getJournalFromPref(this));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        keywordEditText = (EditText) findViewById(R.id.activity_search_notes_keyword);
        searchJournalNotes = (Button) findViewById(R.id.activity_search_notes_search);
        recyclerView = (RecyclerView) findViewById(R.id.activity_search_notes_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        searchJournalNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = keywordEditText.getText().toString();
                if (TextUtils.isEmpty(keyword)) keyword = " ";
                ledgerAdapter.setJournals(Services.getInstance(getApplicationContext()).findByNotes(keyword));
            }
        });

        ledgerAdapter = new LedgerCardAdapter(this, new ArrayList<Journal>());
        ledgerAdapter.setOnItemClickListener(SearchNotesActivity.this);
        Services.getInstance(this).registerJournalObserver(ledgerAdapter);
        recyclerView.setAdapter(ledgerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsService.INSTANCE.logScreenViewEvent("SearchNotes");
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
    public void onContextItemClick(View view, int position, long menuID) {
        LedgerAdapter.onContextItemClick(this, ledgerAdapter, view, position, menuID);
    }

    @Override
    protected void onDestroy() {
        Services.getInstance(this).unregisterJournalObserver(ledgerAdapter);
        super.onDestroy();
    }
}
