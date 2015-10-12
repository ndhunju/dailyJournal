package com.ndhunju.dailyjournal.controller.party;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.ImportContacts;
import com.ndhunju.dailyjournal.service.Services;
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
public class PartyListFragment extends Fragment {

    //The serialization (saved instance state) Bundle key representing the
    //activated item position. Only used on tablets.
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    public static final int REQUEST_PARTY_INFO_CHGD = 135;


//     The fragment's current callback object, which is notified of list item clicks.
    private Callbacks mCallbacks = sDummyCallbacks;

    //List view
    private ListView mPartyLV;
    private Services mServices;
    private EditText srchPartyET;
    private ArrayAdapter<Party> mPartyAdapter;

//      The current activated item position. Only used on tablets
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

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
        View rootView = inflater.inflate(R.layout.fragment_party_list_old, container, false);

        getActivity().setTitle(getString(R.string.str_parties));
        //Wire up widgets
        srchPartyET = (EditText)rootView.findViewById(R.id.fragment_party_list_search_et);
        srchPartyET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //filter the list below
                mPartyAdapter.getFilter().filter(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });



        mPartyLV = (ListView)rootView.findViewById(R.id.fragment_party_list_party_list_lv);
        mPartyLV.setAdapter(mPartyAdapter);
        mPartyLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mCallbacks.onItemSelected(String.valueOf(id));
            }
        });
        mPartyLV.setOnCreateContextMenuListener(this);

        refreshList();

        //When user clicks on Add Party button, create a Party and pass the ID to previous activity
        ((Button)rootView.findViewById(R.id.fragment_party_list_add_party_btn)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String name = srchPartyET.getText().toString();
                Party newParty = mServices.addParty(name);
                UtilsView.toast(getActivity(), name + " saved.");
                mPartyAdapter.add(newParty);
                mPartyAdapter.notifyDataSetChanged();
            }
        });

        return rootView;
    }


    private ListView getListView(){
        return mPartyLV;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
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
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(data == null) return;

        switch (requestCode){
            case REQUEST_PARTY_INFO_CHGD:
                if(!data.getBooleanExtra(Constants.KEY_PARTY_INFO_CHGD, false))
                    return;
                refreshList();
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_party_list_activity, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_party_list_activity_add:
                Intent newPartyIntent = new Intent(getActivity(), PartyActivity.class);
                startActivityForResult(newPartyIntent, REQUEST_PARTY_INFO_CHGD);
                break;

            case R.id.menu_party_list_activity_import:
                final List<ImportContacts.Contact> contacts = ImportContacts.getContacts(getActivity());
                final ArrayList<ImportContacts.Contact> importContacts = new ArrayList<>();

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.msg_choose, getString(R.string.str_contact)));
                builder.setNegativeButton(getString(android.R.string.cancel), null);
                builder.setMultiChoiceItems(ImportContacts.getNames(contacts), null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                //Add checked contacts into importContacts list
                                if (b) importContacts.add(contacts.get(i));
                                else importContacts.remove(contacts.get(i));
                            }
                        });
                builder.setPositiveButton(getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new ImportContactsAsync(getActivity(), new ImportContactsAsync.Callback(){

                                    @Override
                                    public void onFinished() {
                                        refreshList();
                                    }
                                }).execute(importContacts);
                            }
                        });
                AlertDialog selectContactsAD = builder.create();
                selectContactsAD.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    public void refreshList(){
        mPartyAdapter = new PartyAdapter(getActivity(), mServices.getParties());
        mPartyLV.setAdapter(mPartyAdapter);
        mPartyLV.setSelection(0);
    }

    public ArrayAdapter getArrayAdapter(){
        return mPartyAdapter;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //prepare context menu
        MenuItem itemEdit = menu.add(0, PartyListActivity.CONTEXT_MENU_PARTY_EDIT, 1, getString(R.string.str_edit));
        itemEdit.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));

        MenuItem itemDelete = menu.add(0, PartyListActivity.CONTEXT_MENU_PARTY_DELETE, 2, getString(R.string.str_delete));
        itemDelete.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_delete));

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();


        //Get the list item position
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        ListView lv = (ListView)info.targetView.getParent();
        Object obj = lv.getAdapter().getItem(info.position);
        final Party party;

        if(!(obj instanceof Party)) {
            return false;
        }

        party = (Party)obj;

        //get the id of select journal
        long journalId = info.id;

        switch (id){
            case PartyListActivity.CONTEXT_MENU_PARTY_EDIT:
                Intent partyEditIntent = new Intent(getActivity(), PartyActivity.class);
                partyEditIntent.putExtra(Constants.KEY_PARTY_ID, party.getId());
                startActivityForResult(partyEditIntent, REQUEST_PARTY_INFO_CHGD);
                break;
            case PartyListActivity.CONTEXT_MENU_PARTY_DELETE:
                String msg = String.format(getString(R.string.msg_delete_confirm), getString(R.string.str_party));
                //Alert user before deleting the Journal
                UtilsView.alert(getActivity(), msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mServices.deleteParty(party);
                        //remove same journal from the adapter as well rather than reloading
                        mPartyAdapter.remove(party);
                        mPartyAdapter.notifyDataSetChanged();
                        String msg = String.format(getString(R.string.msg_deleted), party.getName());
                        UtilsView.toast(getActivity(), msg);
                    }
                }, null);

                break;
        }
        return true;
    }
}
