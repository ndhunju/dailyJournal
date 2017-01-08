package com.ndhunju.dailyjournal.controller.party;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.database.PartyDAO;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.util.List;
import java.util.Locale;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link PartyListActivity}
 * in two-pane mode (on tablets) or a {@link PartyDetailActivity}
 * on handsets.
 */
public class PartyDetailFragment extends Fragment implements PartyDAO.Observer, LedgerAdapter.OnItemClickListener {

    public static final String TAG = PartyDetailFragment.class.getName();
    //Variables
    private Party mParty;
    private Services mServices;

    //View Variables
    private View footerView;
    private TextView nameTV;
    private ImageView picIV;
    private TableRow headerRow;
    private TextView balanceTV;
    private RecyclerView ledgerListView;
    private ViewGroup footerViewContainer;
    private LedgerAdapter ledgerAdapter;



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

        //Wire up the widgets view
        rootView.findViewById(R.id.fragment_party_detail_party_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPartyActivity();
            }
        });
        picIV = (ImageView) rootView.findViewById(R.id.fragment_party_detail_circle_iv);
        nameTV = (TextView) rootView.findViewById(R.id.fragment_party_detail_name_tv);
        nameTV.setMovementMethod(new ScrollingMovementMethod());

        ledgerListView = (RecyclerView) rootView.findViewById(R.id.activity_party_lv);
        ledgerListView.setLayoutManager(new LinearLayoutManager(getContext()));
        ledgerListView.getItemAnimator().setSupportsChangeAnimations(false);

        balanceTV = (TextView) rootView.findViewById(R.id.fragment_party_detail_balance_tv);
        headerRow = (TableRow)rootView.findViewById(R.id.fragment_party_detail_header_tr);
        footerViewContainer = (ViewGroup) rootView.findViewById(R.id.fragment_party_detail_footer);

        ((TextView)rootView.findViewById(R.id.activity_party_col_header_dr))
                .setText(getString(R.string.str_dr));

        ((TextView)rootView.findViewById(R.id.activity_party_col_header_cr))
                .setText(getString(R.string.str_cr));


        balanceTV.setSingleLine();

        setPartyViews(mParty);
        setLedgerListViews();
        setFooterView(mParty);

        //Animate the list view but wait until the view is laid out
        ledgerListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (ViewCompat.isLaidOut(ledgerListView)) {
                    DisplayMetrics metrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    ledgerListView.setTranslationY(metrics.heightPixels);
                    AnticipateInterpolator interpolator = new AnticipateInterpolator();
                    ledgerListView.animate().setInterpolator(interpolator)
                            .setDuration(500)
                            .setStartDelay(2)
                            .translationYBy(-metrics.heightPixels)
                            .start();
                    // remove listener
                    ledgerListView.removeOnLayoutChangeListener(this);
                }
            }
        });

        mServices.registerPartyObserver(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setPartyViews(Party party){

        //make the image circular
        //make the image circular
        if (!TextUtils.isEmpty(mParty.getPicturePath())) {
            RoundedBitmapDrawable bitmapDrawable = RoundedBitmapDrawableFactory.create(
                    getResources(),
                    mParty.getPicturePath());

            bitmapDrawable.setCircular(true);
            picIV.setImageDrawable(bitmapDrawable);
        } else {
            picIV.setImageResource(R.drawable.default_party_pic);
        }

        nameTV.setText(party.getName());

        balanceTV.setText(UtilsFormat.formatCurrency(party.calculateBalances(), getActivity()));
        balanceTV.setTextColor(getResources().getColor(party.calculateBalances() < 0 ? R.color.red_medium : R.color.green_medium));

        getActivity().setTitle(mParty.getName());

    }

    private void setLedgerListViews() {

        PreferenceService ps  = PreferenceService.from(getActivity());
        int pos = ps.getVal(R.string.key_pref_ledger_view, 0);

        switch (pos){

            case 0: //Card View
                ledgerAdapter = new LedgerCardAdapter(getActivity(), mParty);
                headerRow.setVisibility(View.GONE);
                break;
            case 1: //Classic View

                ledgerAdapter = new LedgerRowAdapter(getActivity(), mParty);
                headerRow.setVisibility(View.VISIBLE);
                break;
        }

        ledgerListView.setAdapter(ledgerAdapter);
        ledgerAdapter.setOnItemClickListener(this);
        mServices.registerJournalObserver(ledgerAdapter);

    }

    private void setFooterView(Party party){


        //remove the old view
        if(footerView != null) footerViewContainer.removeView(footerView);

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

        footerViewContainer.addView(footerView);
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
        ViewGroup footerRow = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.ledger_card_footer, null);
        TextView totalCr = (TextView) footerRow.findViewById(R.id.ledger_card_footer_cr_total);
        totalCr.setTextColor(ContextCompat.getColor(getContext(), R.color.red_medium));
        TextView totalDr = (TextView) footerRow.findViewById(R.id.ledger_card_footer_dr_total);
        totalDr.setTextColor(ContextCompat.getColor(getContext(), R.color.green_medium));

        totalCr.setText(String.format(Locale.getDefault(), "%1$s%2$s", UtilsFormat.formatCurrency(party.getCreditTotal(), getActivity()), getString(R.string.str_cr)));
        totalDr.setText(String.format(Locale.getDefault(), "%1$s%2$s", UtilsFormat.formatCurrency(party.getDebitTotal(), getActivity()), getString(R.string.str_dr)));
        addAttributes(TextUtils.TruncateAt.MARQUEE, totalCr, totalDr);
        return footerRow;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_party_activity, menu);
        UtilsView.setMenuIconTint(menu, ContextCompat.getColor(getContext(), R.color.icon_tint_menu));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_party_activity_info:
                startPartyActivity();
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

    private void startPartyActivity() {
        //Create intent to pass current mParty id to PartyActivity
        Intent i = new Intent(getActivity(), PartyActivity.class);
        i.putExtra(Constants.KEY_PARTY_ID, mParty.getId());
        i.putExtra(Constants.KEY_POS, getArguments().getInt(Constants.KEY_POS));
        startActivity(i);
    }

    public static void addAttributes(TextUtils.TruncateAt truncateAt, TextView... view) {
        for (TextView v : view) {
            v.setSingleLine();
            v.setEllipsize(truncateAt);
        }
    }

    private static void addDrawables(Context con, View... view) {
        for (View v : view) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                v.setBackground(ContextCompat.getDrawable(con, R.drawable.cell_header_shape));
            } else {
                v.setBackgroundDrawable(ContextCompat.getDrawable(con, R.drawable.cell_header_shape));
            }
        }
    }

    @Override
    public void onPartyAdded(Party party) {
        // this event is irrelevant in this context.
    }

    @Override
    public void onPartyChanged(Party party) {
        // make sure it's the correct party
        if (party.getId() == mParty.getId()) {
            mParty = party;
            setPartyViews(mParty);
            setFooterView(mParty);
        }
    }

    @Override
    public void onPartyDeleted(Party party) {
        if (party.getId() == mParty.getId()) {
            if (getActivity() instanceof PartyListActivity)
                /** host activity is PartyListActivity. Just remove this fragment leaving {@link PartyListFragment} visible */
                getFragmentManager().beginTransaction().remove(this).commit();
            else if (getActivity() instanceof PartyDetailActivity)
                /** host activity is PartyDetailActivity. Finish this activity to show previous activity  */
                getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        mServices.unregisterPartyObserver(this);
        mServices.unregisterJournalObserver(ledgerAdapter);
        super.onDestroyView();
    }

    @Override
    public void onItemClick(View view, int position, List<Journal> journalList) {
        // copy id of journals to an array
        long[] ids = new long[journalList.size()];
        for (int i = 0; i < journalList.size(); i++)
            ids[i] = journalList.get(i).getId();

        LedgerAdapter.createJournalIntent(getActivity(), ids, position);
    }

    @Override
    public void onContextItemClick(View view, int position, long menuId) {
        LedgerAdapter.onContextItemClick(getActivity(), ledgerAdapter, view, position, menuId);
    }

}
