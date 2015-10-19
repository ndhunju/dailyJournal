package com.ndhunju.dailyjournal.controller.mpAndroidCharts.interfaces;

import com.github.mikephil.charting.data.BubbleData;

public interface BubbleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    BubbleData getBubbleData();
}
