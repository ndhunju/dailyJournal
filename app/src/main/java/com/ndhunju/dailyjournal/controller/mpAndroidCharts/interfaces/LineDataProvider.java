package com.ndhunju.dailyjournal.controller.mpAndroidCharts.interfaces;

import com.ndhunju.dailyjournal.controller.mpAndroidCharts.components.YAxis;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.data.LineData;

public interface LineDataProvider extends BarLineScatterCandleBubbleDataProvider {

    LineData getLineData();

    YAxis getAxis(YAxis.AxisDependency dependency);
}
