package com.ndhunju.dailyjournal.controller.party;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.BaseActivity;
import com.ndhunju.dailyjournal.controller.journal.JournalNewActivity;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link PartyListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link PartyDetailLedgerRowFragment}.
 */
public class PartyDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_detail);


        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(Constants.KEY_PARTY_ID, getIntent().getStringExtra(Constants.KEY_PARTY_ID));
            arguments.putInt(Constants.KEY_POS, getIntent().getIntExtra(Constants.KEY_POS, -1));

            PreferenceService ps  = PreferenceService.from(this);
            int pos = ps.getVal(R.string.key_pref_ledger_view, 0);

            PartyDetailFragment fragment;

            switch(pos) {
                default:
                case 0: //Card View
                    fragment = new PartyDetailLedgerCardFragment();
                    break;
                case 1: //Classic View
                    fragment = new PartyDetailLedgerRowFragment();
                    break;
            }

            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(UtilsFormat.getPartyFromPref(this));
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton newJournalFab = (FloatingActionButton) findViewById(
                R.id.activity_party_detail_fab
        );
        newJournalFab.setOnClickListener(v -> {
            Intent newPartyIntent = new Intent(PartyDetailActivity.this, JournalNewActivity.class);
            newPartyIntent.putExtra(
                    Constants.KEY_PARTY_ID,
                    getIntent().getStringExtra(Constants.KEY_PARTY_ID)
            );
            startActivity(newPartyIntent);
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, PartyListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Load the Ad
        FrameLayout adViewContainer = findViewById(R.id.activity_party_detail_ad_view);
        UtilsView.addAdView(
                adViewContainer,
                getString(R.string.admob_party_detail_ad_unit_id),
                "PartyDetailScreen"
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsService.INSTANCE.logScreenViewEvent("PartyDetail");
    }
}
