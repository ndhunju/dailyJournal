package com.ndhunju.dailyjournal.controller.mpAndroidCharts.interfaces;

import com.github.mikephil.charting.data.ScatterData;

public interface ScatterDataProvider extends BarLineScatterCandleBubbleDataProvider {

    ScatterData getScatterData();
}
