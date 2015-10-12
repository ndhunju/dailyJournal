package com.ndhunju.dailyjournal.controller.party;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;


import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.LockService;
import com.ndhunju.dailyjournal.service.Constants;

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
public class PartyListActivity extends Activity implements PartyListFragment.Callbacks {

    //Whether or not the activity is in two-pane mode, i.e. running on a tablet
    private boolean mTwoPane;

    //Context menu id for {@link PartyListFragment} and {@link PartyDetailFragment}
    public static final int CONTEXT_MENU_DELETE = 0;
    public static final int CONTEXT_MENU_EDIT = 1;
    public static final int CONTEXT_MENU_PARTY_DELETE = 2;
    public static final int CONTEXT_MENU_PARTY_EDIT = 3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_list);

        //Fragments are added by android as defined in layout
        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the sw600dp screen
            mTwoPane = true;
            // In two-pane mode, list items should be given the 'activated' state when touched.
            ((PartyListFragment) getFragmentManager()
                                .findFragmentById(R.id.item_list))
                                .setActivateOnItemClick(true);
        }
    }

    /**
     * Callback method from {@link PartyListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(Constants.KEY_PARTY_ID, id);
            PartyDetailFragment fragment = new PartyDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, PartyDetailActivity.class);
            detailIntent.putExtra(Constants.KEY_PARTY_ID, id);
            startActivityForResult(detailIntent, PartyListFragment.REQUEST_PARTY_INFO_CHGD);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //update pass code time
        LockService.updatePasscodeTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //check pass code
        LockService.checkPassCode(PartyListActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case PartyListFragment.REQUEST_PARTY_INFO_CHGD:
                //relay it to list fragment
                getFragmentManager().findFragmentById(R.id.item_list)
                        .onActivityResult(requestCode, resultCode, data);

                break;
        }
    }
}
