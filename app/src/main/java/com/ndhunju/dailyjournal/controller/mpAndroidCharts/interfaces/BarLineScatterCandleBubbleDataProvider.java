package com.ndhunju.dailyjournal.controller.mpAndroidCharts.interfaces;

import com.ndhunju.dailyjournal.controller.mpAndroidCharts.components.YAxis.AxisDependency;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.data.BarLineScatterCandleBubbleData;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.utils.Transformer;

public interface BarLineScatterCandleBubbleDataProvider extends ChartInterface {

    Transformer getTransformer(AxisDependency axis);
    int getMaxVisibleCount();
    boolean isInverted(AxisDependency axis);
    
    int getLowestVisibleXIndex();
    int getHighestVisibleXIndex();

    BarLineScatterCandleBubbleData getData();
}
