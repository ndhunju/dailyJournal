package com.ndhunju.dailyjournal.controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.backup.BackupActivity;
import com.ndhunju.dailyjournal.controller.erase.EraseActivity;
import com.ndhunju.dailyjournal.service.KeyValPersistence;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;
import com.ndhunju.folderpicker.FolderPickerDialogFragment;
import com.ndhunju.folderpicker.OnDialogBtnClickedListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends NavDrawerActivity implements OnDialogBtnClickedListener {

	private static final int REQUEST_CODE_COMPANY_SETTING = 34534;
    private static final int REQUEST_CODE_BACKUP_DIR_PRINTABLE = 9834;
    private static final int SHORTCUT_COLS_NUM = 3;

    // member variables
	Services mServices;
    // view variables
    RecyclerView mRecyclerView;
	TextView mCompanyName;
	TextView mFinancialYear;
	TextView mDrAmount;
	TextView mCrAmount;
	TextView mTotal;

	@Override
	public void onCreate(android.os.Bundle arg0) {
		super.onCreate(arg0);

        addContentFrame(R.layout.activity_home);

        // wire up and set up
		mFinancialYear = (TextView) findViewById(R.id.activity_home_financial_year);
		mCompanyName = (TextView) findViewById(R.id.activity_home_company_name);
		mDrAmount = (TextView) findViewById(R.id.activity_home_dr_balance);
		mCrAmount = (TextView) findViewById(R.id.activity_home_cr_balance);
		mTotal    = (TextView) findViewById(R.id.activity_home_total_balance);

		findViewById(R.id.activity_home_refresh_balance).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                setCompanySettings();
                setUserBalance();
			}
		});

        mRecyclerView = (RecyclerView) findViewById(R.id.activity_home_recycler_view);
        mRecyclerView.setItemAnimator(UtilsView.getDefaultItemAnimator());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), SHORTCUT_COLS_NUM,
                GridLayoutManager.VERTICAL,
                false);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(new ShortCutAdapter());

		mServices = Services.getInstance(getContext());

		setCompanySettings();

		setUserBalance();

	}

	private boolean setCompanySettings() {

		if (TextUtils.isEmpty(mServices.getCompanyName())) {
			startActivityForResult(new Intent(getContext(), CompanySettingsActivity.class), REQUEST_CODE_COMPANY_SETTING);
			return false;
		}

		mCompanyName.setText(mServices.getCompanyName());
		mFinancialYear.setText(getString(R.string.msg_financial_year, UtilsFormat.formatDate(mServices.getFinancialYear(), getContext())));
		return true;
	}

	private void setUserBalance() {
		double drBalance = mServices.getDebitTotal();
		double crBalance = mServices.getCreditTotal();
		double balance = drBalance - crBalance;

		((TextView) findViewById(R.id.activity_home_cr_text)).setText(UtilsFormat.getUserDrFromPref(getContext()));
		((TextView) findViewById(R.id.activity_home_dr_text)).setText(UtilsFormat.getUserCrFromPref(getContext()));

		mDrAmount.setText(UtilsFormat.formatCurrency(drBalance, getContext()));
		mCrAmount.setText(UtilsFormat.formatCurrency(crBalance, getContext()));
		mTotal.setText(UtilsFormat.formatCurrency(balance, getContext()));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_COMPANY_SETTING) {
			setCompanySettings();
		}
	}

    @Override
    public void onDialogBtnClicked(Intent data, int whichBtn, int result, int requestCode) {

        switch (requestCode) {
            case REQUEST_CODE_BACKUP_DIR_PRINTABLE:
                ToolsActivity.onBackUpDirForPrintableSelected(getContext(), data, whichBtn, result);
                break;
        }
    }

	private class ShortCutAdapter extends RecyclerView.Adapter {

        final String PREF_KEY_SAVED_SHORTCUT = "PREF_KEY_SAVED_SHORTCUT";
        final String SAVED_SHORTCUT_DELIMITER = ";";

        ShortCut[] allShortCuts = {
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
                FolderPickerDialogFragment dpdf = FolderPickerDialogFragment.newInstance(null, REQUEST_CODE_BACKUP_DIR_PRINTABLE);
                dpdf.show(getFragmentManager(), FolderPickerDialogFragment.class.getName());
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
