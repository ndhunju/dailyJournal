package com.ndhunju.dailyjournal.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.journal.JournalFragment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;

import java.util.List;

/**
 * Created by Dhunju on 7/29/2016.
 */
public class JournalPagerFragment extends Fragment {

    public static final String TAG = JournalPagerAdapter.class.getSimpleName();
    public static final String BUNDLE_JOURNALS = TAG + "journals";

    Services mServices;
    Party mParty;
    List<Journal> mJournals;

        @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mServices = Services.getInstance(getContext());
        long mPartyId = getArguments().getLong(Constants.KEY_PARTY_ID, Constants.ID_NEW_PARTY);
        mParty = mServices.getParty(mPartyId);
        mJournals = mServices.getJournals(mPartyId);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        View rootView = inflater.inflate(R.layout.fragment_journal_pager, container, false);
        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.journal_view_pager);
        viewPager.setAdapter(new JournalPagerAdapter(appCompatActivity.getSupportFragmentManager()));
        viewPager.setCurrentItem(getArguments().getInt(Constants.KEY_JOURNAL_POS, 0));

        setHasOptionsMenu(true);
        return rootView;
    }


    class JournalPagerAdapter extends FragmentStatePagerAdapter {

        public JournalPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            mJournals.get(position).setTag(position);
            return JournalFragment.newInstance(mJournals.get(position).getId(), mJournals.get(position).getPartyId(), position);
        }

        @Override
        public int getCount() {
            return mJournals.size();
        }

    }

}
