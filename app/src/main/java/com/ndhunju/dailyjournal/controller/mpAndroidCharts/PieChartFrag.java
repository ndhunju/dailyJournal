package com.ndhunju.dailyjournal.controller.mpAndroidCharts;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.charts.PieChart;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.data.Entry;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.data.PieData;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.data.PieDataSet;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.utils.ColorTemplate;
import com.ndhunju.dailyjournal.service.Analytics;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.util.ArrayList;

public class PieChartFrag extends ChartFragment {

    private static final String ARG_CHART_TYPE = "type";
    public static final int LIMIT_ITEM_CHART = 5;

    public static Fragment newInstance(int type) {
        Fragment pieChartFrag = new PieChartFrag();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CHART_TYPE, type );
        pieChartFrag.setArguments(args);
        return pieChartFrag;
    }

    private PieChart mChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_simple_pie, container, false);

        int type = getArguments().getInt(ARG_CHART_TYPE);

        mChart = (PieChart) v.findViewById(R.id.pieChart1);

        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf");
        mChart.setTransparentCircleRadius(50f);
        mChart.setCenterTextTypeface(tf);
        mChart.setCenterTextSize(10f);
        mChart.setHoleRadius(45f);
        mChart.setDescription("");

        StringBuilder dscrptn = new StringBuilder(UtilsFormat.getPartyFromPref(getActivity()));
        dscrptn.append(" ");
        dscrptn.append(type == Analytics.TOP_POS_BAL ? getString(R.string.chart_dscrptn_owe_you) :
                getString(R.string.chart_dscrptn_you_owe));


        Analytics analytics = Analytics.from(getActivity());
        Analytics.PartyData partyData = analytics.getTopPartiesByBalance(type, LIMIT_ITEM_CHART);

        if(partyData == null){ mChart.setNoDataText(getString(R.string.str_no) + " " + getString(R.string.str_data));}
        else{
            mChart.setCenterText(generateCenterText(getString(R.string.str_top) + " " +
                    partyData.balances.length , dscrptn.toString()));
            mChart.setData(generatePieData(partyData.names, partyData.balances, partyData.balanceSum));
        }

        //animate teh chart
        mChart.animateXY(1500, 1500);

        return v;
    }

    private SpannableString generateCenterText(String main, String sub) {
        SpannableString s = new SpannableString(main +"\n"+ sub);
        s.setSpan(new RelativeSizeSpan(2f), 0, main.length(), 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), main.length(), s.length(), 0);
        return s;
    }

    /**
     * @return
     */
    protected PieData generatePieData(String[] xVals, double[] yVals , double yValsSum) {


        ArrayList<Entry> entries1 = new ArrayList<Entry>();


        for(int i = 0; i < yVals.length; i++) {
            //xVals["entry" + (i+1)];
            if(yVals[i] == 0) continue; //if value is 0 ignore it
            entries1.add(new Entry((float)(yVals[i]/yValsSum)*100, i));
        }

        PieDataSet ds1 = new PieDataSet(entries1, "");
        ds1.setColors(ColorTemplate.PASTEL_COLORS);
        ds1.setSliceSpace(2f);
        ds1.setValueTextColor(Color.WHITE);
        ds1.setValueTextSize(12f);

        PieData d = new PieData(xVals, ds1);
        d.setValueTypeface(tf);

        return d;
    }

}
