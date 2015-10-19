package com.ndhunju.dailyjournal.controller.mpAndroidCharts.interfaces;

import com.ndhunju.dailyjournal.controller.mpAndroidCharts.data.BarData;

public interface BarDataProvider extends BarLineScatterCandleBubbleDataProvider {

    BarData getBarData();
    boolean isDrawBarShadowEnabled();
    boolean isDrawValueAboveBarEnabled();
    boolean isDrawHighlightArrowEnabled();
}
