package cz.vsb.genetics.coverage.main;

import cz.vsb.genetics.coverage.CoverageInfo;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.File;
import java.util.Random;

public abstract class CoveragePlotBase implements CoveragePlot {
    protected int width = 1600;
    protected int height = 1200;

    protected abstract JFreeChart createChart(String title, String xLabel, String yLabel, CoverageInfo... coverageInfos);

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void plotCoverage(String title, String xLabel, String yLabel, String outputFile, CoverageInfo... coverageInfos) throws Exception {
        NumberAxis domainAxis = new NumberAxis(xLabel);
        setupDomainAxisRange(domainAxis, coverageInfos);

        JFreeChart coverageChart = createChart(title, xLabel, yLabel, coverageInfos);

        XYPlot xyPlot = coverageChart.getXYPlot();
        xyPlot.setDomainAxis(domainAxis);

        int coverageLimit = getCoverageLimit(coverageInfos);
        if (coverageLimit > 0) {
            NumberAxis rangeAxis = new NumberAxis(yLabel);
            rangeAxis.setRange(0, coverageLimit);
            rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            xyPlot.setRangeAxis(rangeAxis);
        }

        setupSerieColors(xyPlot, coverageInfos);

        File lineChart = new File(outputFile);
        ChartUtils.saveChartAsJPEG(lineChart , coverageChart, width ,height);
    }

    protected XYSeriesCollection createDataset(CoverageInfo[] coverageInfos) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        for (CoverageInfo coverageInfo : coverageInfos) {
            if (coverageInfo == null)
                continue;

            XYSeries series = new XYSeries(coverageInfo.getTitle());
            int[] coverage = coverageInfo.getCoverages();

            if (coverageInfo.getSamplingSize() == 0) {
                for (int i = 0, j = coverageInfo.getPositionStart(); i < coverage.length; i++, j++)
                    series.add(j, coverage[i]);
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

                    series.add(j, coverage[index]);
                }
            }

            dataset.addSeries(series);
        }

        return dataset;
    }

    protected int getCoverageLimit(CoverageInfo[] coverageInfos) {
        int max = 0;

        for (CoverageInfo coverageInfo : coverageInfos)
            if (coverageInfo.getCoverageLimit() > max)
                max = coverageInfo.getCoverageLimit();

        return max;
    }

    private void setupDomainAxisRange(NumberAxis domainAxis, CoverageInfo[] coverageInfos) {
        int lower = Integer.MAX_VALUE;
        int upper = 0;

        for (CoverageInfo coverageInfo : coverageInfos) {
            if (coverageInfo.getPositionStart() < lower)
                lower = coverageInfo.getPositionStart();

            if (coverageInfo.getPositionEnd() > upper)
                upper = coverageInfo.getPositionEnd();
        }

        domainAxis.setRange(lower, upper);
    }

    private void setupSerieColors(XYPlot xyPlot, CoverageInfo[] coverageInfos) {
        for (CoverageInfo coverageInfo : coverageInfos)
            if (coverageInfo.getColor() == null)
                return;

        Paint[] colors = new Paint[coverageInfos.length];
        for (int i = 0; i < colors.length; i++)
            colors[i] = new Color(coverageInfos[i].getColor(), true);

        xyPlot.setDrawingSupplier(new DefaultDrawingSupplier(
                colors,
                DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));
    }
}
