package com.ndhunju.dailyjournal.controller.mpAndroidCharts.interfaces;

import com.ndhunju.dailyjournal.controller.mpAndroidCharts.data.CandleData;

public interface CandleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    CandleData getCandleData();
}
