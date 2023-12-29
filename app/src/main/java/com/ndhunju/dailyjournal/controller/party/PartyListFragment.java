package com.ndhunju.dailyjournal.controller.party;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.ItemDescriptionAdapter;
import com.ndhunju.dailyjournal.controller.ads.AdsLayout;
import com.ndhunju.dailyjournal.controller.backup.SharePartiesReportAsync;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.AdManager;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.ImportContacts;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.util.ArrayList;
import java.util.List;

/**
 * A list fragment representing a list of Items. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link PartyDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class PartyListFragment extends Fragment implements PartyCardAdapter.OnItemClickListener{

    // The serialization (saved instance state) Bundle key representing the
    // activated item position. Only used on tablets.
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final int INVALID_POSITION = -1;
    private static final int REQUEST_PERMISSIONS_READ_CONTACTS = 2323;
    public static final int REQUEST_PARTY_INFO_CHGD = 135;

    // The fragment's current callback object, which is notified of list item clicks.
    private Callbacks mCallbacks = sDummyCallbacks;
    private Runnable runOnResume;

    //List view
    private RecyclerView mPartyLV;
    private Services mServices;
    private EditText srchOrAddPartyET;
    private PartyCardAdapter mPartyAdapter;

    // The current activated item position. Only used on tablets
    private int mActivatedPosition = INVALID_POSITION;

    @Override
    public void onItemClick(View view, int position, long id) {
        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(String.valueOf(id), view, position);
    }

    @Override
    public void onContextItemClick(View view, final int position, long id) {
        onContextItemClick(getActivity(), mPartyAdapter, view, position, id);
    }

    public static void onContextItemClick(final FragmentActivity activity, final PartyCardAdapter partyAdapter, View view, final int position, long id) {
        switch ((int) id){
            case android.R.drawable.ic_menu_edit:
                // selected to edit the party
                Intent partyEditIntent = new Intent(activity, PartyActivity.class);
                partyEditIntent.putExtra(Constants.KEY_PARTY_ID, partyAdapter.getItem(position).getId());
                partyEditIntent.putExtra(Constants.KEY_POS, position);
                activity.startActivityForResult(partyEditIntent, REQUEST_PARTY_INFO_CHGD);
                break;
            case android.R.drawable.ic_menu_delete:
                // selected to delete the Party
                String msg = String.format(activity.getString(R.string.msg_delete_confirm), activity.getString(R.string.str_party));
                //Alert user before deleting the Journal
                UtilsView.alert(activity, msg, (dialogInterface, i) -> {
                    Services.getInstance(activity).deleteParty(partyAdapter.getItem(position));
                    String msg1 = String.format(activity.getString(R.string.msg_deleted), partyAdapter.getItem(position).getName());
                    UtilsView.toast(activity, msg1);
                }, null);

                break;
        }

    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        void onItemSelected(String id, View view, int position);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = (id, view, position) -> {};

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PartyListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServices = Services.getInstance(getActivity());
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_party_list, container, false);

        //Wire up widgets
        srchOrAddPartyET = (EditText)rootView.findViewById(R.id.fragment_party_list_search_et);
        srchOrAddPartyET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //filter the list below
                mPartyAdapter.filter(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mPartyLV = (RecyclerView) rootView.findViewById(R.id.fragment_party_list_party_list);
        mPartyLV.setLayoutManager(new LinearLayoutManager(getContext()));
        mPartyLV.setOnCreateContextMenuListener(this);

        mPartyAdapter = new PartyCardAdapter(getActivity(), mServices.getParties());
        mPartyLV.setAdapter(mPartyAdapter);
        mPartyAdapter.setOnItemClickListener(this);
        mPartyAdapter.setActivatedItemPos(0);

        //When user clicks on Add Party button, create a Party and pass the ID to previous activity
        ((Button)rootView.findViewById(R.id.fragment_party_list_add_party_btn))
                .setOnClickListener(v -> {
                    Editable editable = srchOrAddPartyET.getText();
                    if (editable == null || editable.toString().isEmpty()) {
                        srchOrAddPartyET.setError(getString(
                                R.string.party_name_empty_error
                        ));
                        return;
                    }
                    String name = srchOrAddPartyET.getText().toString();
                    Party newParty = mServices.addParty(name);
                    UtilsView.toast(getActivity(), name + " saved.");
                    mPartyAdapter.dataSetChanged();
                    mPartyAdapter.filter(name);
                });

        FloatingActionButton newPartyFAB = (FloatingActionButton)rootView.findViewById(R.id.fragment_party_list_fab);
        newPartyFAB.setOnClickListener(v -> {
            Intent newPartyIntent = new Intent(getActivity(), PartyActivity.class);
            startActivityForResult(newPartyIntent, REQUEST_PARTY_INFO_CHGD);
        });

        mServices.registerPartyObserver(mPartyAdapter);

        return rootView;
    }

    private RecyclerView getListView(){
        return mPartyLV;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }

        AdsLayout adsLayout = view.findViewById(R.id.fragment_party_list_ads_layout);
        AdManager.INSTANCE.loadAdIfAllowed(
                adsLayout,
                getString(R.string.admob_party_list_ad_unit_id),
                "PartyListScreen"
        );
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Activities containing this fragment must implement its callbacks.
        if (!(context instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (runOnResume != null) {
            runOnResume.run();
            runOnResume = null;
        }
    }

    private void setActivatedPosition(int position) {
        mPartyAdapter.setActivatedItemPos(position);
        mActivatedPosition = position;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_party_list_activity, menu);
        UtilsView.setMenuIconTint(menu, ContextCompat.getColor(getContext(), R.color.icon_tint_menu));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.menu_party_list_activity_import:
                AnalyticsService.INSTANCE.logEvent("didClickOnImportContacts");
                startImportingContacts();
                break;

            case R.id.menu_party_list_activity_share:
                AnalyticsService.INSTANCE.logEvent("didClickOnShareReportInPartyList");
                // let the user choose the type of report she wants to share
                createDialogForSharePartiesReport(getActivity()).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // the system may have displayed a permission dialog (in Marshmallow), causing the fragment to go into onPause() state
                if (this.isResumed()) {
                    startImportingContacts();
                } else {
                    runOnResume = this::startImportingContacts;
                }
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // user denied permissions to access contacts
                // do nothing
                AnalyticsService.INSTANCE.logEvent("didDenyPermissionToReadContacts");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startImportingContacts() {

        if (getActivity() == null) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(
                getActivity(),
                Manifest.permission.READ_CONTACTS
        ) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[] {Manifest.permission.READ_CONTACTS},
                    REQUEST_PERMISSIONS_READ_CONTACTS
            );
            return;
        }

        final List<ImportContacts.Contact> contacts = ImportContacts.getContacts(getActivity());

        ImportContactsView importContactsView = new ImportContactsView(getContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.msg_choose, getString(R.string.str_contact)));
        builder.setNegativeButton(getString(android.R.string.cancel), null);
        builder.setView(importContactsView);
        builder.setPositiveButton(
                getString(android.R.string.ok),
                (dialogInterface, i) -> new ImportContactsAsync(getActivity()).execute(
                        importContactsView.getSelectedContacts()
                )
        );

        AlertDialog selectContactsAD = builder.create();
        importContactsView.setContacts(contacts);
        selectContactsAD.show();
    }

    public static AlertDialog createDialogForSharePartiesReport(final Activity activity) {
        // let the user choose the type of printable she wants to export
        ItemDescriptionAdapter.Item[] options = SharePartiesReportAsync.getStrTypes(activity);

        return new AlertDialog.Builder(activity)
                .setAdapter(
                        new ItemDescriptionAdapter(activity, options),
                        (dialogInterface, optionIndex) -> {
                            shareAllOrSelectPartyDialog(
                                    activity,
                                    optionIndex);
                        })
                .create();


    }

    public static void shareAllOrSelectPartyDialog(final Activity activity, final int optionIndex) {
        // let the user choose the parties
        final List<Party> parties = Services.getInstance(activity).getParties();

        CharSequence[] options = activity.getResources().getStringArray(R.array.options_export_print);
        AlertDialog chooseDialog = new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.str_choose))
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: //All parties
                                //noinspection unchecked
                                new SharePartiesReportAsync(activity, SharePartiesReportAsync.Type.values()[optionIndex]).execute(parties);
                                break;

                            case 1: //Select parties
                                createPartySelectDialogToShare(activity, parties, optionIndex).show();
                                break;

                        }
                    }
                }).create();

        chooseDialog.show();
    }

    public static AlertDialog createPartySelectDialogToShare(final Activity activity, final List<Party> parties, final int optionIndex) {
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
                    // Add checked contacts into selectedParties list
                    if (b) selectedParties.add(parties.get(i));
                    else selectedParties.remove(parties.get(i));
                });
        builder.setPositiveButton(activity.getString(android.R.string.ok),
                (dialogInterface, i) -> new SharePartiesReportAsync(activity, SharePartiesReportAsync.Type.values()[optionIndex]).execute(selectedParties));

        return builder.create();
    }

    public RecyclerView.Adapter getArrayAdapter(){
        return mPartyAdapter;
    }

    @Override
    public void onDestroy() {
        mServices.unregisterPartyObserver(mPartyAdapter);
        super.onDestroy();
    }
}
