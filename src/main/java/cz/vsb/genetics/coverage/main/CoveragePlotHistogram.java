package cz.vsb.genetics.coverage.main;

import cz.vsb.genetics.coverage.CoverageInfo;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

public class CoveragePlotHistogram extends CoveragePlotBase {
    protected JFreeChart createChart(String title, String xLabel, String yLabel, CoverageInfo... coverageInfos) {
        return ChartFactory.createHistogram(title, xLabel, yLabel, createDataset(coverageInfos),
                PlotOrientation.VERTICAL, true, true, false);
    }
}
