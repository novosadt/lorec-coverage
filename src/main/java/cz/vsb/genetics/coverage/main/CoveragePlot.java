package cz.vsb.genetics.coverage.main;

import cz.vsb.genetics.coverage.CoverageInfo;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.flow.DefaultFlowDataset;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.xy.XYSeries;

import java.io.File;

public class CoveragePlot {
    public void plotCoverage(CoverageInfo coverageInfo, String title, String labelX, String labelY, String outputFile) throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int[] coverage = coverageInfo.getCoverages();

        for (int i = 0; i < coverage.length; i++) {
            dataset.addValue((double) coverage[i], "", Integer.valueOf(i));
        }

        JFreeChart coverageChart = ChartFactory.createLineChart(title, labelX, labelY, dataset,
                PlotOrientation.VERTICAL, true, true, false);

        int width = 1600;    /* Width of the image */
        int height = 1200;   /* Height of the image */

        File lineChart = new File( outputFile);
        ChartUtils.saveChartAsJPEG(lineChart , coverageChart, width ,height);
    }
}
