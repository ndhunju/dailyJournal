package com.ndhunju.dailyjournal.controller.party;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.core.view.ViewCompat;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.NavDrawerActivity;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.util.UtilsFormat;

/**
 * An activity representing a list of Parties. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PartyDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link PartyListFragment} and the item details
 * (if present) is a {@link PartyDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link PartyListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class PartyListActivity extends NavDrawerActivity implements PartyListFragment.Callbacks {

    //Whether or not the activity is in two-pane mode, i.e. running on a tablet
    private boolean mTwoPane;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addContentFrame(R.layout.activity_party_list);

        //Fragments are added by android as defined in layout
        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the sw600dp screen
            mTwoPane = true;

        } else {
            mTwoPane = false;
            /** {@link PartyDetailFragment} is attached, remove it. It could be attached on device rotation from
             * Landscape to Portrait*/
            Fragment partyFragment = getSupportFragmentManager().findFragmentByTag(PartyDetailFragment.TAG);
            if (partyFragment != null) getSupportFragmentManager().beginTransaction().remove(partyFragment).commit();
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        if(toolbar != null) {
            toolbar.setTitle(UtilsFormat.getPartyFromPref(this));
            setSupportActionBar(toolbar);
        }

        // Show the Up button in the action bar.
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    /**
     * Callback method from {@link PartyListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id, View view, int position) {

        if (mTwoPane) {

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(Constants.KEY_PARTY_ID, id);
            arguments.putInt(Constants.KEY_POS, position);

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
                    .replace(R.id.item_detail_container, fragment, PartyDetailFragment.TAG).commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, PartyDetailActivity.class);
            detailIntent.putExtra(Constants.KEY_PARTY_ID, id);
            detailIntent.putExtra(Constants.KEY_POS, position);

            View sharedView = view.findViewById(R.id.party_card_circle_iv);
            ViewCompat.setTransitionName(sharedView, getString(R.string.trans_party_img));
            Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this, sharedView,
                    getString(R.string.trans_party_img)).toBundle();
            ActivityCompat.startActivityForResult(this, detailIntent, PartyListFragment.REQUEST_PARTY_INFO_CHGD, bundle);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsService.INSTANCE.logScreenViewEvent("PartyList");
    }
}
