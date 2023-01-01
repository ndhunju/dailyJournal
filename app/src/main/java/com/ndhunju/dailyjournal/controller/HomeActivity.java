package com.ndhunju.dailyjournal.controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.backup.BackupActivity;
import com.ndhunju.dailyjournal.controller.erase.EraseActivity;
import com.ndhunju.dailyjournal.controller.export.ExportPrintableActivity;
import com.ndhunju.dailyjournal.controller.fragment.AppRater;
import com.ndhunju.dailyjournal.controller.journal.JournalNewActivity;
import com.ndhunju.dailyjournal.controller.party.PartyListActivity;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.KeyValPersistence;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;
import com.ndhunju.folderpicker.OnDialogBtnClickedListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeActivity extends NavDrawerActivity implements Services.Listener {

	private static final int REQUEST_CODE_COMPANY_SETTING = 34534;
    private static final int SHORTCUT_COLS_NUM = 3;

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
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), SHORTCUT_COLS_NUM,
                GridLayoutManager.VERTICAL,
                false);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(new ShortCutAdapter());

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
        // Ask users to rate the app after they have used for 20 times
        AppRater rater = new AppRater(HomeActivity.this);
        rater.setLaunchesBeforePrompt(20);
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

    private class ShortCutAdapter extends RecyclerView.Adapter {

        final String PREF_KEY_SAVED_SHORTCUT = "PREF_KEY_SAVED_SHORTCUT";
        final String SAVED_SHORTCUT_DELIMITER = ";";

        ShortCut[] allShortCuts = {
                new NewJournalShortCut(),
                new PartiesShortCut(),
                new DailyReportShortCut(),
                new FindJournalShortCut(),
                new SearchJournalByNoteShortCut(),
                new ExportPrintableShortCut(),
                new BackupShortCut(),
                new ChartsShortCut(),
                new EraseShortcut()
        };

        List<ShortCut> selectedShortCuts = new ArrayList<>();
        KeyValPersistence keyValPersistence;

		/*package*/ ShortCutAdapter() {
            keyValPersistence = KeyValPersistence.from(getContext());

            // retrieve saved shortcuts in order from preferences
            String savedShortCuts = keyValPersistence.get(PREF_KEY_SAVED_SHORTCUT, "");
            for (String shortCutName : savedShortCuts.split(SAVED_SHORTCUT_DELIMITER)) {
                for (ShortCut shortCut : allShortCuts) {
                    if (shortCutName.equals(shortCut.mPrefVal)) {
                        selectedShortCuts.add(shortCut);
                    }
                }
            }

            selectedShortCuts.add(new AddNewShortCut());
        }

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new ShortCutVH(LayoutInflater.from(getContext()).inflate(R.layout.item_activity_home, parent, false),
					parent.getMeasuredWidth() / SHORTCUT_COLS_NUM) ;
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
			((ShortCutVH) holder).bind(position);
		}

		@Override
		public int getItemCount() {
			return selectedShortCuts.size();
		}

		public ShortCut getItem(int position) {
            return selectedShortCuts.get(position);
        }

        public void clearSelectedShortCuts() {
            selectedShortCuts.clear();
        }

        /*package*/ class ShortCutVH extends RecyclerView.ViewHolder implements View.OnClickListener{

			TextView itemName;
			ImageView itemIcon;

            /*package*/ ShortCutVH(View itemView, int itemSize) {
				super(itemView);
				itemView.setOnClickListener(this);
				itemName = (TextView) itemView.findViewById(R.id.item_home_activity_name);
				itemIcon = (ImageView)itemView.findViewById(R.id.item_home_activity_icon);

				int margin = UtilsView.dpToPx(getContext(), 5);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(itemSize - 2 * margin, itemSize - 2 * margin);
				params.setMargins(margin, margin, margin, margin);
				itemView.setLayoutParams(params);

				itemIcon.setMinimumWidth(itemSize/2);
				itemIcon.setMinimumHeight(itemSize/2);
			}

            /*package*/ void bind(int position) {
				itemView.setTag(getItem(position));
				itemName.setText(getItem(position).mStringResId);
				itemIcon.setImageResource(getItem(position).mIconResId);
			}

			@Override
			public void onClick(View v) {
                ((ShortCut) v.getTag()).onClick();
			}
		}

        private class ShortCut {
            @StringRes int mStringResId;
            @DrawableRes int mIconResId;
            String mPrefVal;

            ShortCut(int stringResId, int iconResId) {
                mPrefVal = this.getClass().getSimpleName();
                mStringResId = stringResId;
                mIconResId = iconResId;
            }

            void onClick() {}

        }

        private class AddNewShortCut extends ShortCut {

            AddNewShortCut() {
                super(R.string.str_add_shortcut, R.drawable.ic_add_24dp);
            }

            @Override
            void onClick() {
                super.onClick();

                // create array of ShortCut's name
                String[] shortCutsName = new String[allShortCuts.length];
                final boolean[] selectedShortCut = new boolean[allShortCuts.length];

                for (int i = 0; i < allShortCuts.length; i++) {
                    shortCutsName[i] = getString(allShortCuts[i].mStringResId);
                    selectedShortCut[i] = selectedShortCuts.contains(allShortCuts[i]);
                }

                // show dialog where user can select from available shortcuts
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.str_select_shortcut)
                        .setMultiChoiceItems(shortCutsName, selectedShortCut, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    // add newly selected at first
                                    selectedShortCuts.add(0, allShortCuts[which]);
                                    notifyItemInserted(0);
                                } else {
                                    for (int i = 0 ; i < selectedShortCuts.size(); i++) {
                                        if (selectedShortCuts.get(i).equals(allShortCuts[which])) {
                                            selectedShortCuts.remove(i);
                                            notifyItemRemoved(i);
                                        }
                                    }
                                }
                            }
                        }).setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // save selected short cuts to preferences
                                StringBuilder selectedShortCutNames = new StringBuilder();
                                for (ShortCut shortCut : selectedShortCuts) {
                                    selectedShortCutNames.append(shortCut.mPrefVal).append(SAVED_SHORTCUT_DELIMITER);
                                }
                                keyValPersistence.putString(PREF_KEY_SAVED_SHORTCUT, selectedShortCutNames.toString());
                            }
                }).create().show();
            }
        }

        private class NewJournalShortCut extends ShortCut {

            NewJournalShortCut() {
                super(UtilsFormat.getJournalFromPref(getContext()).contains(getString(R.string.str_journal))
                                ? R.string.nav_item_journal : R.string.nav_item_transaction,
                        R.drawable.ic_journal_shortcut);
            }

            @Override
            void onClick() {
                super.onClick();
                startActivity(new Intent(getContext(), JournalNewActivity.class).putExtra(Constants.KEY_JOURNAL_ID, Constants.ID_NEW_JOURNAL));
            }
        }

        private class PartiesShortCut extends ShortCut {

            PartiesShortCut() {
                super(UtilsFormat.getPartyFromPref(getContext()).contains(getString(R.string.str_party))
                        ? R.string.nav_item_party : R.string.nav_item_account,
                        R.drawable.ic_parties_shortcut);
            }

            @Override
            void onClick() {
                super.onClick();
                startActivity(new Intent(getContext(), PartyListActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        }

        private class DailyReportShortCut extends ShortCut {

            DailyReportShortCut() {
                super(R.string.title_activity_daily_report, R.drawable.ic_daily_report_24dp);
            }

            @Override
            void onClick() {
                super.onClick();
                startActivity(new Intent(getContext(), DailyReportActivity.class));
            }
        }

        private class FindJournalShortCut extends ShortCut {

            FindJournalShortCut() {
                super(R.string.activity_home_find_journal_by_date, R.drawable.ic_date_range);
            }

            @Override
            void onClick() {
                super.onClick();
                startActivity(new Intent(getContext(), SpannedLedgerListActivity.class));
            }
        }

        private class SearchJournalByNoteShortCut extends ShortCut {

            SearchJournalByNoteShortCut() {
                super(R.string.activity_home_search_journal_notes, R.drawable.ic_search);
            }

            @Override
            void onClick() {
                super.onClick();
                startActivity(new Intent(getContext(), SearchNotesActivity.class));
            }
        }

        private class BackupShortCut extends ShortCut {

            BackupShortCut() {
                super(R.string.activity_home_backup, R.drawable.ic_save);
            }

            @Override
            void onClick() {
                super.onClick();
                startActivity(new Intent(getContext(), BackupActivity.class));
            }
        }

        private class ChartsShortCut extends ShortCut {

            ChartsShortCut() {
                super(R.string.activity_home_charts, R.drawable.ic_chart_pie);
            }

            @Override
            void onClick() {
                super.onClick();
                startActivity(new Intent(getContext(), ChartsActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        }

        private class ExportPrintableShortCut extends ShortCut {

            ExportPrintableShortCut() {
                super(R.string.str_export_printable, R.drawable.ic_content_copy_black_24dp);
            }

            @Override
            void onClick() {
                super.onClick();
                startActivity(new Intent(getContext(), ExportPrintableActivity.class));
            }
        }

        private class EraseShortcut extends ShortCut {

            EraseShortcut() {
                super(R.string.title_activity_erase, R.drawable.ic_delete_black_24dp);
            }

            @Override
            void onClick() {
                super.onClick();
                startActivity(new Intent(getContext(), EraseActivity.class));
            }
        }
	}
}
