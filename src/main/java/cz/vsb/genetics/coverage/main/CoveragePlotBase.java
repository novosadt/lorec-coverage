package cz.vsb.genetics.coverage.main;

import cz.vsb.genetics.coverage.CoverageInfo;
import org.apache.commons.lang3.ObjectUtils;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class CoveragePlotBase implements CoveragePlot {
    protected int width = 1600;
    protected int height = 1200;

    protected abstract JFreeChart createChart(String title, String xLabel, String yLabel, SamplingType samplingType, CoverageInfo... coverageInfos);

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void plotCoverage(String title, String xLabel, String yLabel, String outputFile, SamplingType samplingType, CoverageInfo... coverageInfos) throws Exception {
        NumberAxis domainAxis = new NumberAxis(xLabel);
        setupDomainAxisRange(domainAxis, coverageInfos);

        JFreeChart coverageChart = createChart(title, xLabel, yLabel, samplingType, coverageInfos);

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

    protected XYSeriesCollection createDataset(CoverageInfo[] coverageInfos, SamplingType samplingType) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        for (CoverageInfo coverageInfo : coverageInfos) {
            if (coverageInfo == null)
                continue;

            XYSeries series = new XYSeries(coverageInfo.getTitle());
            int[] coverage = coverageInfo.getCoverages();

            switch (samplingType) {
                case NONE: sampleNone(coverageInfo, series, coverage); break;
                case MEAN: sampleMean(coverageInfo, series, coverage); break;
                case MEDIAN: sampleMedian(coverageInfo, series, coverage); break;
                default: sampleRandom(coverageInfo, series, coverage); break;
            }

            dataset.addSeries(series);
        }

        return dataset;
    }

    private static void sampleNone(CoverageInfo coverageInfo, XYSeries series, int[] coverage) {
        for (int i = 0, j = coverageInfo.getPositionStart(); i < coverage.length; i++, j++)
            series.add(j, coverage[i]);
    }

    private static void sampleRandom(CoverageInfo coverageInfo, XYSeries series, int[] coverage) {
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

    private static void sampleMean(CoverageInfo coverageInfo, XYSeries series, int[] coverage) {
        int sampleSize = coverageInfo.getSamplingSize() - 1;
        Random random = new Random();

        long sum = 0;
        int to = coverageInfo.getPositionStart() + sampleSize;
        to = to > coverage.length ? coverage.length : to;
        int counter = 0;

        for (int i = coverageInfo.getPositionStart(); i < to; i++) {
            sum += coverage[i];

            if (++counter == sampleSize) {
                series.add((i - sampleSize + i) / 2 , sum / sampleSize);
                sum = 0;
                counter = 0;
            }
        }

        if (counter > 0)
            series.add((coverage.length - counter + coverage.length) / 2 , sum / counter);
    }

    private static void sampleMedian(CoverageInfo coverageInfo, XYSeries series, int[] coverage) {
        int sampleSize = coverageInfo.getSamplingSize() - 1;
        Random random = new Random();

        int to = coverageInfo.getPositionStart() + sampleSize;
        to = to > coverage.length ? coverage.length : to;
        List<Integer> values = new ArrayList<>();

        for (int i = coverageInfo.getPositionStart(); i < to; i++) {
            values.add(coverage[i]);

            if (values.size() == sampleSize) {
                series.add((i - sampleSize + i) / 2 , ObjectUtils.median(values.toArray(new Integer[sampleSize])));
                values.clear();
            }
        }

        if (values.size() > 0)
            series.add((coverage.length - values.size() + coverage.length) / 2 , ObjectUtils.median(values.toArray(new Integer[values.size()])));
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
