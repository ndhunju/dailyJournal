package com.ndhunju.dailyjournal.controller.export;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.BaseActivity;
import com.ndhunju.dailyjournal.controller.ItemDescriptionAdapter;
import com.ndhunju.dailyjournal.controller.backup.ExportPartiesReportAsync;
import com.ndhunju.dailyjournal.controller.party.PartyListFragment;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;
import com.ndhunju.folderpicker.FolderPickerDialogFragment;
import com.ndhunju.folderpicker.OnDialogBtnClickedListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class ExportPrintableActivity extends BaseActivity implements OnDialogBtnClickedListener {

    // Constants
    private static final int REQUEST_CODE_BACKUP_DIR_PRINTABLE = 1264;
    private static final int REQUEST_PERMISSIONS_WRITE_STORAGE = 2323;

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

            if (!checkWriteStoragePermission()) {
                return;
            }

            switch (which) {
                case 0: // Local Storage
                    FolderPickerDialogFragment dpdf = FolderPickerDialogFragment.newInstance(null, REQUEST_CODE_BACKUP_DIR_PRINTABLE);
                    dpdf.show(getFragmentManager(), FolderPickerDialogFragment.class.getName());
                    break;

                case 1: // Other Apps
                    PartyListFragment.createDialogForSharePartiesReport(getActivity()).show();
                    break;

                case 2: // Attachments
                    break;
            }
        });
    }

    private boolean checkWriteStoragePermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getActivity().requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_WRITE_STORAGE);
            }
            return false;
        }

        return true;
    }

    // After user grants permission, resume what user clicked on
//    @Override
////    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
////        if (requestCode == REQUEST_PERMISSIONS_WRITE_STORAGE) {
////            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
////                showExportPrintableOptions();
////            }
////        }
////        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
////    }

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

    @Override
    public void onDialogBtnClicked(Intent data, int whichBtn, int result, int requestCode) {

        switch (requestCode) {

            case REQUEST_CODE_BACKUP_DIR_PRINTABLE:
                onBackUpDirForPrintableSelected(getActivity(), data, whichBtn, result);
                break;

        }

    }

    public static void onBackUpDirForPrintableSelected(final Activity activity, Intent data, int whichBtn, int result) {
        if (result != Activity.RESULT_OK)
            UtilsView.toast(activity, activity.getString(R.string.str_failed));
        if (whichBtn == OnDialogBtnClickedListener.BUTTON_POSITIVE) {
            data.getData();
            final String dir = data.getStringExtra(FolderPickerDialogFragment.KEY_CURRENT_DIR);

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

    }

    public static void createAllOrSelectPartyDialog(final Activity activity, final String dir, final int optionIndex) {
        //Let the user choose the parties
        final List<Party> parties = Services.getInstance(activity).getParties();

        CharSequence[] options = activity.getResources().getStringArray(R.array.options_export_print);
        AlertDialog chooseDialog = new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.str_choose))
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: //All parties
                            //noinspection unchecked
                            new ExportPartiesReportAsync(
                                    activity,
                                    dir,
                                    ExportPartiesReportAsync.Type.values()[optionIndex]
                            ).execute(parties);
                            break;

                        case 1: //Select parties
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
