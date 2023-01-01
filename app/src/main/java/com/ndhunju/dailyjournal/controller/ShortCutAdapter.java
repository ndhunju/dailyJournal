package com.ndhunju.dailyjournal.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.backup.BackupActivity;
import com.ndhunju.dailyjournal.controller.erase.EraseActivity;
import com.ndhunju.dailyjournal.controller.export.ExportPrintableActivity;
import com.ndhunju.dailyjournal.controller.journal.JournalNewActivity;
import com.ndhunju.dailyjournal.controller.party.PartyListActivity;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.KeyValPersistence;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

public class ShortCutAdapter extends RecyclerView.Adapter<ShortCutAdapter.ShortCutVH> {

    // Static Variables
    static final int SHORTCUT_COLS_NUM = 3;
    static final String PREF_KEY_SAVED_SHORTCUT = "PREF_KEY_SAVED_SHORTCUT";
    static final String SAVED_SHORTCUT_DELIMITER = ";";
    static final String DEFAULT_SHORTCUTS =
            ShortCut.getPrefVal(NewJournalShortCut.class) + SAVED_SHORTCUT_DELIMITER +
            ShortCut.getPrefVal(PartiesShortCut.class) + SAVED_SHORTCUT_DELIMITER +
            ShortCut.getPrefVal(BackupShortCut.class);

    /**
     * Writes {@link ShortCutAdapter#DEFAULT_SHORTCUTS} to persistence storage.
     * Note: It will override existing values.
     */
    static public void putDefaultShortCutsToPersistence(Context context) {
        KeyValPersistence keyValPersistence = KeyValPersistence.from(context);
        keyValPersistence.putString(PREF_KEY_SAVED_SHORTCUT, DEFAULT_SHORTCUTS);
    }

    // Member Variables
    final Context context;
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

    /*package*/ ShortCutAdapter(Context context) {
        this.context = context;
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

    public Context getContext() {
        return context;
    }

    private String getString(@StringRes int stringResId) {
        return context.getString(stringResId);
    }

    private void startActivity(Intent intent) {
        context.startActivity(intent);
    }

    @NonNull
    @Override
    public ShortCutVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ShortCutVH(LayoutInflater.from(getContext()).inflate(
                R.layout.item_activity_home,
                parent,
                false
        ), parent.getMeasuredWidth() / SHORTCUT_COLS_NUM);
    }

    @Override
    public void onBindViewHolder(@NonNull ShortCutVH holder, int position) {
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

    /*package*/ class ShortCutVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView itemName;
        ImageView itemIcon;

        /*package*/ ShortCutVH(View itemView, int itemSize) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemName = (TextView) itemView.findViewById(R.id.item_home_activity_name);
            itemIcon = (ImageView) itemView.findViewById(R.id.item_home_activity_icon);

            int margin = UtilsView.dpToPx(getContext(), 5);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    itemSize - 2 * margin,
                    itemSize - 2 * margin
            );
            params.setMargins(margin, margin, margin, margin);
            itemView.setLayoutParams(params);

            itemIcon.setMinimumWidth(itemSize / 2);
            itemIcon.setMinimumHeight(itemSize / 2);
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

    private static class ShortCut {
        @StringRes
        int mStringResId;
        @DrawableRes
        int mIconResId;
        String mPrefVal;

        ShortCut(int stringResId, int iconResId) {
            mPrefVal = getPrefVal(this.getClass());
            mStringResId = stringResId;
            mIconResId = iconResId;
        }

        void onClick() {
        }

        static String getPrefVal(Class<? extends ShortCut> klass) {
            return klass.getSimpleName();
        }

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
                    .setMultiChoiceItems(shortCutsName, selectedShortCut, (dialog, which, isChecked) -> {
                        if (isChecked) {
                            // add newly selected at first
                            selectedShortCuts.add(0, allShortCuts[which]);
                            notifyItemInserted(0);
                        } else {
                            for (int i = 0; i < selectedShortCuts.size(); i++) {
                                if (selectedShortCuts.get(i).equals(allShortCuts[which])) {
                                    selectedShortCuts.remove(i);
                                    notifyItemRemoved(i);
                                }
                            }
                        }
                    }).setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
                        // save selected short cuts to preferences
                        StringBuilder selectedShortCutNames = new StringBuilder();
                        for (ShortCut shortCut : selectedShortCuts) {
                            selectedShortCutNames.append(shortCut.mPrefVal).append(SAVED_SHORTCUT_DELIMITER);
                        }
                        keyValPersistence.putString(PREF_KEY_SAVED_SHORTCUT, selectedShortCutNames.toString());
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
            startActivity(new Intent(getContext(), JournalNewActivity.class)
                    .putExtra(Constants.KEY_JOURNAL_ID, Constants.ID_NEW_JOURNAL));
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
