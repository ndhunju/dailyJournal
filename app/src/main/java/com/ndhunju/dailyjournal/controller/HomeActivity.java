package com.ndhunju.dailyjournal.controller;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.fragment.AppRater;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeActivity extends NavDrawerActivity implements Services.Listener {

	private static final int REQUEST_CODE_COMPANY_SETTING = 34534;

    // member variables
	Services mServices;
    boolean mRefreshNeeded;

    // view variables
    View mRefreshHomeBtn;
    TextView mCompanyName;
    ViewPager mSummaryPager;
    RecyclerView mRecyclerView;


	@Override
	public void onCreate(android.os.Bundle arg0) {
		super.onCreate(arg0);

        addContentFrame(R.layout.activity_home);

        // wire up and set up
        mCompanyName = (TextView) findViewById(R.id.activity_home_company_name);

        mRecyclerView = (RecyclerView) findViewById(R.id.activity_home_recycler_view);
        mRecyclerView.setItemAnimator(UtilsView.getDefaultItemAnimator());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(
                getContext(),
                ShortCutAdapter.SHORTCUT_COLS_NUM,
                GridLayoutManager.VERTICAL,
                false
        );
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(new ShortCutAdapter(this));

        mSummaryPager = (ViewPager) findViewById(R.id.activity_home_summary_pager);
        mSummaryPager.setAdapter(new SummaryPagerAdapter());

        TabLayout dotTabLayout = (TabLayout) findViewById(R.id.activity_home_tab_dots);
        dotTabLayout.setupWithViewPager(mSummaryPager);

        mRefreshHomeBtn = findViewById(R.id.activity_home_refresh_home);
        mRefreshHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshHomeView();
            }
        });

        setupSizeForSummaryPager();

		mServices = Services.getInstance(getContext());
        mServices.addListener(this);
        mRefreshNeeded = true;

	}

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (DisclaimerFragmentDialog.shouldShow(getContext())) {
            getSupportFragmentManager().beginTransaction().add(new DisclaimerFragmentDialog(), null).commit();
        }
    }

    private void setupSizeForSummaryPager() {

        mSummaryPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                // Make sure the view is laid out before determining the height
                if (!ViewCompat.isLaidOut(mSummaryPager)) {
                    return;
                }

                ViewGroup summaryRootView = (ViewGroup) findViewById(R.id.item_summary_root);
                int summaryPagerChildHeight = 0;
                // add height of each child
                for (int i = 0; i < summaryRootView.getChildCount(); i++) {
                    summaryPagerChildHeight += summaryRootView.getChildAt(i).getHeight();
                }
                // add top and bottom padding as well
                summaryPagerChildHeight += summaryRootView.getPaddingBottom() + summaryRootView.getPaddingTop();
                // set the height because by default view pager covers the entire screen
                ViewGroup.LayoutParams params = mSummaryPager.getLayoutParams();
                params.height = summaryPagerChildHeight;
                mSummaryPager.setLayoutParams(params);

                // remove this GlobalLayoutListener
                mSummaryPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRefreshNeeded) {
           refreshHomeView();
        }

        askUserToRate();
        AnalyticsService.INSTANCE.logScreenViewEvent("Home");
    }

    int currentSummaryIndex;

    private void refreshHomeView() {
        // save the index of the summary page that user was viewing before refresh
        currentSummaryIndex = mSummaryPager.getCurrentItem();
        mRecyclerView.getAdapter().notifyDataSetChanged();
        mSummaryPager.setAdapter(new SummaryPagerAdapter());
        mSummaryPager.setCurrentItem(currentSummaryIndex);
        setCompanySettings();
        mRefreshNeeded = false;
    }

	private boolean setCompanySettings() {

		if (TextUtils.isEmpty(mServices.getCompanyName())) {
            CompanySettingsActivity.startActivity(this, REQUEST_CODE_COMPANY_SETTING);
			return false;
		}

		mCompanyName.setText(mServices.getCompanyName());
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_COMPANY_SETTING) {
			setCompanySettings();
		}
	}

    @Override
    public void onEraseAll() {
        mRefreshNeeded = true;
        ((ShortCutAdapter) mRecyclerView.getAdapter()).clearSelectedShortCuts();
    }

    private void askUserToRate(){
        // Ask users to rate the app after they have used for 11 times
        AppRater rater = new AppRater(HomeActivity.this);
        rater.setLaunchesBeforePrompt(11);
        rater.setLaunchesBeforeRePrompt(7);
        rater.setPhrases(
                R.string.msg_rate_title,
                R.string.msg_rate_body,
                R.string.str_rate,
                R.string.str_later,
                R.string.str_no_thanks
        );
        rater.show();
    }

    private class SummaryPagerAdapter extends PagerAdapter {

        private class Summary {

            ViewGroup rootView;

            void bind(ViewGroup layout) {
                rootView = layout;

                // format the label based on user's preferences
                ((TextView) rootView.findViewById(R.id.item_summary_journal_count_label)).setText(getString(R.string.str_no_of_journal, UtilsFormat.getJournalFromPref(getContext())));
                ((TextView) rootView.findViewById(R.id.item_summary_cr_text)).setText(UtilsFormat.getUserDrFromPref(getContext()));
                ((TextView) rootView.findViewById(R.id.item_summary_dr_text)).setText(UtilsFormat.getUserCrFromPref(getContext()));

            }

        }

        private class YearlySummary extends Summary{

            @Override
            void bind(ViewGroup layout) {
                super.bind(layout);
                double drBalance = mServices.getDebitTotal();
                double crBalance = mServices.getCreditTotal();
                double balance = drBalance - crBalance;

                ((TextView) rootView.findViewById(R.id.item_summary_title)).setText(getString(R.string.msg_financial_year, UtilsFormat.formatDate(mServices.getFinancialYear(), getContext())));
                ((TextView) rootView.findViewById(R.id.item_summary_journal_count)).setText(String.valueOf(mServices.getTotalJournalCount()));
                ((TextView) rootView.findViewById(R.id.item_summary_dr_balance)).setText(UtilsFormat.formatCurrency(drBalance, getContext()));
                ((TextView) rootView.findViewById(R.id.item_summary_cr_balance)).setText(UtilsFormat.formatCurrency(crBalance, getContext()));
                ((TextView) rootView.findViewById(R.id.item_summary_total_balance)).setText(UtilsFormat.formatCurrency(balance, getContext()));
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

                ((TextView) rootView.findViewById(R.id.item_summary_title)).setText(getString(R.string.msg_todays_date, UtilsFormat.formatDate(new Date(), getContext())));
                ((TextView) rootView.findViewById(R.id.item_summary_journal_count)).setText(String.valueOf(mServices.getTodaysJournalCount()));
                ((TextView) rootView.findViewById(R.id.item_summary_dr_balance)).setText(UtilsFormat.formatCurrency(drBalance, getContext()));
                ((TextView) rootView.findViewById(R.id.item_summary_cr_balance)).setText(UtilsFormat.formatCurrency(crBalance, getContext()));
                ((TextView) rootView.findViewById(R.id.item_summary_total_balance)).setText(UtilsFormat.formatCurrency(balance, getContext()));
            }
        }

        private List<Summary> summaries;

        public SummaryPagerAdapter() {
            summaries = new ArrayList<>();
            summaries.add(new TodaysSummary());
            summaries.add(new YearlySummary());
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            ViewGroup root = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_summary, collection, false);
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

    }

}
