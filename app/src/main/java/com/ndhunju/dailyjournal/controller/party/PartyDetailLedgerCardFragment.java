package com.ndhunju.dailyjournal.controller.party;

import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.util.Locale;

/** @see PartyDetailFragment */
public class PartyDetailLedgerCardFragment extends PartyDetailFragment {

    public static final String TAG = PartyDetailLedgerCardFragment.class.getName();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_party_detail_ledger_card;
    }

    @Override
    protected void setLedgerListView(View container) {
        super.setLedgerListView(container);
        setLedgerAdapter(new LedgerCardAdapter(getActivity(), getParty()));
    }

    protected void setFooterView(ViewGroup root) {
        ViewGroup footerRow = (ViewGroup) root.findViewById(R.id.ledger_card_footer);
        TextView totalCr = (TextView) footerRow.findViewById(R.id.ledger_card_footer_cr_total);
        totalCr.setTextColor(ContextCompat.getColor(getContext(), R.color.red_medium));
        TextView totalDr = (TextView) footerRow.findViewById(R.id.ledger_card_footer_dr_total);
        totalDr.setTextColor(ContextCompat.getColor(getContext(), R.color.green_medium));
        TextView totalTV = (TextView) footerRow.findViewById(R.id.ledger_card_footer_total);

        totalCr.setText(String.format(Locale.getDefault(), "%1$s%2$s", UtilsFormat.formatCurrency(getParty().getCreditTotal(), getActivity()), getString(R.string.str_cr)));
        totalDr.setText(String.format(Locale.getDefault(), "%1$s%2$s", UtilsFormat.formatCurrency(getParty().getDebitTotal(), getActivity()), getString(R.string.str_dr)));
        totalTV.setText(UtilsFormat.formatCurrency(getParty().calculateBalances(), getContext()));

        addAttributes(TextUtils.TruncateAt.MARQUEE, totalCr, totalDr, totalTV);
    }

}
