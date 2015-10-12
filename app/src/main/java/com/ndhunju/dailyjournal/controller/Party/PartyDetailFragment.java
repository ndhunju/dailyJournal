package com.ndhunju.dailyjournal.controller.party;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.journal.JournalActivity;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.ReportGenerator;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link PartyListActivity}
 * in two-pane mode (on tablets) or a {@link PartyDetailActivity}
 * on handsets.
 */
public class PartyDetailFragment extends Fragment {

    //Constants
    private static final int REQUEST_JOURNAL_CHGD = 5457;
    private static final int REQUEST_PARTY_INFO_CHGD = 135;

    //Variables
    private Party mParty;
    private double balance;
    private Services mServices;

    //View Variables
    private View footerView;
    private TextView balanceTV;
    private ListView ledgerListView;
    private LedgerAdapter ledgerAdapter;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PartyDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(Constants.KEY_PARTY_ID)) {
            // Load the content specified by the fragment argument
            long partyId = Long.parseLong(getArguments().getString(Constants.KEY_PARTY_ID));
            mServices = Services.getInstance(getActivity());
            mParty = mServices.getParty(partyId);

            //if party in not found
            if(mParty == null) mParty = new Party(getString(R.string.str_parties));
            balance = mParty.calculateBalances();
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_party_detail, container, false);

        //Wire up the widgets/view
        balanceTV = (TextView) rootView.findViewById(R.id.activity_party_balance_tv);
        ledgerListView = (ListView)rootView.findViewById(R.id.activity_party_ll);

        balanceTV.setText(UtilsFormat.formatCurrency(balance, getActivity()));
        balanceTV.setSingleLine();

        ledgerAdapter = new LedgerAdapter(getActivity(), mServices.getJournals(mParty.getId()));
        ledgerListView.setAdapter(ledgerAdapter);
        ledgerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                if(id>0) createJournalIntent(id);
            }
        });

        ledgerListView.setOnCreateContextMenuListener(this);
        footerView = getFooterRow(mParty, inflater);
        ledgerListView.addFooterView(footerView);
        getActivity().setTitle(mParty.getName());

        return rootView;
    }

    private void createJournalIntent(long id) {
        //Open journal activity to show the detail info of the clicked Journal
        Intent intent = new Intent(getActivity(), JournalActivity.class);
        intent.putExtra(Constants.KEY_JOURNAL_ID, id);
        intent.putExtra(Constants.KEY_PARTY_ID, mParty.getId());
        startActivityForResult(intent, REQUEST_JOURNAL_CHGD);
    }

    //Add Totals in the footer row
    private TableRow getFooterRow(Party party, LayoutInflater layoutInflater) {
        TableRow footerRow = (TableRow)layoutInflater.inflate(R.layout.ledger_row, null);
        TextView col0 = (TextView) footerRow.findViewById(R.id.ledger_row_col0);
        TextView col1 = (TextView) footerRow.findViewById(R.id.ledger_row_col1);
        TextView col2 = (TextView) footerRow.findViewById(R.id.ledger_row_col2);
        TextView col3 = (TextView) footerRow.findViewById(R.id.ledger_row_col3);
        TextView col4 = (TextView) footerRow.findViewById(R.id.ledger_row_col4);

        col1.setText(getString(R.string.str_total));
        col2.setText("");
        col3.setText(UtilsFormat.formatDecimal(party.getDebitTotal(), getActivity()));
        col4.setText(UtilsFormat.formatDecimal(party.getCreditTotal(), getActivity()));
        addDrawables(getActivity(), col0, col1, col2, col3, col4);
        //add common attributes
        addAttributes(TextUtils.TruncateAt.MARQUEE, col3, col4);
        return footerRow;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        switch (requestCode) {
            case REQUEST_JOURNAL_CHGD:

                if(!data.getBooleanExtra(Constants.KEY_JOURNAL_CHGD, false))
                    return;

                double balance = mParty.calculateBalances();
                balanceTV.setText(UtilsFormat.formatCurrency(balance, getActivity()));

                mParty = mServices.getParty(mParty.getId());
                ledgerAdapter = new LedgerAdapter(getActivity(), mServices.getJournals(mParty.getId()));
                ledgerListView.setAdapter(ledgerAdapter);
                ledgerListView.removeFooterView(footerView);
                ledgerListView.addFooterView(footerView= getFooterRow(mParty, getActivity().getLayoutInflater()));
                balanceTV.setText(UtilsFormat.formatCurrency(mParty.calculateBalances(), getActivity()));

                //Alert the user if balance is negative
                break;

            case REQUEST_PARTY_INFO_CHGD:

                if(!data.getBooleanExtra(Constants.KEY_PARTY_INFO_CHGD, false))
                    return;

                Constants.ChangeType type = (Constants.ChangeType) data.getSerializableExtra(Constants.KEY_CHANGE_TYPE);

                Intent intent = new Intent();
                intent.putExtra(Constants.KEY_PARTY_INFO_CHGD, true);

                boolean twoPane = false;
                //If {@link PartyDetailFragment} in embedded in {@link PartyListActivity} notify
                //{@link PartyListFragment} about the change
                if(twoPane = getActivity() instanceof PartyListActivity){
                    ((PartyListFragment)getActivity().getFragmentManager()
                            .findFragmentById(R.id.item_list))
                            .refreshList();
                }

                switch (type){
                    case EDITED:
                        //Party information was changed, update the title
                        getActivity().setTitle(data.getStringExtra(Constants.KEY_PARTY_NAME));
                        intent.putExtra(Constants.KEY_CHANGE_TYPE, Constants.ChangeType.EDITED);
                        getActivity().setResult(Activity.RESULT_OK, intent); //relay it to the parent activity
                        break;
                    case DELETED:
                        if(twoPane) return;
                        intent.putExtra(Constants.KEY_CHANGE_TYPE, Constants.ChangeType.DELETED);
                        getActivity().setResult(Activity.RESULT_OK, intent);
                        getActivity().finish();
                        break;
                }

                break;
        }


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_party_activity, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.menu_party_activity_info:
                //Create intent to pass current mParty id to PartyActivity
                Intent i = new Intent(getActivity(),PartyActivity.class);
                i.putExtra(Constants.KEY_PARTY_ID, mParty.getId());
                startActivityForResult(i, REQUEST_PARTY_INFO_CHGD);
                break;

            case R.id.menu_party_activity_share:
                String[] options = ReportGeneratorAsync.getStrTypes();
                new AlertDialog.Builder(getActivity()).setItems(options,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new ReportGeneratorAsync(getActivity(), ReportGeneratorAsync.Type.values()[i])
                                        .execute(mParty.getId());
                            }
                        })
                        .create().show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //prepare context menu
        MenuItem itemEdit = menu.add(0, PartyListActivity.CONTEXT_MENU_EDIT, 1, getString(R.string.str_edit));
        itemEdit.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));

        MenuItem itemDelete = menu.add(0, PartyListActivity.CONTEXT_MENU_DELETE, 2, getString(R.string.str_delete));
        itemDelete.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_delete));

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();


        //Get the list item position
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        ListView lv = (ListView)info.targetView.getParent();

        final Journal journal;
        Object obj = lv.getAdapter().getItem(info.position);
        //obj could be an instance of Party or Journal
        if(!(obj instanceof Journal)) {
            //return false so that other fragment can handle it
            return false;
        }

        journal = (Journal)obj;

        //get the id of select journal
        long journalId = info.id;

        switch (id){
            case PartyListActivity.CONTEXT_MENU_EDIT:
                createJournalIntent(journalId);
                break;

            case PartyListActivity.CONTEXT_MENU_DELETE:
                String msg = String.format(getString(R.string.msg_delete_confirm), getString(R.string.str_journal));
                //Alert user before deleting the Journal
                UtilsView.alert(getActivity(), msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mServices.deleteJournal(journal);
                        //remove same journal from the adapter as well rather than reloading
                        ledgerAdapter.remove(journal);
                        ledgerAdapter.notifyDataSetChanged();
                        String msg = String.format(getString(R.string.msg_deleted), getString(R.string.str_journal));
                        UtilsView.toast(getActivity(), msg);
                    }
                }, null);

                break;
        }
        return true;
    }

    public static void addAttributes(TextUtils.TruncateAt truncateAt, TextView... view){
        for(TextView v : view){
            v.setSingleLine();
            v.setEllipsize(truncateAt);
        }
    }

    private static void addDrawables(Context con, View... view){
        for(View v : view){
            v.setBackgroundDrawable(con.getResources().getDrawable(R.drawable.heading_shape));
        }
    }
}
