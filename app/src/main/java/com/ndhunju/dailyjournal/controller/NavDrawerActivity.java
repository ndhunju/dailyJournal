package com.ndhunju.dailyjournal.controller;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.journal.JournalNewActivity;
import com.ndhunju.dailyjournal.controller.party.PartyListActivity;
import com.ndhunju.dailyjournal.controller.preference.MyPreferenceActivity;
import com.ndhunju.dailyjournal.util.UtilsFormat;

/**
 * Created by Dhunju on 6/26/2016.
 */
public class NavDrawerActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ViewGroup mContentFrame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        mContentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.title_activity_journal) {
            //CharSequence activityTitle;
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                //getSupportActionBar().setTitle(activityTitle);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //activityTitle = getSupportActionBar().getTitle();
                //getSupportActionBar().setTitle(getString(R.string.nav_menu));
            }
        };
        mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_file_upload_black_24dp);

        try {
            setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        } catch (Exception ignore) {}

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        NavListAdapter adapter = new NavListAdapter(getContext());
        adapter.add(R.string.str_dashboard, R.drawable.ic_dashboard_black_48dp)
                .add(UtilsFormat.getJournalFromPref(getContext()).contains(getString(R.string.str_journal)) ? R.string.nav_item_journal : R.string.nav_item_transaction, R.drawable.ic_journal)
                .add(UtilsFormat.getPartyFromPref(getContext()).contains(getString(R.string.str_party)) ? R.string.nav_item_party : R.string.nav_item_account, R.drawable.ic_nav_parties)
                .add(R.string.nav_item_preference, R.drawable.ic_settings_black_48dp)
                .add(R.string.nav_item_tools,    R.drawable.ic_tools)
                .add(R.string.nav_item_share,    R.drawable.ic_share)
                .setIconTint(ContextCompat.getColor(getContext(), R.color.blue_medium));
        // Set the adapter for the list view
        mDrawerList.setAdapter(adapter);

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(this);

        //UtilsFormat.getPartyFromPref(getActivity())

    }

    public void addContentFrame(@LayoutRes int resource) {
        mContentFrame.addView(getLayoutInflater().inflate(resource, mContentFrame, false));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        switch ((int) id) {
            default:
            case R.mipmap.ic_app:
                // do nothing
                break;
            case R.drawable.ic_dashboard_black_48dp:
                if (this instanceof HomeActivity) return;
                startActivity(new Intent(getContext(), HomeActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.drawable.ic_journal:
                if (this instanceof JournalNewActivity) return;
                startActivity(new Intent(getContext(), JournalNewActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.drawable.ic_nav_parties:
                if (this instanceof PartyListActivity) return;
                startActivity(new Intent(getContext(), PartyListActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.drawable.ic_settings_black_48dp:
                startActivity(new Intent(getContext(), MyPreferenceActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.drawable.ic_tools:
                if (this instanceof ToolsActivity) return;
                startActivity(new Intent(getContext(), ToolsActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.drawable.ic_share:
                //user clicked on Share option
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.msg_share_subject));
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_share_body) + " "
                        + getString(R.string.link_app));
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, getString(R.string.str_choose)));
                break;
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) return true;
        else return super.onOptionsItemSelected(item);
    }

    public Context getContext(){
        return NavDrawerActivity.this;
    }

    static class NavListAdapter extends IconTextAdapter {

        public NavListAdapter(Context context) {
            super(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position == 0) {
                return LayoutInflater.from(getContext()).inflate(R.layout.nav_header, parent, false);
            } else {
                return super.getView(position, convertView, parent);
            }
        }

        @Override
        public long getItemId(int position) {
            if (position == 0) return R.mipmap.ic_app;
            return super.getItemId(position - 1);
        }

        @Override
        public String getItem(int position) {
            if (position == 0) return null;
            return super.getItem(position - 1);
        }

        @Override
        public Drawable getItemIcon(int position) {
            if (position == 0) return null;
            return super.getItemIcon(position -1);
        }

        @Override
        public int getCount() {
            return super.getCount() + 1;
        }
    }

}
