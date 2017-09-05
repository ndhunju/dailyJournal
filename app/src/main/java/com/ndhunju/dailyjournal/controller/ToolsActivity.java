package com.ndhunju.dailyjournal.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.backup.BackupActivity;
import com.ndhunju.dailyjournal.controller.backup.ExportPartiesReportAsync;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsView;
import com.ndhunju.folderpicker.FolderPickerDialogFragment;
import com.ndhunju.folderpicker.OnDialogBtnClickedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ndhunju on 1/7/17.
 * This activity groups together all the tools this app
 * has to offer.
 */

public class ToolsActivity extends NavDrawerActivity implements OnDialogBtnClickedListener {

    private static final int REQUEST_CODE_BACKUP_DIR_PRINTABLE = 1264;

    RecyclerView recyclerView;
    int columnCount = 2;      // default column count

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addContentFrame(R.layout.activity_tools);

        // get column count value from the resources
        columnCount = getResources().getInteger(R.integer.activity_tools_column_count);

        // wire up and set up
        recyclerView = (RecyclerView) findViewById(R.id.activity_tools_recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), columnCount,
                GridLayoutManager.VERTICAL,
                false);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(new ToolsAdapter());
    }

    @Override
    public void onDialogBtnClicked(Intent data, int whichBtn, int result, int requestCode) {

        switch (requestCode) {

            case REQUEST_CODE_BACKUP_DIR_PRINTABLE:
                if (result != Activity.RESULT_OK)
                    UtilsView.toast(getContext(), getString(R.string.str_failed));
                if (whichBtn == OnDialogBtnClickedListener.BUTTON_POSITIVE) {
                    data.getData();
                    final String dir = data.getStringExtra(FolderPickerDialogFragment.KEY_CURRENT_DIR);
                    final Services services = Services.getInstance(getContext());

                    // let the user choose the type of printable she wants to export
                    String[] options = ExportPartiesReportAsync.getStrTypes();
                    new AlertDialog.Builder(getContext()).setItems(options,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int optionIndex) {
                                    createAllOrSelectPartyDialog(services, dir, optionIndex);
                                }
                            })
                            .create().show();
                }

                break;

        }

    }

    private void createAllOrSelectPartyDialog(final Services services, final String dir, final int optionIndex) {
        //Let the user choose the parties
        final List<Party> parties = services.getParties();

        CharSequence[] options = getResources().getStringArray(R.array.options_export_print);
        AlertDialog chooseDialog = new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.str_choose))
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: //All parties
                                new ExportPartiesReportAsync(getActivity(), dir, ExportPartiesReportAsync.Type.values()[optionIndex]).execute(parties);
                                break;

                            case 1: //Select parties
                                createPartySelectDialogToExport(services, parties, dir, optionIndex).show();
                                break;

                        }
                    }
                }).create();

        chooseDialog.show();
    }

    private AlertDialog createPartySelectDialogToExport(Services services, final List<Party> parties, final String dir, final int optionIndex) {
        final ArrayList<Party> selectedParties = new ArrayList<>();

        // create array of Parties' name
        String[] allParties = new String[parties.size()];
        for (int i = 0; i < parties.size(); i++)
            allParties[i] = parties.get(i).getName();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.msg_choose, getString(R.string.str_contact)));
        builder.setNegativeButton(getString(android.R.string.cancel), null);
        builder.setMultiChoiceItems(allParties, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        //Add checked contacts into selectedParties list
                        if (b) selectedParties.add(parties.get(i));
                        else selectedParties.remove(parties.get(i));
                    }
                });
        builder.setPositiveButton(getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new ExportPartiesReportAsync(getActivity(), dir, ExportPartiesReportAsync.Type.values()[optionIndex]).execute(selectedParties);
                    }
                });

        return builder.create();
    }

    private Activity getActivity() {
        return this;
    }

    private class ToolsAdapter extends RecyclerView.Adapter {

        int[] itemStringResId = {R.string.activity_home_find_journal_by_date, R.string.activity_home_search_journal_notes,
                R.string.activity_home_backup, R.string.activity_home_charts, R.string.str_export_printable};
        int[] itemIconResId = {R.drawable.ic_date_range, R.drawable.ic_search,
                R.drawable.ic_save, R.drawable.ic_chart_pie,R.drawable.ic_content_copy_black_24dp};

        public ToolsAdapter() {}

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ToolVH(LayoutInflater.from(getContext()).inflate(R.layout.item_activity_home, parent, false),
                    parent.getMeasuredWidth()/columnCount) ;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ToolVH) holder).bind(position);
        }

        @Override
        public int getItemCount() {
            return itemStringResId.length;
        }

        class ToolVH extends RecyclerView.ViewHolder implements View.OnClickListener{

            TextView itemName;
            ImageView itemIcon;

            public ToolVH(View itemView, int itemSize) {
                super(itemView);
                itemView.setOnClickListener(this);
                itemName = (TextView) itemView.findViewById(R.id.item_home_activity_name);
                itemIcon = (ImageView)itemView.findViewById(R.id.item_home_activity_icon);

                int margin = UtilsView.dpToPx(getContext(), 5);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(itemSize - 2*margin, itemSize - 2*margin);
                params.setMargins(margin, margin, margin, margin);
                itemView.setLayoutParams(params);

                itemIcon.setMinimumWidth(itemSize/2);
                itemIcon.setMinimumHeight(itemSize/2);
            }

            public void bind(int position) {
                itemView.setTag(itemStringResId[position]);
                itemName.setText(itemStringResId[position]);
                itemIcon.setImageResource(itemIconResId[position]);
            }

            @Override
            public void onClick(View v) {
                switch ((int) v.getTag()) {
                    case R.string.activity_home_find_journal_by_date:
                        startActivity(new Intent(getContext(), SpannedLedgerListActivity.class));
                        break;
                    case R.string.activity_home_backup:
                        startActivity(new Intent(getContext(), BackupActivity.class));
                        break;
                    case R.string.activity_home_search_journal_notes:
                        startActivity(new Intent(getContext(), SearchNotesActivity.class));
                        break;
                    case R.string.activity_home_charts:
                        startActivity(new Intent(getContext(), ChartsActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        break;
                    case R.string.str_export_printable:
                        FolderPickerDialogFragment dpdf = FolderPickerDialogFragment.newInstance(null, REQUEST_CODE_BACKUP_DIR_PRINTABLE);
                        dpdf.show(getFragmentManager(), FolderPickerDialogFragment.class.getName());
                        break;
                }
            }
        }
    }

}
