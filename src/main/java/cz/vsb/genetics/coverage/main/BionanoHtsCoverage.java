package cz.vsb.genetics.coverage.main;

import cz.vsb.genetics.common.ChromosomeRegion;
import cz.vsb.genetics.coverage.CoverageCalculator;
import cz.vsb.genetics.coverage.CoverageInfo;
import cz.vsb.genetics.coverage.CoverageStatistics;
import cz.vsb.genetics.ngs.coverage.BamCoverageCalculatorMT;
import cz.vsb.genetics.ngs.coverage.BamCoverageCalculatorST;
import cz.vsb.genetics.om.coverage.BionanoCoverageCalculator;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class BionanoHtsCoverage {
    private static final String ARG_BIONANO_CMAP_REF = "bionano_cmap_ref";
    private static final String ARG_BIONANO_CMAP_QRY = "bionano_cmap_qry";
    private static final String ARG_BIONANO_XMAP = "bionano_xmap";
    private static final String ARG_BIONANO_SAMPLING_STEP = "bionano_sampling_step";
    private static final String ARG_HTS_BAM = "hts_bam";
    private static final String ARG_HTS_BAI = "hts_bai";
    private static final String ARG_HTS_SAMPLING_STEP = "hts_sampling_step";
    private static final String ARG_THREADS = "threads";
    private static final String ARG_REGION = "region";
    private static final String ARG_REGION_FILE = "region_file";
    private static final String ARG_STATISTICS = "statistics";
    private static final String ARG_TITLE = "title";
    private static final String ARG_SAMPLING_TYPE = "sampling_type";
    private static final String ARG_PLOT_TYPE = "plot_type";
    private static final String ARG_SINGLE_IMAGE = "single_image";
    private static final String ARG_COVERAGE_LIMIT = "coverage_limit";
    private static final String ARG_OUTPUT_HTS_IMG = "output_hts_img";
    private static final String ARG_OUTPUT_OM_IMG = "output_om_img";
    private static final String ARG_OUTPUT_IMG = "output_img";

    public static void main(String[] args) {
        CommandLine cmd = getCommandLine(args);

        String bam = cmd.hasOption(ARG_HTS_BAM) ? cmd.getOptionValue(ARG_HTS_BAM) : null;
        String bai = cmd.hasOption(ARG_HTS_BAI) ? cmd.getOptionValue(ARG_HTS_BAI) : null;
        String xmap = cmd.hasOption(ARG_BIONANO_XMAP) ? cmd.getOptionValue(ARG_BIONANO_XMAP) : null;
        String cmapQuery = cmd.hasOption(ARG_BIONANO_CMAP_QRY) ? cmd.getOptionValue(ARG_BIONANO_CMAP_QRY) : null;
        String cmapReference = cmd.hasOption(ARG_BIONANO_CMAP_REF) ? cmd.getOptionValue(ARG_BIONANO_CMAP_REF) : null;


        if (!(StringUtils.isNoneBlank(bam, bai) || StringUtils.isNoneBlank(xmap, cmapQuery, cmapReference))) {
            System.err.println("At least, bam and bai or xmap, cmap query and cmap reference must be defined.");
            System.exit(1);
        }

        try {
            BionanoHtsCoverage coverage = new BionanoHtsCoverage();

            if (cmd.hasOption(ARG_STATISTICS)) {
                coverage.calculateStatistics(bam, bai, cmapReference, cmapQuery, xmap, cmd);
            }
            else {
                coverage.plotCoverage(bam, bai, cmapReference, cmapQuery, xmap, cmd);
            }
        }
        catch (Exception e) {
            System.out.println("Error occured: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static CommandLine getCommandLine(String[] args) {
        Options options = new Options();

        Option bionanoCmapQry = new Option("cmap_q", ARG_BIONANO_CMAP_QRY, true, "bionano cmap query file");
        bionanoCmapQry.setArgName("cmap file");
        bionanoCmapQry.setType(String.class);
        options.addOption(bionanoCmapQry);

        Option bionanoCmapRef = new Option("cmap_r", ARG_BIONANO_CMAP_REF, true, "bionano cmap reference file");
        bionanoCmapRef.setArgName("cmap file");
        bionanoCmapRef.setType(String.class);
        options.addOption(bionanoCmapRef);

        Option bionanoXmap = new Option("xmap", ARG_BIONANO_XMAP, true, "bionano xmap file");
        bionanoXmap.setArgName("xmap file");
        bionanoXmap.setType(String.class);
        options.addOption(bionanoXmap);

        Option bionanoSamplingStep = new Option("bss", ARG_BIONANO_SAMPLING_STEP, true, "no. of marks used for Bionano optical maps sampling - default 10");
        bionanoSamplingStep.setArgName("sampling step");
        bionanoSamplingStep.setType(Integer.class);
        options.addOption(bionanoSamplingStep);

        Option htsBam = new Option("bam", ARG_HTS_BAM, true, "hts bam file");
        htsBam.setArgName("bam file");
        htsBam.setType(String.class);
        options.addOption(htsBam);

        Option htsBai = new Option("bai", ARG_HTS_BAI, true, "hts bam index file");
        htsBai.setArgName("bai file");
        htsBai.setType(String.class);
        options.addOption(htsBai);

        Option htsSamplingStep = new Option("hss", ARG_HTS_SAMPLING_STEP, true, "region size (no. of bases) used for HTS sampling - default 100");
        htsSamplingStep.setArgName("sampling step");
        htsSamplingStep.setType(Integer.class);
        options.addOption(htsSamplingStep);

        Option threads = new Option("t", ARG_THREADS, true, "number of threads for parallel processing");
        threads.setArgName("threads");
        threads.setType(Integer.class);
        options.addOption(threads);

        Option coverageLimit = new Option("cl", ARG_COVERAGE_LIMIT, true, "Set coverage limit for plotting (maximum y axis value)");
        coverageLimit.setArgName("coverage limit");
        coverageLimit.setType(Integer.class);
        options.addOption(coverageLimit);

        Option region = new Option("r", ARG_REGION, true, "chromosomal region of interest (e.g. chr1:1-1000)");
        region.setArgName("chromosomal region");
        region.setType(String.class);
        options.addOption(region);

        Option regionFile = new Option("rf", ARG_REGION_FILE, true, "file with chromosomal regions of interest (contig_name region");
        regionFile.setRequired(true);
        regionFile.setArgName("chromosomal regions file");
        regionFile.setType(String.class);
        options.addOption(regionFile);

        Option title = new Option("ti", ARG_TITLE, true, "plot title");
        title.setArgName("title");
        title.setType(String.class);
        options.addOption(title);

        Option samplingType = new Option("st", ARG_SAMPLING_TYPE, true, "sampling type [random|mean|median|none] - default random");
        samplingType.setArgName("sampling type");
        samplingType.setType(String.class);
        options.addOption(samplingType);

        Option plotType = new Option("pt", ARG_PLOT_TYPE, true, "plot type [histogram|line|spline] - default histogram");
        plotType.setArgName("plot type");
        plotType.setType(String.class);
        options.addOption(plotType);

        Option singleImage = new Option("si", ARG_SINGLE_IMAGE, false, "whether to plot HTS and OM in single image");
        singleImage.setArgName("single image");
        options.addOption(singleImage);

        Option statistics = new Option("stats", ARG_STATISTICS, true, "calculate coverage statistics for region file (min, q1, median, q3, max, median)");
        statistics.setArgName("statistics output file");
        statistics.setType(String.class);
        options.addOption(statistics);

        Option outputHtsImg = new Option("img_hts", ARG_OUTPUT_HTS_IMG, true, "output HTS coverage plot");
        outputHtsImg.setArgName("hts coverage image");
        outputHtsImg.setType(String.class);
        options.addOption(outputHtsImg);

        Option outputOmImg = new Option("img_om", ARG_OUTPUT_OM_IMG, true, "output OM coverage plot");
        outputOmImg.setArgName("om coverage image");
        outputOmImg.setType(String.class);
        options.addOption(outputOmImg);

        Option outputImg = new Option("img", ARG_OUTPUT_IMG, true, "output OM/WGS coverage plot");
        outputImg.setArgName("joint hts/om coverage image");
        outputImg.setType(String.class);
        options.addOption(outputImg);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("\nSVC - Bionano Genomics (OM) and HTS coverage plot tool, v" + version() + "\n");
            System.out.println(e.getMessage());
            System.out.println();
            formatter.printHelp(
                    300,
                    "\njava -jar om-hts-coverage.jar ",
                    "\noptions:",
                    options,
                    "\nTomas Novosad, VSB-TU Ostrava, 2023" +
                            "\nFEI, Department of Computer Science" +
                            "\nVersion: " + version() +
                            "\nLicense: GPL-3.0-only ");

            System.exit(1);
        }

        return cmd;
    }

    private static String version() {
        final Properties properties = new Properties();

        try {
            properties.load(BionanoHtsCoverage.class.getClassLoader().getResourceAsStream("project.properties"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return properties.getProperty("version");
    }

    private void calculateStatistics(String bam, String bai, String cmapReference, String cmapQuery, String xmap, CommandLine cmd) throws Exception {
        String regionFile = cmd.getOptionValue(ARG_REGION_FILE);
        String outputStats = cmd.getOptionValue(ARG_STATISTICS);
        int threads = cmd.hasOption(ARG_THREADS) ? Integer.valueOf(cmd.getOptionValue(ARG_THREADS)) : 1;

        List<ChromosomeRegion> regions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(regionFile))) {
            String line;
            while((line = reader.readLine()) != null) {
                String[] values = line.split("\t");

                if (values.length < 2) {
                    logError("Invalid line: " + line);
                    continue;
                }

                ChromosomeRegion region = ChromosomeRegion.valueOf(values[1]);

                if (region == null) {
                    logError("Invalid region: " + line);
                    continue;
                }

                region.setName(values[0]);
                regions.add(region);
            }
        }

        List<CoverageInfo> coverageInfosHts = getCoverageInfoHts(bam, bai, regions, threads, 0);
        List<CoverageInfo> coverageInfosOm = getCoverageInfoOm(cmapReference, cmapQuery, xmap, regions, 0);

        assert coverageInfosHts.size() == regions.size() && coverageInfosOm.size() == regions.size();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputStats))) {
            writer.write("contig_name\tregion\tlength\thts_min\thts_q1\thts_median\thts_q3\thts_max\thts_mean\thts_stddev\tom_min\tom_q2\tom_median\tom_q3\tom_max\tom_mean\tom_stddev\tom_site_count\n");
            CoverageStatistics stats = new CoverageStatistics();
            for (int i = 0; i < coverageInfosHts.size(); i++) {
                ChromosomeRegion region = regions.get(i);
                System.out.printf("Calculating statistics for: %s - %s... %d/%d\n", region.getName(), region, i + 1, coverageInfosHts.size());

                CoverageInfo coverageInfoHts = coverageInfosHts.get(i);
                CoverageInfo coverageInfoOm = coverageInfosOm.get(i);

                String out = String.format("%s\t%s\t%d", region.getName(), region, region.getLength());
                String format = "\t%d\t%d\t%d\t%d\t%d\t%d\t%d";
                String blank = "\t\t\t\t\t\t\t";

                if (coverageInfoHts != null) {
                    stats.calculateStatistics(coverageInfoHts);
                    out += String.format(format, stats.min(), stats.q1(), stats.median(), stats.q3(), stats.max(), stats.mean(), stats.standardDeviation());
                }
                else
                    out += blank;

                if (coverageInfoOm != null) {
                    stats.calculateStatistics(coverageInfoOm);
                    out += String.format(format, stats.min(), stats.q1(), stats.median(), stats.q3(), stats.max(), stats.mean(), stats.standardDeviation());
                    out += "\t" + coverageInfoOm.getSiteCount();
                }
                else
                    out += blank + "\t";

                out += "\n";

                writer.write(out);
            }
        }
    }

    private void plotCoverage(String bam, String bai, String cmapReference, String cmapQuery, String xmap, CommandLine cmd) throws Exception {
        String outputHtsImg = cmd.hasOption(ARG_OUTPUT_HTS_IMG) ? cmd.getOptionValue(ARG_OUTPUT_HTS_IMG) : null;
        String outputOmImg = cmd.hasOption(ARG_OUTPUT_OM_IMG) ? cmd.getOptionValue(ARG_OUTPUT_OM_IMG) : null;
        String outputImg = cmd.hasOption(ARG_OUTPUT_IMG) ? cmd.getOptionValue(ARG_OUTPUT_IMG) : null;
        String title = cmd.hasOption(ARG_TITLE) ? cmd.getOptionValue(ARG_TITLE) : "";
        boolean singleImage = cmd.hasOption(ARG_SINGLE_IMAGE);
        SamplingType samplingType = SamplingType.of(cmd.getOptionValue(ARG_SAMPLING_TYPE));
        PlotType plotType = PlotType.of(cmd.getOptionValue(ARG_PLOT_TYPE));
        int threads = cmd.hasOption(ARG_THREADS) ? Integer.valueOf(cmd.getOptionValue(ARG_THREADS)) : 1;
        int htsSamplingStep = cmd.hasOption(ARG_HTS_SAMPLING_STEP) ? Integer.valueOf(cmd.getOptionValue(ARG_HTS_SAMPLING_STEP)) : 100;
        int bionanoSamplingStep = cmd.hasOption(ARG_BIONANO_SAMPLING_STEP) ? Integer.valueOf(cmd.getOptionValue(ARG_BIONANO_SAMPLING_STEP)) : 10;
        String region = cmd.hasOption(ARG_REGION) ? cmd.getOptionValue(ARG_REGION) : null;

        CoverageInfo htsCoverage = getCoverageInfoHts(bam, bai, ChromosomeRegion.valueOf(region), threads, htsSamplingStep);
        CoverageInfo omCoverage = getCoverageInfoOm(cmapReference, cmapQuery, xmap, ChromosomeRegion.valueOf(region), bionanoSamplingStep);

        if (htsCoverage == null && omCoverage == null) {
            exitError("Missing arguments for coverage calculation. Some of bam, bai, cmap, xmap or region");
        }

        if (cmd.hasOption(ARG_COVERAGE_LIMIT)) {
            int coverageLimit = Integer.valueOf(cmd.getOptionValue(ARG_COVERAGE_LIMIT));
            if (htsCoverage != null)
                htsCoverage.setCoverageLimit(coverageLimit);
            if (omCoverage != null)
                omCoverage.setCoverageLimit(coverageLimit);
        }

        CoveragePlot coveragePlot = createCoveragePlot(plotType);

        if (singleImage) {
            coveragePlot.plotCoverage(title, "Position", "Coverage", outputImg, samplingType, htsCoverage, omCoverage);
        }
        else {
            if (htsCoverage != null)
                coveragePlot.plotCoverage(title, "Position", "Coverage", outputHtsImg, samplingType, htsCoverage);

            if (omCoverage != null)
                coveragePlot.plotCoverage(title, "Position", "Coverage", outputOmImg, samplingType, omCoverage);
        }
    }

    private CoverageInfo getCoverageInfoHts(String bam, String bai, ChromosomeRegion region, int threads, int samplingSize) throws Exception {
        return getCoverageInfoHts(bam, bai, Arrays.asList(region), threads, samplingSize).get(0);
    }

    private List<CoverageInfo> getCoverageInfoHts(String bam, String bai, List<ChromosomeRegion> regions, int threads, int samplingSize) throws Exception {
        if (StringUtils.isBlank(bam) || StringUtils.isBlank(bai) || regions == null || regions.size() == 0)
            return Collections.singletonList(null);

        try (CoverageCalculator coverageCalculator = threads == 1
                ? new BamCoverageCalculatorST(bam, bai) : new BamCoverageCalculatorMT(bam, bai, threads)) {
            coverageCalculator.open();

            List<CoverageInfo> coverageInfos = new ArrayList<>();
            int counter = 1;
            for (ChromosomeRegion region : regions) {
                System.out.printf("Calculating coverage for: %s - %s... %d/%d\n", region.getName(), region, counter++, regions.size());

                CoverageInfo coverageInfo = coverageCalculator.getIntervalCoverage(region.getChromosome(), region.getStart(), region.getEnd());
                coverageInfo.setSamplingSize(samplingSize);
                coverageInfo.setColor(Color.RED.getRGB());
                coverageInfo.setName("HTS");

                coverageInfos.add(coverageInfo);
            }

            return coverageInfos;
        }
    }

    private CoverageInfo getCoverageInfoOm(String cmapRef, String cmapQry, String xmap, ChromosomeRegion region, int samplingSize) throws Exception {
        return getCoverageInfoOm(cmapRef, cmapQry, xmap, Arrays.asList(region), samplingSize).get(0);
    }

    private List<CoverageInfo> getCoverageInfoOm(String cmapRef, String cmapQry, String xmap, List<ChromosomeRegion> regions, int samplingSize) throws Exception {
        if (StringUtils.isBlank(cmapRef) || StringUtils.isBlank(cmapQry) || StringUtils.isBlank(xmap) || regions == null || regions.size() == 0)
            return Collections.singletonList(null);

        try (CoverageCalculator coverageCalculator = new BionanoCoverageCalculator(cmapRef, cmapQry, xmap)) {
            coverageCalculator.open();

            List<CoverageInfo> coverageInfos = new ArrayList<>();
            int counter = 1;
            for (ChromosomeRegion region : regions) {
                System.out.printf("Calculating coverage for: %s - %s... %d/%d\n", region.getName(), region, counter++, regions.size());

                CoverageInfo coverageInfo = coverageCalculator.getIntervalCoverage(region.getChromosome(), region.getStart(), region.getEnd());
                coverageInfo.setSamplingSize(samplingSize);
                coverageInfo.setColor(Color.BLUE.getRGB());
                coverageInfo.setName("OM");

                coverageInfos.add(coverageInfo);
            }

            return coverageInfos;
        }
    }

    private CoveragePlot createCoveragePlot(PlotType plotType) {
        CoveragePlot coveragePlot;

        switch (plotType) {
            case LINE:
                coveragePlot = new CoveragePlotXYStepChart();
                break;
            case SPLINE:
                coveragePlot = new CoveragePlotXYSplineChart();
                break;
            default:
                coveragePlot = new CoveragePlotHistogramChart();
                break;
        }

        return coveragePlot;
    }

    private void logError(String msg) {
        System.err.println("ERROR: " + msg);
    }
    
    private void exitError(String message) {
        if (StringUtils.isNoneBlank(message))
            System.err.println("ERROR: " + message);
        
        System.exit(1);
    }
}
