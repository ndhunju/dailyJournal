package com.ndhunju.dailyjournal.controller.mpAndroidCharts;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ndhunju.dailyjournal.controller.mpAndroidCharts.data.BarData;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.data.BarDataSet;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.data.BarEntry;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.data.ChartData;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.data.LineData;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.data.LineDataSet;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.utils.ColorTemplate;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.utils.FileUtils;

import java.util.ArrayList;

public abstract class ChartFragment extends Fragment {

    protected Typeface tf;

    public ChartFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        tf = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected BarData generateBarData(int dataSets, float range, int count) {

        ArrayList<BarDataSet> sets = new ArrayList<BarDataSet>();

        for(int i = 0; i < dataSets; i++) {

            ArrayList<BarEntry> entries = new ArrayList<BarEntry>();

//            entries = FileUtils.loadEntriesFromAssets(getActivity().getAssets(), "stacked_bars.txt");

            for(int j = 0; j < count; j++) {
                entries.add(new BarEntry((float) (Math.random() * range) + range / 4, j));
            }

            BarDataSet ds = new BarDataSet(entries, getLabel(i));
            ds.setColors(ColorTemplate.VORDIPLOM_COLORS);
            sets.add(ds);
        }

        BarData d = new BarData(ChartData.generateXVals(0, count), sets);
        d.setValueTypeface(tf);
        return d;
    }



    protected LineData generateLineData() {

//        DataSet ds1 = new DataSet(n, "O(n)");
//        DataSet ds2 = new DataSet(nlogn, "O(nlogn)");
//        DataSet ds3 = new DataSet(nsquare, "O(n\u00B2)");
//        DataSet ds4 = new DataSet(nthree, "O(n\u00B3)");

        ArrayList<LineDataSet> sets = new ArrayList<LineDataSet>();

        LineDataSet ds1 = new LineDataSet(FileUtils.loadEntriesFromAssets(getActivity().getAssets(), "sine.txt"), "Sine function");
        LineDataSet ds2 = new LineDataSet(FileUtils.loadEntriesFromAssets(getActivity().getAssets(), "cosine.txt"), "Cosine function");

        ds1.setLineWidth(2f);
        ds2.setLineWidth(2f);

        ds1.setDrawCircles(false);
        ds2.setDrawCircles(false);

        ds1.setColor(ColorTemplate.VORDIPLOM_COLORS[0]);
        ds2.setColor(ColorTemplate.VORDIPLOM_COLORS[1]);

        // load DataSets from textfiles in assets folders
        sets.add(ds1);
        sets.add(ds2);

//        sets.add(FileUtils.dataSetFromAssets(getActivity().getAssets(), "n.txt"));
//        sets.add(FileUtils.dataSetFromAssets(getActivity().getAssets(), "nlogn.txt"));
//        sets.add(FileUtils.dataSetFromAssets(getActivity().getAssets(), "square.txt"));
//        sets.add(FileUtils.dataSetFromAssets(getActivity().getAssets(), "three.txt"));

        int max = Math.max(sets.get(0).getEntryCount(), sets.get(1).getEntryCount());

        LineData d = new LineData(ChartData.generateXVals(0, max),  sets);
        d.setValueTypeface(tf);
        return d;
    }

    protected LineData getComplexity() {

        ArrayList<LineDataSet> sets = new ArrayList<LineDataSet>();

        LineDataSet ds1 = new LineDataSet(FileUtils.loadEntriesFromAssets(getActivity().getAssets(), "n.txt"), "O(n)");
        LineDataSet ds2 = new LineDataSet(FileUtils.loadEntriesFromAssets(getActivity().getAssets(), "nlogn.txt"), "O(nlogn)");
        LineDataSet ds3 = new LineDataSet(FileUtils.loadEntriesFromAssets(getActivity().getAssets(), "square.txt"), "O(n\u00B2)");
        LineDataSet ds4 = new LineDataSet(FileUtils.loadEntriesFromAssets(getActivity().getAssets(), "three.txt"), "O(n\u00B3)");

        ds1.setColor(ColorTemplate.VORDIPLOM_COLORS[0]);
        ds2.setColor(ColorTemplate.VORDIPLOM_COLORS[1]);
        ds3.setColor(ColorTemplate.VORDIPLOM_COLORS[2]);
        ds4.setColor(ColorTemplate.VORDIPLOM_COLORS[3]);

        ds1.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0]);
        ds2.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[1]);
        ds3.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[2]);
        ds4.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[3]);

        ds1.setLineWidth(2.5f);
        ds1.setCircleSize(3f);
        ds2.setLineWidth(2.5f);
        ds2.setCircleSize(3f);
        ds3.setLineWidth(2.5f);
        ds3.setCircleSize(3f);
        ds4.setLineWidth(2.5f);
        ds4.setCircleSize(3f);


        // load DataSets from textfiles in assets folders
        sets.add(ds1);
        sets.add(ds2);
        sets.add(ds3);
        sets.add(ds4);

        LineData d = new LineData(ChartData.generateXVals(0, ds1.getEntryCount()), sets);
        d.setValueTypeface(tf);
        return d;
    }

    private String[] mLabels = new String[] { "Company A", "Company B", "Company C", "Company D", "Company E", "Company F" };
//    private String[] mXVals = new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec" };

    private String getLabel(int i) {
        return mLabels[i];
    }
}
