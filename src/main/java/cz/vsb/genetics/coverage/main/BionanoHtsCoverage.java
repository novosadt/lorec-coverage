package cz.vsb.genetics.coverage.main;

import cz.vsb.genetics.common.ChromosomeRegion;
import cz.vsb.genetics.coverage.CoverageCalculator;
import cz.vsb.genetics.coverage.CoverageInfo;
import cz.vsb.genetics.ngs.coverage.BamCoverageCalculatorMT;
import cz.vsb.genetics.ngs.coverage.BamCoverageCalculatorST;
import cz.vsb.genetics.om.coverage.BionanoCoverageCalculator;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

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
    private static final String ARG_TITLE = "title";
    private static final String ARG_SAMPLING_TYPE = "sampling_type";
    private static final String ARG_PLOT_TYPE = "plot_type";

    private static final String ARG_SINGLE_IMAGE = "single_image";
    private static final String ARG_COVERAGE_LIMIT = "coverage_limit";
    private static final String ARG_OUTPUT_CSV = "output_csv";
    private static final String ARG_OUTPUT_HTS_IMG = "output_hts_img";
    private static final String ARG_OUTPUT_OM_IMG = "output_om_img";
    private static final String ARG_OUTPUT_IMG = "output_img";

    public static void main(String[] args) {
        CommandLine cmd = getCommandLine(args);

        String xmap = cmd.hasOption(ARG_BIONANO_XMAP) ? cmd.getOptionValue(ARG_BIONANO_XMAP) : null;
        String cmapQuery = cmd.hasOption(ARG_BIONANO_CMAP_QRY) ? cmd.getOptionValue(ARG_BIONANO_CMAP_QRY) : null;
        String cmapReference = cmd.hasOption(ARG_BIONANO_CMAP_REF) ? cmd.getOptionValue(ARG_BIONANO_CMAP_REF) : null;
        String bam = cmd.hasOption(ARG_HTS_BAM) ? cmd.getOptionValue(ARG_HTS_BAM) : null;
        String bai = cmd.hasOption(ARG_HTS_BAI) ? cmd.getOptionValue(ARG_HTS_BAI) : null;
        int threads = cmd.hasOption(ARG_THREADS) ? Integer.valueOf(cmd.getOptionValue(ARG_THREADS)) : 1;
        int htsSamplingStep = cmd.hasOption(ARG_HTS_SAMPLING_STEP) ? Integer.valueOf(cmd.getOptionValue(ARG_HTS_SAMPLING_STEP)) : 100;
        int bionanoSamplingStep = cmd.hasOption(ARG_BIONANO_SAMPLING_STEP) ? Integer.valueOf(cmd.getOptionValue(ARG_BIONANO_SAMPLING_STEP)) : 10;
        String region = cmd.getOptionValue(ARG_REGION);
        String title = cmd.hasOption(ARG_TITLE) ? cmd.getOptionValue(ARG_TITLE) : "";
        String outputCsv = cmd.hasOption(ARG_OUTPUT_CSV) ? cmd.getOptionValue(ARG_OUTPUT_CSV) : null;
        String outputHtsImg = cmd.hasOption(ARG_OUTPUT_HTS_IMG) ? cmd.getOptionValue(ARG_OUTPUT_HTS_IMG) : null;
        String outputOmImg = cmd.hasOption(ARG_OUTPUT_OM_IMG) ? cmd.getOptionValue(ARG_OUTPUT_OM_IMG) : null;
        String outputImg = cmd.hasOption(ARG_OUTPUT_IMG) ? cmd.getOptionValue(ARG_OUTPUT_IMG) : null;
        boolean singleImage = cmd.hasOption(ARG_SINGLE_IMAGE) ? true : false;
        SamplingType samplingType = SamplingType.of(cmd.getOptionValue(ARG_SAMPLING_TYPE));
        PlotType plotType = PlotType.of(cmd.getOptionValue(ARG_PLOT_TYPE));

        try {
            CoverageInfo htsCoverage = getCoverageInfoHts(bam, bai, ChromosomeRegion.valueOf(region), threads, htsSamplingStep);
            CoverageInfo omCoverage = getCoverageInfoOm(cmapReference, cmapQuery, xmap, ChromosomeRegion.valueOf(region), bionanoSamplingStep);

            if (cmd.hasOption(ARG_COVERAGE_LIMIT)) {
                int coverageLimit = Integer.valueOf(cmd.getOptionValue(ARG_COVERAGE_LIMIT));
                if (htsCoverage != null)
                    htsCoverage.setCoverageLimit(coverageLimit);
                if (omCoverage != null)
                    omCoverage.setCoverageLimit(coverageLimit);
            }

            plotCoverage(htsCoverage, omCoverage, outputHtsImg, outputOmImg, outputImg, title, singleImage, samplingType, plotType);
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

        Option bionanoSamplingStep = new Option("hss", ARG_BIONANO_SAMPLING_STEP, true, "no. of marks used for Bionano optical maps sampling - default 10");
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
        region.setRequired(true);
        region.setArgName("chromosomal region");
        region.setType(String.class);
        options.addOption(region);

        Option title = new Option("ti", ARG_TITLE, true, "plot title");
        title.setArgName("title");
        title.setType(String.class);
        options.addOption(title);

        Option samplingType = new Option("st", ARG_SAMPLING_TYPE, true, "sampling type [random|mean|median|none] - default random");
        samplingType.setArgName("sampling type");
        samplingType.setType(String.class);
        options.addOption(samplingType);

        Option plotType = new Option("pt", ARG_PLOT_TYPE, true, "plot type [histogram|line] - default histogram");
        plotType.setArgName("plot type");
        plotType.setType(String.class);
        options.addOption(plotType);

        Option singleImage = new Option("si", ARG_SINGLE_IMAGE, false, "whether to plot HTS and OM in single image");
        singleImage.setArgName("single image");
        options.addOption(singleImage);

        Option outputCsv = new Option("csv", ARG_OUTPUT_CSV, true, "output result file - CSV");
        outputCsv.setArgName("csv file");
        outputCsv.setType(String.class);
        options.addOption(outputCsv);

        Option outputHtsImg = new Option("img_hts", ARG_OUTPUT_HTS_IMG, true, "output HTS coverage plot");
        outputHtsImg.setArgName("jpg file");
        outputHtsImg.setType(String.class);
        options.addOption(outputHtsImg);

        Option outputOmImg = new Option("img_om", ARG_OUTPUT_OM_IMG, true, "output OM coverage plot");
        outputOmImg.setArgName("jpg file");
        outputOmImg.setType(String.class);
        options.addOption(outputOmImg);

        Option outputImg = new Option("img", ARG_OUTPUT_IMG, true, "output OM/WGS coverage plot");
        outputImg.setArgName("jpg file");
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

    private static CoverageInfo getCoverageInfoHts(String bam, String bai, ChromosomeRegion region, int threads, int samplingSize) throws Exception {
        if (StringUtils.isBlank(bam) || StringUtils.isBlank(bai) || region == null)
            return null;

        try (CoverageCalculator coverageCalculator = threads == 1
                ? new BamCoverageCalculatorST(bam, bai) : new BamCoverageCalculatorMT(bam, bai, threads)) {
            coverageCalculator.open();

            CoverageInfo coverageInfo = coverageCalculator.getIntervalCoverage(region.getChromosome(), region.getStart(), region.getEnd());
            coverageInfo.setSamplingSize(samplingSize);
            coverageInfo.setTitle("HTS");

            return coverageInfo;
        }
    }

    private static CoverageInfo getCoverageInfoOm(String cmapRef, String cmapQry, String xmap, ChromosomeRegion region, int samplingSize) throws Exception {
        if (StringUtils.isBlank(cmapRef) || StringUtils.isBlank(cmapQry) || StringUtils.isBlank(xmap) || region == null)
            return null;

        try (CoverageCalculator coverageCalculator = new BionanoCoverageCalculator(cmapRef, cmapQry, xmap)) {
            coverageCalculator.open();

            CoverageInfo coverageInfo = coverageCalculator.getIntervalCoverage(region.getChromosome(), region.getStart(), region.getEnd());
            coverageInfo.setSamplingSize(samplingSize);
            coverageInfo.setTitle("OM");

            return coverageInfo;
        }
    }

    private static void plotCoverage(CoverageInfo htsCoverage, CoverageInfo omCoverage, String htsImgFile, String omImgFile, String imgFile,
                                     String title, boolean singleImage, SamplingType samplingType, PlotType plotType) throws Exception {
        if (htsCoverage == null && omCoverage == null) {
            exitError();
        }

        CoveragePlot coveragePlot;

        switch (plotType) {
            case LINE:
                coveragePlot = new CoveragePlotXYStepChart();
                break;
            default:
                coveragePlot = new CoveragePlotHistogram();
                break;
        }

        if (singleImage) {
            if (StringUtils.isBlank(imgFile))
                exitError();

            coveragePlot.plotCoverage(title, "Position", "Coverage", imgFile, samplingType, htsCoverage, omCoverage);
        }
        else {
            if (htsCoverage != null)
                coveragePlot.plotCoverage(title, "Position", "Coverage", htsImgFile, samplingType, htsCoverage);

            if (omCoverage != null)
                coveragePlot.plotCoverage(title, "Position", "Coverage", omImgFile, samplingType, omCoverage);
        }
    }

    private static void exitError() {
        System.out.println("Nothing to plot - probably missing (or bad) some mandatory parameters. Exiting...");
        System.exit(1);
    }
}
