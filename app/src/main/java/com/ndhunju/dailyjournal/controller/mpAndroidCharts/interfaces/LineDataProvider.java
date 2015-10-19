package com.ndhunju.dailyjournal.controller.mpAndroidCharts.interfaces;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;

public interface LineDataProvider extends BarLineScatterCandleBubbleDataProvider {

    LineData getLineData();

    YAxis getAxis(YAxis.AxisDependency dependency);
}
