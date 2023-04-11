package cz.vsb.genetics.coverage.main;

import cz.vsb.genetics.coverage.CoverageInfo;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.util.Random;

public class CoveragePlot {
    private int width = 1600;
    private int height = 1200;

    public void plotCoverage(String title, String xLabel, String yLabel, String outputFile, CoverageInfo... coverageInfos) throws Exception {
        JFreeChart coverageChart = ChartFactory.createXYStepChart(title, xLabel, yLabel, createXYDataset(coverageInfos),
                PlotOrientation.VERTICAL, true, true, false);

        NumberAxis xAxis = new NumberAxis(xLabel);
        xAxis.setRange(coverageInfos[0].getPositionStart(), coverageInfos[0].getPositionEnd());
        coverageChart.getXYPlot().setDomainAxis(xAxis);

        if (coverageInfos[0].getCoverageLimit() > 0) {
            NumberAxis yAxis = new NumberAxis(yLabel);
            yAxis.setRange(0, coverageInfos[0].getCoverageLimit());
            coverageChart.getXYPlot().setRangeAxis(yAxis);
        }

        File lineChart = new File( outputFile);
        ChartUtils.saveChartAsJPEG(lineChart , coverageChart, width ,height);
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    XYDataset createXYDataset(CoverageInfo[] coverageInfos) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        for (CoverageInfo coverageInfo : coverageInfos) {
            if (coverageInfo == null)
                continue;

            XYSeries xySeries = new XYSeries(coverageInfo.getTitle());
            int[] coverage = coverageInfo.getCoverages();

            if (coverageInfo.getSamplingSize() == 0) {
                for (int i = 0, j = coverageInfo.getPositionStart(); i < coverage.length; i++, j++)
                    xySeries.add(j, coverage[i]);
            }
            else {
                int sampleSize = coverageInfo.getSamplingSize() - 1;
                Random random = new Random();

                for (int i = 0, j = coverageInfo.getPositionStart(); i < coverage.length; i += sampleSize, j += sampleSize) {
                    int index = random.nextInt(sampleSize) + i;

                    if (index >= coverage.length) {
                        index = coverage.length - 1;
                        j = coverageInfo.getPositionEnd();
                    }

                    xySeries.add(j, coverage[index]);
                }
            }

            dataset.addSeries(xySeries);
        }

        return dataset;
    }
}
