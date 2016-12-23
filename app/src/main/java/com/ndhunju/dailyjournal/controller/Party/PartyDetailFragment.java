package com.ndhunju.dailyjournal.controller.party;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.database.PartyDAO;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.util.ArrayList;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link PartyListActivity}
 * in two-pane mode (on tablets) or a {@link PartyDetailActivity}
 * on handsets.
 */
public class PartyDetailFragment extends Fragment {

    //Constants
    private static final int REQUEST_JOURNAL__CHGD = 156;
    private static final int REQUEST_PARTY_INFO_CHGD = 135;

    //Variables
    private Party mParty;
    private Services mServices;

    //View Variables
    private View footerView;
    private TextView nameTV;
    private ImageView picIV;
    private TableRow headerRow;
    private TextView balanceTV;
    private ListView ledgerListView;
    private ArrayAdapter ledgerAdapter;



    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PartyDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(Constants.KEY_PARTY_ID)) {
            // Load the content specified by the fragment argument
            long partyId = 0;

            //don't remember why i choose string instead of long
            try{partyId = Long.parseLong(getArguments().getString(Constants.KEY_PARTY_ID));}
            catch (NumberFormatException ex){ex.printStackTrace();}

            mServices = Services.getInstance(getActivity());
            mParty = mServices.getParty(partyId);

            //if party in not found
            if (mParty == null) mParty = new Party(getString(R.string.str_party));
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_party_detail, container, false);

        //Wire up the widgets/view
        picIV = (ImageView) rootView.findViewById(R.id.fragment_party_detail_circle_iv);
        nameTV = (TextView) rootView.findViewById(R.id.fragment_party_detail_name_tv);
        ledgerListView = (ListView) rootView.findViewById(R.id.activity_party_lv);
        balanceTV = (TextView) rootView.findViewById(R.id.fragment_party_detail_balance_tv);
        headerRow = (TableRow)rootView.findViewById(R.id.fragment_party_detail_header_tr);

        ((TextView)rootView.findViewById(R.id.activity_party_col_header_dr))
                .setText(getString(R.string.str_dr));

        ((TextView)rootView.findViewById(R.id.activity_party_col_header_cr))
                .setText(getString(R.string.str_cr));


        balanceTV.setSingleLine();

        setPartyViews(mParty);
        setLedgerListViews(mServices.getJournals(mParty.getId()));
        setFooterView(mParty);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Animate the list view
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ledgerListView.setTranslationY(metrics.heightPixels);
        AnticipateInterpolator interpolator = new AnticipateInterpolator();
        ledgerListView.animate().setInterpolator(interpolator)
                .setDuration(1000)
                .setStartDelay(5)
                .translationYBy(-metrics.heightPixels)
                .start();
        //getView().anim
    }

    private void setPartyViews(Party party){

        //make the image circular
        RoundedBitmapDrawable bitmapDrawable = mParty.getPicturePath().equals("")?
                RoundedBitmapDrawableFactory.create(getResources(),
                        BitmapFactory.decodeResource(getResources(), R.drawable.party_default_pic))
                : RoundedBitmapDrawableFactory.create(getResources(),
                mParty.getPicturePath());

        bitmapDrawable.setCircular(true);
        picIV.setImageDrawable(bitmapDrawable);

        nameTV.setText(party.getName());

        balanceTV.setText(UtilsFormat.formatCurrency(party.calculateBalances(), getActivity()));
        balanceTV.setTextColor(getResources().getColor(party.calculateBalances() < 0 ? R.color.red_medium : R.color.green_medium));

        getActivity().setTitle(mParty.getName());
    }

    private void setLedgerListViews(ArrayList<Journal> journals) {

        PreferenceService ps  = PreferenceService.from(getActivity());
        int pos = ps.getVal(R.string.key_pref_ledger_view, 0);

        switch (pos){

            case 0: //Card View
                ledgerAdapter = new LedgerCardAdapter(getActivity(), journals);
                headerRow.setVisibility(View.GONE);
                break;
            case 1: //Classic View

                ledgerAdapter = new LedgerRowAdapter(getActivity(),journals);
                headerRow.setVisibility(View.VISIBLE);
                break;
        }

        ledgerListView.setAdapter(ledgerAdapter);
        ledgerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                if (id > 0) createJournalIntent(id);
            }
        });

        ledgerListView.setOnCreateContextMenuListener(this);

    }

    private void setFooterView(Party party){

        //remove the old view
        if(footerView != null) ledgerListView.removeFooterView(footerView);

        //getInt the type of view user has selected
        PreferenceService ps  = PreferenceService.from(getActivity());
        int pos = ps.getVal(R.string.key_pref_ledger_view, 0);


        switch (pos){

            case 0: //Card View
                footerView = getFooterCard(party);
                break;
            case 1: //Classic View
                footerView = getFooterRow(party);
                break;
        }

        ledgerListView.addFooterView(footerView);
    }

    private void createJournalIntent(long id) {
        //Open journal activity to show the detail info of the clicked Journal
        Intent intent = new Intent(getActivity(), JournalActivity.class);
        intent.putExtra(Constants.KEY_JOURNAL_ID, id);
        intent.putExtra(Constants.KEY_PARTY_ID, mParty.getId());
        startActivityForResult(intent, REQUEST_JOURNAL__CHGD);
    }

    //Add Totals in the footer row
    private TableRow getFooterRow(Party party) {
        TableRow footerRow = (TableRow) getActivity().getLayoutInflater().inflate(R.layout.ledger_row, null);
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

    private View getFooterCard(Party party) {
        FrameLayout footerRow = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.ledger_card_footer, null);
        TextView totalCr = (TextView) footerRow.findViewById(R.id.ledger_card_footer_cr_total);
        totalCr.setTextColor(getResources().getColor(R.color.red_medium));
        TextView totalDr = (TextView) footerRow.findViewById(R.id.ledger_card_footer_dr_total);
        totalDr.setTextColor(getResources().getColor(R.color.green_medium));

        totalCr.setText(UtilsFormat.formatCurrency(party.getCreditTotal(), getActivity()) + getString(R.string.str_cr));
        totalDr.setText(UtilsFormat.formatCurrency(party.getDebitTotal(), getActivity()) + getString(R.string.str_cr));
        addAttributes(TextUtils.TruncateAt.MARQUEE, totalCr, totalDr);
        return footerRow;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK)
            return;

        boolean twoPane = getActivity() instanceof PartyListActivity;


        switch (requestCode) {

            case REQUEST_PARTY_INFO_CHGD:
            case REQUEST_JOURNAL__CHGD:

                //if party information and journal was not changed, do nothing
                if (!data.getBooleanExtra(Constants.KEY_PARTY_INFO_CHGD, false)
                        && !data.getBooleanExtra(Constants.KEY_JOURNAL_CHGD, false)){
                    return;
                }

                //Party information or journal was changed, getInt the latest
                mParty = mServices.getParty(mParty.getId());
                if(mParty != null) {
                    setPartyViews(mParty);
                    setLedgerListViews(mServices.getJournals(mParty.getId()));
                    setFooterView(mParty);
                }

                //If {@link PartyDetailFragment} in embedded in {@link PartyListActivity} notify
                //{@link PartyListFragment} about the change
                if (twoPane) {
                    ((PartyListFragment) getActivity().getFragmentManager()
                            .findFragmentById(R.id.item_list))
                            .refreshList();

                    //Check if the party was deleted. mParty should be null
                    if (mParty == null){
                        mParty = new Party("");
                        setPartyViews(mParty);
                        setLedgerListViews(new ArrayList<Journal>());
                        setFooterView(mParty);
                    }
                }else {

                    //relay it to the previous activity as well
                    data.putExtra(Constants.KEY_PARTY_INFO_CHGD, true);
                    getActivity().setResult(Activity.RESULT_OK, data);

                    //Check if the party was deleted. mParty should be null
                    if (mParty == null)  getActivity().finish();
                    break;
                }

        }


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_party_activity, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_party_activity_info:
                //Create intent to pass current mParty id to PartyActivity
                Intent i = new Intent(getActivity(), PartyActivity.class);
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
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ListView lv = (ListView) info.targetView.getParent();

        final Journal journal;
        Object obj = lv.getAdapter().getItem(info.position);
        //obj could be an instance of Party or Journal
        if (!(obj instanceof Journal)) {
            //return false so that other fragment can handle it
            return false;
        }

        journal = (Journal) obj;

        //getInt the id of select journal
        long journalId = info.id;

        switch (id) {
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
                        //getInt updates copy of Party and update the views
                        mParty = mServices.getParty(mParty.getId());
                        setPartyViews(mParty);
                        //update the list view as well
                        setFooterView(mParty);

                        String msg = String.format(getString(R.string.msg_deleted), getString(R.string.str_journal));
                        UtilsView.toast(getActivity(), msg);
                    }
                }, null);

                break;
        }
        return true;
    }

    public static void addAttributes(TextUtils.TruncateAt truncateAt, TextView... view) {
        for (TextView v : view) {
            v.setSingleLine();
            v.setEllipsize(truncateAt);
        }
    }

    private static void addDrawables(Context con, View... view) {
        for (View v : view) {
            v.setBackgroundDrawable(con.getResources().getDrawable(R.drawable.cell_header_shape));
        }
    }
}
