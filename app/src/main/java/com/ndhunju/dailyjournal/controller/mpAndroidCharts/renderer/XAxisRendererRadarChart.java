
package com.ndhunju.dailyjournal.controller.mpAndroidCharts.renderer;

import android.graphics.Canvas;
import android.graphics.PointF;

import com.ndhunju.dailyjournal.controller.mpAndroidCharts.charts.RadarChart;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.components.XAxis;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.utils.Utils;
import com.ndhunju.dailyjournal.controller.mpAndroidCharts.utils.ViewPortHandler;

public class XAxisRendererRadarChart extends XAxisRenderer {

    private RadarChart mChart;

    public XAxisRendererRadarChart(ViewPortHandler viewPortHandler, XAxis xAxis, RadarChart chart) {
        super(viewPortHandler, xAxis, null);

        mChart = chart;
    }

    @Override
    public void renderAxisLabels(Canvas c) {

        if (!mXAxis.isEnabled() || !mXAxis.isDrawLabelsEnabled())
            return;

        mAxisLabelPaint.setTypeface(mXAxis.getTypeface());
        mAxisLabelPaint.setTextSize(mXAxis.getTextSize());
        mAxisLabelPaint.setColor(mXAxis.getTextColor());

        float sliceangle = mChart.getSliceAngle();

        // calculate the factor that is needed for transforming the value to
        // pixels
        float factor = mChart.getFactor();

        PointF center = mChart.getCenterOffsets();

        int mod = mXAxis.mAxisLabelModulus;
        for (int i = 0; i < mXAxis.getValues().size(); i += mod) {
            String label = mXAxis.getValues().get(i);

            float angle = (sliceangle * i + mChart.getRotationAngle()) % 360f;

            PointF p = Utils.getPosition(center, mChart.getYRange() * factor
                    + mXAxis.mLabelWidth / 2f, angle);

            drawLabel(c, label, i, p.x, p.y + mXAxis.mLabelHeight / 2f);
        }
    }

	/**
	 * XAxis LimitLines on RadarChart not yet supported.
	 *
	 * @param c
	 */
	@Override
	public void renderLimitLines(Canvas c) {
		// this space intentionally left blank
	}
}
