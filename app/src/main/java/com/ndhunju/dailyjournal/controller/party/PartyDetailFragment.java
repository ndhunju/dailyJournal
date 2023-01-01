package com.ndhunju.dailyjournal.controller.party;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.ItemDescriptionAdapter;
import com.ndhunju.dailyjournal.database.PartyDAO;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.util.List;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link PartyListActivity}
 * in two-pane mode (on tablets) or a {@link PartyDetailActivity}
 * on handsets.
 */
public abstract class PartyDetailFragment extends Fragment implements PartyDAO.Observer, LedgerAdapter.OnItemClickListener {

    public static final String TAG = PartyDetailFragment.class.getName();
    //Variables
    private Party mParty;
    private Services mServices;

    //View Variables
    private TextView nameTV;
    private ImageView picIV;
    private TextView balanceTV;
    private RecyclerView ledgerListView;
    private LedgerAdapter ledgerAdapter;
    private FloatingActionButton newJournalFab;

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
        ViewGroup rootView = (ViewGroup) inflater.inflate(getLayoutId(), container, false);

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
        ledgerListView.setItemAnimator(UtilsView.getDefaultItemAnimator());

        newJournalFab = (FloatingActionButton) getActivity().findViewById(R.id.activity_party_detail_fab);
        /** newJournalFab is null when this fragment is hosted by {@link PartyListActivity} */
        if (newJournalFab != null) {
            ledgerListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy > 0 && newJournalFab.getVisibility() == View.VISIBLE) {
                        // scrolled down, hide fab
                        newJournalFab.hide();
                    } else if (dy < 0 && newJournalFab.getVisibility() == View.GONE) {
                        // scrolled up, show fab
                        newJournalFab.show();
                    }
                }
            });
        }

        balanceTV = (TextView) rootView.findViewById(R.id.fragment_party_detail_balance_tv);

        balanceTV.setSingleLine();

        setPartyView(rootView);
        setLedgerListView(rootView);
        setFooterView(rootView);

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
        mServices.registerJournalObserver(getLedgerAdapter());

        return rootView;
    }

    @LayoutRes
    protected abstract int getLayoutId();

    protected void setPartyView(ViewGroup root) {

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

        nameTV.setText(mParty.getName());

        balanceTV.setText(UtilsFormat.formatCurrency(mParty.calculateBalances(), getActivity()));
        balanceTV.setTextColor(getResources().getColor(mParty.calculateBalances() < 0 ? R.color.red_medium : R.color.green_medium));

        getActivity().setTitle(mParty.getName());

    }

    public Party getParty() {
        return mParty;
    }

    protected void setLedgerListView(View container) {
    }

    public void setLedgerAdapter(LedgerAdapter ledgerAdapter) {
        this.ledgerAdapter = ledgerAdapter;
        this.ledgerAdapter.setOnItemClickListener(this);
        this.ledgerListView.setAdapter(this.ledgerAdapter);
    }

    public LedgerAdapter getLedgerAdapter() {
        return ledgerAdapter;
    }

    public RecyclerView getLedgerListView() {
        return ledgerListView;
    }

    protected abstract void setFooterView(ViewGroup root);

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_party_activity, menu);
        if (getContext() != null) UtilsView.setMenuIconTint(menu, ContextCompat.getColor(getContext(), R.color.icon_tint_menu));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_party_activity_info:
                startPartyActivity();
                break;
            case R.id.menu_party_activity_share:
                showOptionsForAction(Intent.ACTION_SEND);
                break;
            case R.id.menu_party_activity_view_report:
                showOptionsForAction(Intent.ACTION_VIEW);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showOptionsForAction(String action) {
        ItemDescriptionAdapter.Item[] options = ReportGeneratorAsync.getStrTypes(getContext());

        new AlertDialog.Builder(getContext())
                .setAdapter(
                        new ItemDescriptionAdapter(getContext(), options),
                        (dialogInterface, optionIndex) -> {
                            dialogInterface.dismiss();
                            new ReportGeneratorAsync(
                                    getActivity(),
                                    ReportGeneratorAsync.Type.values()[optionIndex],
                                    action
                                    ).execute(mParty.getId());
                        })
                .create()
                .show();
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
            if (v == null) return;
            v.setSingleLine();
            v.setEllipsize(truncateAt);
        }
    }

    public static void addDrawables(Context con, View... view) {
        for (View v : view) {
            if (v == null) return;
            v.setBackground(ContextCompat.getDrawable(con, R.drawable.cell_header_shape));
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
            setPartyView((ViewGroup) getView());
            setFooterView((ViewGroup) getView());
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
