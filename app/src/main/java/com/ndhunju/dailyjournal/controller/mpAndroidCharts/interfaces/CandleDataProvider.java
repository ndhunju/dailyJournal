package com.ndhunju.dailyjournal.controller.mpAndroidCharts.interfaces;

import com.github.mikephil.charting.data.CandleData;

public interface CandleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    CandleData getCandleData();
}
