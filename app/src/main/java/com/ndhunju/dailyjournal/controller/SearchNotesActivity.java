package com.ndhunju.dailyjournal.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.party.LedgerCardAdapter;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;

/**
 * Created by Dhunju on 8/14/2016.
 */
public class SearchNotesActivity extends AppCompatActivity {

    EditText keywordEditText;
    Button searchJournalNotes;
    RecyclerView recyclerView;

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
                recyclerView.setAdapter(new LedgerCardAdapter(SearchNotesActivity.this,
                        Services.getInstance(getApplicationContext()).journalDAO.searchNotes(keyword)));
            }
        });


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
}
