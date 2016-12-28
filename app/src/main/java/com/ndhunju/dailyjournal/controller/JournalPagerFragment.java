package com.ndhunju.dailyjournal.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.journal.JournalFragment;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;

/**
 * Created by Dhunju on 7/29/2016.
 * This fragment shows {@link JournalFragment} in a pager.
 */
public class JournalPagerFragment extends Fragment {

    public static final String TAG = JournalPagerAdapter.class.getSimpleName();

    Services mServices;
    Integer currentPos;
    long[] mJournalIds; // id of Journals this pager will show


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServices = Services.getInstance(getContext());
        currentPos = getArguments().getInt(Constants.KEY_JOURNAL_POS, 0);
        mJournalIds = getArguments().getLongArray(JournalPagerActivity.BUNDLE_JOURNAL_IDS);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        View rootView = inflater.inflate(R.layout.fragment_journal_pager, container, false);
        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.journal_view_pager);
        viewPager.setAdapter(new JournalPagerAdapter(appCompatActivity.getSupportFragmentManager()));
        viewPager.setCurrentItem(currentPos);

        setHasOptionsMenu(true);
        return rootView;
    }


    class JournalPagerAdapter extends FragmentStatePagerAdapter {

        public JournalPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            return JournalFragment.newInstance(mJournalIds[position], position);
        }

        @Override
        public int getCount() {
            return mJournalIds.length;
        }

    }

}
