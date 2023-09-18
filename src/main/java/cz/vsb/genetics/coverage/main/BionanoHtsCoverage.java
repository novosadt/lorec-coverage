package cz.vsb.genetics.coverage.main;

import cz.vsb.genetics.common.ChromosomeRegion;
import cz.vsb.genetics.coverage.CoverageCalculator;
import cz.vsb.genetics.coverage.CoverageInfo;
import cz.vsb.genetics.coverage.CoverageStatistics;
import cz.vsb.genetics.ngs.coverage.BamCoverageCalculatorMT;
import cz.vsb.genetics.ngs.coverage.BamCoverageCalculatorST;
import cz.vsb.genetics.om.coverage.BionanoCoverageCalculator;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;

public class BionanoHtsCoverage {
    private static final Logger log = LoggerFactory.getLogger(BionanoHtsCoverage.class);

    private static final String ARG_BIONANO_CMAP_REF = "bionano_cmap_ref";
    private static final String ARG_BIONANO_CMAP_QRY = "bionano_cmap_qry";
    private static final String ARG_BIONANO_XMAP = "bionano_xmap";
    private static final String ARG_BIONANO_SAMPLING_STEP = "bionano_sampling_step";
    private static final String ARG_HTS_BAM = "hts_bam";
    private static final String ARG_HTS_SAMPLING_STEP = "hts_sampling_step";
    private static final String ARG_THREADS = "threads";
    private static final String ARG_REGION = "region";
    private static final String ARG_REGION_FILE = "region_file";
    private static final String ARG_STATISTICS = "statistics";
    private static final String ARG_TITLE = "title";
    private static final String ARG_SAMPLING_TYPE = "sampling_type";
    private static final String ARG_PLOT_TYPE = "plot_type";
    private static final String ARG_SINGLE_IMAGE = "single_image";
    private static final String ARG_COVERAGE_LIMIT_HTS = "coverage_limit_hts";
    private static final String ARG_COVERAGE_LIMIT_OM = "coverage_limit_om";
    private static final String ARG_OUTPUT_HTS_IMG = "output_hts_img";
    private static final String ARG_OUTPUT_OM_IMG = "output_om_img";
    private static final String ARG_OUTPUT_IMG = "output_img";
    private static final String ARG_OUTPUT_DIR = "output_dir";
    private static final String ARG_OUTPUT_FORMAT = "output_format";
    private static final String ARG_SAMPLE_NAME = "sample_name";


    private static final Color[] COLORS = {Color.BLACK, Color.CYAN, Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.YELLOW};

    public static void main(String[] args) {
        CommandLine cmd = getCommandLine(args);

        String bam = cmd.hasOption(ARG_HTS_BAM) ? cmd.getOptionValue(ARG_HTS_BAM) : null;
        String xmap = cmd.hasOption(ARG_BIONANO_XMAP) ? cmd.getOptionValue(ARG_BIONANO_XMAP) : null;
        String cmapQuery = cmd.hasOption(ARG_BIONANO_CMAP_QRY) ? cmd.getOptionValue(ARG_BIONANO_CMAP_QRY) : null;
        String cmapReference = cmd.hasOption(ARG_BIONANO_CMAP_REF) ? cmd.getOptionValue(ARG_BIONANO_CMAP_REF) : null;
        ImageFormat imageFormat = ImageFormat.of(cmd.getOptionValue(ARG_OUTPUT_FORMAT));

        if (!(StringUtils.isNoneBlank(bam) || StringUtils.isNoneBlank(xmap, cmapQuery, cmapReference))) {
            log.error("At least, bam and bai or xmap, cmap query and cmap reference must be defined.");
            System.exit(1);
        }

        try {
            BionanoHtsCoverage coverage = new BionanoHtsCoverage();

            String[] bams = bam.split(";");

            if (cmd.hasOption(ARG_STATISTICS)) {
                coverage.calculateStatistics(bams, cmapReference, cmapQuery, xmap, cmd);
            }
            else {
                if (cmd.hasOption(ARG_REGION))
                    coverage.plotCoverage(bams, cmapReference, cmapQuery, xmap, imageFormat, cmd);

                if (cmd.hasOption(ARG_REGION_FILE))
                    coverage.plotCoverageMulti(bams, cmapReference, cmapQuery, xmap, imageFormat, cmd);
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
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

        Option htsSamplingStep = new Option("hss", ARG_HTS_SAMPLING_STEP, true, "region size (no. of bases) used for HTS sampling - default 100");
        htsSamplingStep.setArgName("sampling step");
        htsSamplingStep.setType(Integer.class);
        options.addOption(htsSamplingStep);

        Option threads = new Option("t", ARG_THREADS, true, "number of threads for parallel processing");
        threads.setArgName("threads");
        threads.setType(Integer.class);
        options.addOption(threads);

        Option coverageLimitHts = new Option("hcl", ARG_COVERAGE_LIMIT_HTS, true, "Set coverage limit for plotting HTS (maximum y axis value)");
        coverageLimitHts.setArgName("coverage limit");
        coverageLimitHts.setType(Integer.class);
        options.addOption(coverageLimitHts);

        Option coverageLimitOm = new Option("bcl", ARG_COVERAGE_LIMIT_OM, true, "Set coverage limit for plotting Bionano optical maps (maximum y axis value)");
        coverageLimitOm.setArgName("coverage limit");
        coverageLimitOm.setType(Integer.class);
        options.addOption(coverageLimitOm);

        Option region = new Option("r", ARG_REGION, true, "chromosomal region of interest (e.g. chr1:1-1000)");
        region.setArgName("chromosomal region");
        region.setType(String.class);
        options.addOption(region);

        Option regionFile = new Option("rf", ARG_REGION_FILE, true, "file with chromosomal regions of interest (contig_name region");
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

        Option outputDir = new Option("od", ARG_OUTPUT_DIR, true, "output directory for OM/WGS coverage plots");
        outputDir.setArgName("output directory");
        outputDir.setType(String.class);
        options.addOption(outputDir);

        Option sampleName = new Option("sn", ARG_SAMPLE_NAME, true, "sample name for prefixing OM/WGS coverage plot titles and image names");
        sampleName.setArgName("sample name");
        sampleName.setType(String.class);
        options.addOption(sampleName);

        Option outputFormat = new Option("of", ARG_OUTPUT_FORMAT, true, "output image format [jpg|png|pdf|svg]");
        outputFormat.setArgName("output format");
        outputFormat.setType(String.class);
        options.addOption(outputFormat);

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
            log.error(e.getMessage(), e);
        }

        return properties.getProperty("version");
    }

    private void calculateStatistics(String[] bams, String cmapReference, String cmapQuery, String xmap, CommandLine cmd) throws Exception {
        String regionFile = cmd.getOptionValue(ARG_REGION_FILE);
        String outputStats = cmd.getOptionValue(ARG_STATISTICS);
        int threads = cmd.hasOption(ARG_THREADS) ? Integer.parseInt(cmd.getOptionValue(ARG_THREADS)) : 1;

        List<ChromosomeRegion> regions = getChromosomeRegions(regionFile);
        Map<ChromosomeRegion, List<CoverageInfo>> coverageInfosHts = getCoverageInfoHts(bams, regions, threads, 0);
        Map<ChromosomeRegion, CoverageInfo> coverageInfosOm = getCoverageInfoOm(cmapReference, cmapQuery, xmap, regions, 0);

        assert coverageInfosHts.size() == regions.size() && coverageInfosOm.size() == regions.size();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputStats))) {
            writer.write("contig_name\tregion\tlength\thts_min\thts_q1\thts_median\thts_q3\thts_max\thts_mean\thts_stddev\tom_min\tom_q2\tom_median\tom_q3\tom_max\tom_mean\tom_stddev\tom_site_count\n");
            CoverageStatistics stats = new CoverageStatistics();
            for (int i = 0; i < regions.size(); i++) {
                ChromosomeRegion region = regions.get(i);
                log.info(String.format("Calculating statistics for: %s - %s... %d/%d\n", region.getName(), region, i + 1, coverageInfosHts.size()));

                List<CoverageInfo> coverageInfoHts = coverageInfosHts.get(region);
                CoverageInfo coverageInfoOm = coverageInfosOm.get(region);

                String out = String.format("%s\t%s\t%d", region.getName(), region, region.getLength());
                String format = "\t%s\t%d\t%d\t%d\t%d\t%d\t%d\t%d";
                String blank = "\t\t\t\t\t\t\t\t";

                if (coverageInfoHts != null) {
                    for (CoverageInfo coverageInfo : coverageInfoHts) {
                        stats.calculateStatistics(coverageInfo);
                        out += String.format(format, coverageInfo.getName(), stats.min(), stats.q1(), stats.median(), stats.q3(), stats.max(), stats.mean(), stats.standardDeviation());
                    }
                }
                else
                    out += blank;

                if (coverageInfoOm != null) {
                    stats.calculateStatistics(coverageInfoOm);
                    out += String.format(format, coverageInfoOm.getName(), stats.min(), stats.q1(), stats.median(), stats.q3(), stats.max(), stats.mean(), stats.standardDeviation());
                    out += "\t" + coverageInfoOm.getSiteCount();
                }
                else
                    out += blank + "\t";

                out += "\n";

                writer.write(out);
            }
        }
    }

    private List<ChromosomeRegion> getChromosomeRegions(String regionFile) throws IOException {
        List<ChromosomeRegion> regions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(regionFile))) {
            String line;
            while((line = reader.readLine()) != null) {
                String[] values = line.split("\t");

                if (values.length < 1) {
                    logError("Invalid line: " + line);
                    continue;
                }

                ChromosomeRegion region = values.length == 1 ? ChromosomeRegion.valueOf(values[0]) : ChromosomeRegion.valueOf(values[1]);

                if (region == null) {
                    logError("Invalid region: " + line);
                    continue;
                }

                if (values.length > 1)
                    region.setName(values[0]);

                regions.add(region);
            }
        }
        return regions;
    }

    private void plotCoverage(String[] bams, String cmapReference, String cmapQuery, String xmap, ImageFormat imageFormat, CommandLine cmd) throws Exception {
        String outputHtsImg = cmd.hasOption(ARG_OUTPUT_HTS_IMG) ? cmd.getOptionValue(ARG_OUTPUT_HTS_IMG) : null;
        String outputOmImg = cmd.hasOption(ARG_OUTPUT_OM_IMG) ? cmd.getOptionValue(ARG_OUTPUT_OM_IMG) : null;
        String outputImg = cmd.hasOption(ARG_OUTPUT_IMG) ? cmd.getOptionValue(ARG_OUTPUT_IMG) : null;
        String title = cmd.hasOption(ARG_TITLE) ? cmd.getOptionValue(ARG_TITLE) : "";
        boolean singleImage = cmd.hasOption(ARG_SINGLE_IMAGE);
        SamplingType samplingType = SamplingType.of(cmd.getOptionValue(ARG_SAMPLING_TYPE));
        PlotType plotType = PlotType.of(cmd.getOptionValue(ARG_PLOT_TYPE));
        int threads = cmd.hasOption(ARG_THREADS) ? Integer.parseInt(cmd.getOptionValue(ARG_THREADS)) : 1;
        int htsSamplingStep = cmd.hasOption(ARG_HTS_SAMPLING_STEP) ? Integer.parseInt(cmd.getOptionValue(ARG_HTS_SAMPLING_STEP)) : 100;
        int bionanoSamplingStep = cmd.hasOption(ARG_BIONANO_SAMPLING_STEP) ? Integer.parseInt(cmd.getOptionValue(ARG_BIONANO_SAMPLING_STEP)) : 10;
        String region = cmd.hasOption(ARG_REGION) ? cmd.getOptionValue(ARG_REGION) : null;

        int coverageLimitHts = cmd.hasOption(ARG_COVERAGE_LIMIT_HTS) ? Integer.parseInt(cmd.getOptionValue(ARG_COVERAGE_LIMIT_HTS)) : 0;
        int coverageLimitOm = cmd.hasOption(ARG_COVERAGE_LIMIT_OM) ? Integer.parseInt(cmd.getOptionValue(ARG_COVERAGE_LIMIT_OM)) : 0;


        List<CoverageInfo> htsCoverage = getCoverageInfoHts(bams, ChromosomeRegion.valueOf(region), threads, htsSamplingStep);
        CoverageInfo omCoverage = getCoverageInfoOm(cmapReference, cmapQuery, xmap, ChromosomeRegion.valueOf(region), bionanoSamplingStep);

        if (htsCoverage == null && omCoverage == null) {
            exitError("Missing arguments for coverage calculation. Some of bam, bai, cmap, xmap or region");
        }

        if (htsCoverage != null)
            htsCoverage.forEach(coverageInfo -> coverageInfo.setCoverageLimit(coverageLimitHts));

        if (omCoverage != null)
            omCoverage.setCoverageLimit(coverageLimitOm);


        plotRegionCoverage(outputHtsImg, outputOmImg, outputImg, title, singleImage, samplingType, plotType, htsCoverage, omCoverage, imageFormat);
    }

    private void plotCoverageMulti(String[] bams, String cmapReference, String cmapQuery, String xmap, ImageFormat imageFormat, CommandLine cmd) throws Exception {
        int threads = cmd.hasOption(ARG_THREADS) ? Integer.parseInt(cmd.getOptionValue(ARG_THREADS)) : 1;
        int htsSamplingStep = cmd.hasOption(ARG_HTS_SAMPLING_STEP) ? Integer.parseInt(cmd.getOptionValue(ARG_HTS_SAMPLING_STEP)) : 100;
        int bionanoSamplingStep = cmd.hasOption(ARG_BIONANO_SAMPLING_STEP) ? Integer.parseInt(cmd.getOptionValue(ARG_BIONANO_SAMPLING_STEP)) : 10;
        boolean singleImage = cmd.hasOption(ARG_SINGLE_IMAGE);
        SamplingType samplingType = SamplingType.of(cmd.getOptionValue(ARG_SAMPLING_TYPE));
        PlotType plotType = PlotType.of(cmd.getOptionValue(ARG_PLOT_TYPE));
        String regionFile = cmd.hasOption(ARG_REGION_FILE) ? cmd.getOptionValue(ARG_REGION_FILE) : null;
        int coverageLimitHts = cmd.hasOption(ARG_COVERAGE_LIMIT_HTS) ? Integer.parseInt(cmd.getOptionValue(ARG_COVERAGE_LIMIT_HTS)) : 0;
        int coverageLimitOm = cmd.hasOption(ARG_COVERAGE_LIMIT_OM) ? Integer.parseInt(cmd.getOptionValue(ARG_COVERAGE_LIMIT_OM)) : 0;
        String sampleName = cmd.hasOption(ARG_SAMPLE_NAME) ? cmd.getOptionValue(ARG_SAMPLE_NAME) : "";
        String outputDir = cmd.hasOption(ARG_OUTPUT_DIR) ? cmd.getOptionValue(ARG_OUTPUT_DIR) : "./";
        outputDir = outputDir.endsWith("/") || outputDir.endsWith("\\") ? outputDir : outputDir + "/";

        List<ChromosomeRegion> regions = getChromosomeRegions(regionFile);
        Map<ChromosomeRegion, List<CoverageInfo>> coverageInfosHts = getCoverageInfoHts(bams, regions, threads, htsSamplingStep);
        Map<ChromosomeRegion, CoverageInfo> coverageInfosOm = getCoverageInfoOm(cmapReference, cmapQuery, xmap, regions, bionanoSamplingStep);

        if (coverageInfosHts.size() == 0 && coverageInfosOm.size() == 0) {
            exitError("Missing arguments for coverage calculation. Probably some of bam, bai, cmap, xmap or region file arguments missing.");
        }

        for (int i = 0; i < regions.size(); i++) {
            ChromosomeRegion region = regions.get(i);
            log.info(String.format("Plotting coverage for: %s - %s... %d/%d\n", region.getName(), region, i + 1, regions.size()));

            List<CoverageInfo> htsCoverage = coverageInfosHts.get(region);
            CoverageInfo omCoverage = coverageInfosOm.get(region);

            if (htsCoverage == null && omCoverage == null) {
                log.info("No coverage information for region: " + region);
                continue;
            }

            if (htsCoverage != null)
                htsCoverage.forEach(coverageInfo -> coverageInfo.setCoverageLimit(coverageLimitHts));

            if (omCoverage != null)
                omCoverage.setCoverageLimit(coverageLimitOm);

            String name = StringUtils.isBlank(region.getName()) ? sampleName : sampleName + "_" + region.getName();
            String extension = "." + imageFormat.value;
            String outputHtsImg = outputDir + name + "_hts_" + region.toString().replaceAll(":", "_") + extension;
            String outputOmImg = outputDir + name + "_om_" + region.toString().replaceAll(":", "_") + extension;
            String outputImg = outputDir + name + "_" + region.toString().replaceAll(":", "_") + extension;
            String title = StringUtils.join(sampleName, region.getName(), region);

            plotRegionCoverage(outputHtsImg, outputOmImg, outputImg, title, singleImage, samplingType, plotType, htsCoverage, omCoverage, imageFormat);
        }
    }

    private void plotRegionCoverage(String outputHtsImg, String outputOmImg, String outputImg, String title, boolean singleImage,
                                    SamplingType samplingType, PlotType plotType, List<CoverageInfo> htsCoverage, CoverageInfo omCoverage, ImageFormat imageFormat) throws Exception {
        CoveragePlot coveragePlot = createCoveragePlot(plotType);

        if (singleImage) {
            htsCoverage.add(omCoverage);
            coveragePlot.plotCoverage(title, "Position", "Coverage", outputImg, samplingType, htsCoverage, imageFormat);
        }
        else {
            if (htsCoverage != null)
                coveragePlot.plotCoverage(title, "Position", "Coverage", outputHtsImg, samplingType, htsCoverage, imageFormat);

            if (omCoverage != null)
                coveragePlot.plotCoverage(title, "Position", "Coverage", outputOmImg, samplingType, Collections.singletonList(omCoverage), imageFormat);
        }
    }

    private List<CoverageInfo> getCoverageInfoHts(String[] bams, ChromosomeRegion region, int threads, int samplingSize) throws Exception {
        return getCoverageInfoHts(bams, Collections.singletonList(region), threads, samplingSize).get(region);
    }

    private Map<ChromosomeRegion, List<CoverageInfo>> getCoverageInfoHts(String[] bams, List<ChromosomeRegion> regions, int threads, int samplingSize) throws Exception {
        if (bams.length == 0 || regions == null || regions.size() == 0)
            return Collections.emptyMap();

        Map<ChromosomeRegion, List<CoverageInfo>> coverages = new HashMap<>();

        for (String bam : bams) {
            String bai = bam + ".bai";

            try (CoverageCalculator coverageCalculator = threads == 1
                    ? new BamCoverageCalculatorST(bam, bai) : new BamCoverageCalculatorMT(bam, bai, threads)) {
                coverageCalculator.open();

                int counter = 1;
                for (ChromosomeRegion region : regions) {
                    List<CoverageInfo> coverageInfos = coverages.get(region);
                    if (coverageInfos == null) {
                        coverageInfos = new ArrayList<>();
                        coverages.put(region, coverageInfos);
                    }

                    log.info(String.format("Calculating coverage for: %s - %s - %s... %d/%d\n", bam, region.getName(), region, counter++, regions.size()));

                    CoverageInfo coverageInfo = coverageCalculator.getIntervalCoverage(region.getChromosome(), region.getStart(), region.getEnd());
                    coverageInfo.setSamplingSize(samplingSize);
                    coverageInfo.setColor(Color.RED.getRGB());
                    coverageInfo.setName("HTS - " + FilenameUtils.removeExtension(new File(bam).getName()));

                    coverageInfos.add(coverageInfo);
                }
            }
        }

        return coverages;
    }

    private CoverageInfo getCoverageInfoOm(String cmapRef, String cmapQry, String xmap, ChromosomeRegion region, int samplingSize) throws Exception {
        return getCoverageInfoOm(cmapRef, cmapQry, xmap, Collections.singletonList(region), samplingSize).get(region);
    }

    private Map<ChromosomeRegion, CoverageInfo> getCoverageInfoOm(String cmapRef, String cmapQry, String xmap, List<ChromosomeRegion> regions, int samplingSize) throws Exception {
        if (StringUtils.isBlank(cmapRef) || StringUtils.isBlank(cmapQry) || StringUtils.isBlank(xmap) || regions == null || regions.size() == 0)
            return Collections.emptyMap();

        try (CoverageCalculator coverageCalculator = new BionanoCoverageCalculator(cmapRef, cmapQry, xmap)) {
            coverageCalculator.open();

            Map<ChromosomeRegion, CoverageInfo> coverageInfos = new HashMap<>();
            int counter = 1;
            for (ChromosomeRegion region : regions) {
                log.info(String.format("Calculating coverage for: %s - %s... %d/%d\n", region.getName(), region, counter++, regions.size()));

                CoverageInfo coverageInfo = coverageCalculator.getIntervalCoverage(region.getChromosome(), region.getStart(), region.getEnd());
                coverageInfo.setSamplingSize(samplingSize);
                coverageInfo.setColor(Color.BLUE.getRGB());
                coverageInfo.setName("OM");

                coverageInfos.put(region, coverageInfo);
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
        log.error(msg);
    }
    
    private void exitError(String message) {
        if (StringUtils.isNoneBlank(message))
            log.error(message);
        
        System.exit(1);
    }
}
