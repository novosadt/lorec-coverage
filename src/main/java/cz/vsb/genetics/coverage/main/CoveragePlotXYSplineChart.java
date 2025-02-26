/*
 * Copyright (C) 2025  Tomas Novosad
 * VSB-TUO, Faculty of Electrical Engineering and Computer Science
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package cz.vsb.genetics.coverage.main;

import cz.vsb.genetics.coverage.CoverageInfo;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYSplineRenderer;

import java.util.List;

public class CoveragePlotXYSplineChart extends CoveragePlotBase {
    protected JFreeChart createChart(String title, String xLabel, String yLabel, SamplingType samplingType, List<CoverageInfo> coverageInfos) {
        JFreeChart chart = ChartFactory.createXYStepChart(title, xLabel, yLabel, createDataset(coverageInfos, samplingType),
                PlotOrientation.VERTICAL, true, true, false);

        XYSplineRenderer renderer = new XYSplineRenderer();
        renderer.setFillType(XYSplineRenderer.FillType.NONE);
        chart.getXYPlot().setRenderer(renderer);

        return chart;
    }
}
