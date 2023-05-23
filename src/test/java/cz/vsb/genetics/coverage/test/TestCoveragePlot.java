/*
 * Copyright (C) 2021  Tomas Novosad
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


package cz.vsb.genetics.coverage.test;

import cz.vsb.genetics.common.Chromosome;
import cz.vsb.genetics.coverage.CoverageCalculator;
import cz.vsb.genetics.coverage.CoverageInfo;
import cz.vsb.genetics.coverage.main.CoveragePlot;
import cz.vsb.genetics.coverage.main.CoveragePlotHistogram;
import cz.vsb.genetics.coverage.main.CoveragePlotXYStepChart;
import cz.vsb.genetics.coverage.main.SamplingType;
import cz.vsb.genetics.ngs.coverage.BamCoverageCalculatorMT;
import cz.vsb.genetics.ngs.coverage.BamCoverageCalculatorST;
import cz.vsb.genetics.om.coverage.BionanoCoverageCalculator;
import org.apache.commons.lang3.time.StopWatch;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TestCoveragePlot {
//    private static final String BAM_FILE = "./src/test/resources/coverage/bam/test.bam";
//    private static final String BAM_INDEX_FILE = "./src/test/resources/coverage/bam/test.bai";
    private static final String BAM_FILE = "c:\\data\\fnol\\wgs\\linkedread\\phased_possorted_bam.chr21.bam";
    private static final String BAM_INDEX_FILE = "c:\\data\\fnol\\wgs\\linkedread\\phased_possorted_bam.chr21.bam.bai";
    private static final String BAM_COVERAGE_PLOT_ST = "c:\\tmp\\fnol\\test-hts-coverage\\test.coverage.bam.st.jpg";
    private static final String BAM_COVERAGE_PLOT_MT = "c:\\tmp\\fnol\\test-hts-coverage\\test.coverage.bam.mt.jpg";

    private static final String CMAP_REF = "c:\\data\\fnol\\wgs\\om\\test_r.cmap";
    private static final String CMAP_QRY = "c:\\data\\fnol\\wgs\\om\\test_q.cmap";
    private static final String XMAP = "c:\\data\\fnol\\wgs\\om\\test.xmap";
    private static final String OM_COVERAGE_PLOT = "./src/test/resources/coverage/bam/test.coverage.om.jpg";

    private static final String OM_BAM_COVERAGE_PLOT = "c:\\tmp\\fnol\\test-hts-coverage\\test.coverage.om.bam.jpg";

    public static void main(String[] args) {
        try {
            //ch17 - TP53
//            Chromosome chromosome = Chromosome.chr17;
//            int start = 7668421;
//            int end = 7687490;

            Chromosome chromosome = Chromosome.chr21;
            int start = 1;
            int end = 46709983;

//            Chromosome chromosome = Chromosome.chr21;
//            int start = 8221000;
//            int end = 8222000;

            //testBamCoverageAtIntervalST(chromosome, start, end);
            //testBamCoverageAtIntervalMT(chromosome, start, end, 6);
            //testOmCoverageAtInterval(chromosome, start, end);
            testWgsOmCoverage(chromosome, start, end, 6);
        }
        catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    public static void testBamCoverageAtIntervalST(Chromosome chromosome, int start, int end) throws Exception {
        System.out.println("\nTesting bam coverage info at interval - single-threaded.");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        CoverageCalculator coverageCalculator = new BamCoverageCalculatorST(BAM_FILE, BAM_INDEX_FILE);
        coverageCalculator.open();
        CoverageInfo coverageInfo = coverageCalculator.getIntervalCoverage(chromosome, start, end);
        coverageInfo.setSamplingSize(100);
        coverageInfo.setCoverageLimit(2000);
        coverageCalculator.close();

        stopWatch.stop();
        printTime(stopWatch.getTime());

        CoveragePlot coveragePlot = new CoveragePlotHistogram();
        coveragePlot.plotCoverage("Chromosome 21", "position", "coverage", BAM_COVERAGE_PLOT_ST, SamplingType.RANDOM, coverageInfo);
    }

    public static void testBamCoverageAtIntervalMT(Chromosome chromosome, int start, int end, int threads) throws Exception {
        System.out.println("\nTesting bam coverage info at interval - multi-threaded.");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        CoverageCalculator coverageCalculator = new BamCoverageCalculatorMT(BAM_FILE, BAM_INDEX_FILE, threads);
        coverageCalculator.open();
        CoverageInfo coverageInfo = coverageCalculator.getIntervalCoverage(chromosome, start, end);
        coverageInfo.setSamplingSize(100);
        coverageCalculator.close();

        stopWatch.stop();
        printTime(stopWatch.getTime());

        CoveragePlot coveragePlot = new CoveragePlotXYStepChart();
        coveragePlot.plotCoverage("Chromosome 21", "position", "coverage", BAM_COVERAGE_PLOT_MT, SamplingType.RANDOM, coverageInfo);
    }

    public static void testOmCoverageAtInterval(Chromosome chromosome, int start, int end) throws Exception {
        System.out.println("\nTesting OM coverage info at interval - single-threaded.");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        CoverageCalculator coverageCalculator = new BionanoCoverageCalculator(CMAP_REF, CMAP_QRY, XMAP);
        coverageCalculator.open();
        CoverageInfo coverageInfo = coverageCalculator.getIntervalCoverage(chromosome, start, end);
        coverageInfo.setSamplingSize(10);
        coverageCalculator.close();

        stopWatch.stop();
        printTime(stopWatch.getTime());

        CoveragePlot coveragePlot = new CoveragePlotHistogram();
        coveragePlot.plotCoverage("Chromosome 21", "position", "coverage", OM_COVERAGE_PLOT, SamplingType.RANDOM, coverageInfo);
    }

    public static void testWgsOmCoverage(Chromosome chromosome, int start, int end, int threads) throws Exception {
        System.out.println("\nTesting WGS/OM coverage info at interval.");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        CoverageCalculator coverageCalculatorBam = new BamCoverageCalculatorMT(BAM_FILE, BAM_INDEX_FILE, threads);
        coverageCalculatorBam.open();
        CoverageInfo coverageInfoBam = coverageCalculatorBam.getIntervalCoverage(chromosome, start, end);
        coverageInfoBam.setTitle("WGS");
        coverageInfoBam.setSamplingSize(100);
        coverageInfoBam.setColor(0x8000ff00);
        coverageCalculatorBam.close();

        CoverageCalculator coverageCalculatorOm = new BionanoCoverageCalculator(CMAP_REF, CMAP_QRY, XMAP);
        coverageCalculatorOm.open();
        CoverageInfo coverageInfoOm = coverageCalculatorOm.getIntervalCoverage(chromosome, start, end);
        coverageInfoOm.setTitle("OM");
        coverageInfoOm.setSamplingSize(10);
        coverageInfoOm.setColor(0x80ff0000);
        coverageCalculatorOm.close();

        stopWatch.stop();
        printTime(stopWatch.getTime());

        CoveragePlot coveragePlot = new CoveragePlotHistogram();
        coveragePlot.plotCoverage("Chromosome 21", "position", "coverage", OM_BAM_COVERAGE_PLOT, SamplingType.RANDOM, coverageInfoBam, coverageInfoOm);
    }

    private static void printTime(long mils) {
        Date time = new Date(mils);

        DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        System.out.println("Time elapsed: " + df.format(time));
    }
}
