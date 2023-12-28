package com.ndhunju.dailyjournal.controller.export;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.BaseActivity;
import com.ndhunju.dailyjournal.controller.ItemDescriptionAdapter;
import com.ndhunju.dailyjournal.controller.backup.ExportPartiesReportAsync;
import com.ndhunju.dailyjournal.controller.party.PartyListFragment;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.util.ArrayList;
import java.util.List;

public class ExportPrintableActivity extends BaseActivity {

    // Constants

    // View member variables
    ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_printable);

        setSupportActionBar(findViewById(R.id.toolbar));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = findViewById(R.id.export_printable_option_list_view);

        setupExportPrintablesOptions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsService.INSTANCE.logScreenViewEvent("ExportPrintable");
    }

    private void setupExportPrintablesOptions() {
        String[] names = getResources().getStringArray(R.array.options_export_print_location);
        String[] descriptions = getResources().getStringArray(R.array.options_export_print_location_description);
        ItemDescriptionAdapter.Item[] items = new ItemDescriptionAdapter.Item[names.length];

        for (int i = 0; i < names.length; i++) {
            items[i] = new ItemDescriptionAdapter.Item(names[i], descriptions[i]);
        }

        listView.setAdapter(new ItemDescriptionAdapter(getActivity(), items));

        listView.setOnItemClickListener((adapterView, view, which, id) -> {

            switch (which) {
                case 0: // Local Storage
                    onBackUpDirForPrintableSelected(
                            getActivity(),
                            UtilsFile.getPublicDownloadDir()
                    );
                    break;

                case 1: // Other Apps
                    PartyListFragment.createDialogForSharePartiesReport(getActivity()).show();
                    break;

                case 2: // Attachments
                    break;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private AppCompatActivity getActivity() {
        return this;
    }

    public static void onBackUpDirForPrintableSelected(final Activity activity, String dir) {
        // let the user choose the type of printable she wants to export
        ItemDescriptionAdapter.Item[] options = ExportPartiesReportAsync.getStrTypes(activity);

        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setAdapter(
                        new ItemDescriptionAdapter(activity, options),
                        (dialogInterface, optionIndex) -> createAllOrSelectPartyDialog(
                                activity,
                                dir,
                                optionIndex)
                ).create();

        alertDialog.show();
    }

    public static void createAllOrSelectPartyDialog(final Activity activity, final String dir, final int optionIndex) {
        //Let the user choose the parties
        final List<Party> parties = Services.getInstance(activity).getParties();

        CharSequence[] options = activity.getResources().getStringArray(R.array.options_export_print);
        AlertDialog chooseDialog = new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.str_choose))
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // All parties
                            //noinspection unchecked
                            new ExportPartiesReportAsync(
                                    activity,
                                    dir,
                                    ExportPartiesReportAsync.Type.values()[optionIndex]
                            ).execute(parties);
                            break;

                        case 1: // Select parties
                            createPartySelectDialogToExport(
                                    activity,
                                    parties,
                                    dir,
                                    optionIndex
                            ).show();
                            break;

                    }
                }).create();

        chooseDialog.show();
    }

    public static AlertDialog createPartySelectDialogToExport(
            final Activity activity,
            final List<Party> parties,
            final String dir,
            final int optionIndex
    ) {
        final ArrayList<Party> selectedParties = new ArrayList<>();

        // create array of Parties' name
        String[] allParties = new String[parties.size()];
        for (int i = 0; i < parties.size(); i++)
            allParties[i] = parties.get(i).getName();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.msg_choose, UtilsFormat.getPartyFromPref(activity)));
        builder.setNegativeButton(activity.getString(android.R.string.cancel), null);
        builder.setMultiChoiceItems(allParties, null,
                (dialogInterface, i, b) -> {
                    //Add checked contacts into selectedParties list
                    if (b) selectedParties.add(parties.get(i));
                    else selectedParties.remove(parties.get(i));
                });
        builder.setPositiveButton(activity.getString(android.R.string.ok),
                (dialogInterface, i) -> new ExportPartiesReportAsync(
                        activity,
                        dir,
                        ExportPartiesReportAsync.Type.values()[optionIndex]
                ).execute(selectedParties));

        return builder.create();
    }
}
