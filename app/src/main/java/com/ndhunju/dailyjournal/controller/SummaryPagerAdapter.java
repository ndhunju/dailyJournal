package com.ndhunju.dailyjournal.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.viewpager.widget.PagerAdapter;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class SummaryPagerAdapter extends PagerAdapter {

    private final Services mServices;
    private final List<Summary> summaries;

    public SummaryPagerAdapter(Services services) {
        mServices = services;
        summaries = new ArrayList<>();
        summaries.add(new TodaysSummary());
        summaries.add(new YearlySummary());
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        ViewGroup root = (ViewGroup) LayoutInflater.from(getContext()).inflate(
                R.layout.item_summary,
                collection,
                false
        );
        summaries.get(position).bind(root);
        collection.addView(root);
        return root;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return summaries.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    private Context getContext() {
        return mServices.getContext();
    }

    private String getString(@StringRes int resId, Object... formatArgs) {
        return mServices.getContext().getString(resId, formatArgs);
    }

    private class Summary {

        ViewGroup rootView;

        void bind(ViewGroup layout) {
            rootView = layout;

            // format the label based on user's preferences
            ((TextView) rootView.findViewById(R.id.item_summary_journal_count_label))
                    .setText(getString(
                            R.string.str_no_of_journal,
                            UtilsFormat.getJournalFromPref(getContext())
                    ));
            ((TextView) rootView.findViewById(R.id.item_summary_cr_text))
                    .setText(UtilsFormat.getUserDrFromPref(getContext()));
            ((TextView) rootView.findViewById(R.id.item_summary_dr_text))
                    .setText(UtilsFormat.getUserCrFromPref(getContext()));

        }

    }

    private class YearlySummary extends Summary {

        @Override
        void bind(ViewGroup layout) {
            super.bind(layout);
            double drBalance = mServices.getDebitTotal();
            double crBalance = mServices.getCreditTotal();
            double balance = drBalance - crBalance;

            ((TextView) rootView.findViewById(R.id.item_summary_title))
                    .setText(getString(
                            R.string.msg_financial_year,
                            UtilsFormat.formatDate(mServices.getFinancialYear(), getContext())
                    ));
            ((TextView) rootView.findViewById(R.id.item_summary_journal_count))
                    .setText(String.valueOf(mServices.getTotalJournalCount()));
            ((TextView) rootView.findViewById(R.id.item_summary_dr_balance))
                    .setText(UtilsFormat.formatCurrency(drBalance, getContext()));
            ((TextView) rootView.findViewById(R.id.item_summary_cr_balance))
                    .setText(UtilsFormat.formatCurrency(crBalance, getContext()));
            ((TextView) rootView.findViewById(R.id.item_summary_total_balance))
                    .setText(UtilsFormat.formatCurrency(balance, getContext()));
        }
    }

    private class TodaysSummary extends Summary {

        @Override
        void bind(ViewGroup layout) {
            super.bind(layout);
            mServices.calculateTodaysDrCrTotal();
            double drBalance = mServices.getTodaysDebit();
            double crBalance = mServices.getTodaysCredit();
            double balance = drBalance - crBalance;

            ((TextView) rootView.findViewById(R.id.item_summary_title))
                    .setText(getString(
                            R.string.msg_todays_date,
                            UtilsFormat.formatDate(new Date(), getContext())
                    ));
            ((TextView) rootView.findViewById(R.id.item_summary_journal_count))
                    .setText(String.valueOf(mServices.getTodaysJournalCount()));
            ((TextView) rootView.findViewById(R.id.item_summary_dr_balance))
                    .setText(UtilsFormat.formatCurrency(drBalance, getContext()));
            ((TextView) rootView.findViewById(R.id.item_summary_cr_balance))
                    .setText(UtilsFormat.formatCurrency(crBalance, getContext()));
            ((TextView) rootView.findViewById(R.id.item_summary_total_balance))
                    .setText(UtilsFormat.formatCurrency(balance, getContext()));
        }
    }

}
