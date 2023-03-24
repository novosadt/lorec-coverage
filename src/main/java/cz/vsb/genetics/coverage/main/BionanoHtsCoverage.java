package cz.vsb.genetics.coverage.main;

import org.apache.commons.cli.*;

import java.util.Properties;

public class BionanoHtsCoverage {
    private static final String ARG_BIONANO_CMAP_REF = "bionano_cmap_ref";
    private static final String ARG_BIONANO_CMAP_QRY = "bionano_cmap_qry";
    private static final String ARG_BIONANO_XMAP = "bionano_xmap";
    private static final String ARG_HTS_BAM = "hts_bam";
    private static final String ARG_HTS_BAI = "hts_bai";
    private static final String ARG_THREADS = "threads";
    private static final String ARG_REGION = "region";
    private static final String ARG_REGION_FILE = "region_file";
    private static final String ARG_OUTPUT_CSV = "output_csv";
    private static final String ARG_OUTPUT_IMG = "output_img";

    public static void main(String[] args) {
        CommandLine cmd = getCommandLine(args);

        String xmap = cmd.getOptionValue(ARG_BIONANO_XMAP);
        String cmapQuery = cmd.getOptionValue(ARG_BIONANO_CMAP_QRY);
        String cmapReference = cmd.getOptionValue(ARG_BIONANO_CMAP_REF);
        String bam = cmd.getOptionValue(ARG_HTS_BAM);
        String bai = cmd.getOptionValue(ARG_HTS_BAI);
        int threads = cmd.hasOption(ARG_THREADS) ? Integer.valueOf(cmd.getOptionValue(ARG_THREADS)) : 1;
        String region = cmd.hasOption(ARG_REGION) ? cmd.getOptionValue(ARG_REGION) : null;
        String regionFile = cmd.hasOption(ARG_REGION_FILE) ? cmd.getOptionValue(ARG_REGION_FILE) : null;
        String outputCsv = cmd.getOptionValue(ARG_OUTPUT_CSV);
        String outputImg = cmd.getOptionValue(ARG_OUTPUT_IMG);

    }

    private static CommandLine getCommandLine(String[] args) {
        Options options = new Options();

        Option bionanoCmapQry = new Option("cmap_q", ARG_BIONANO_CMAP_QRY, true, "bionano cmap query file");
        bionanoCmapQry.setRequired(true);
        bionanoCmapQry.setArgName("cmap file");
        bionanoCmapQry.setType(String.class);
        options.addOption(bionanoCmapQry);

        Option bionanoCmapRef = new Option("cmap_r", ARG_BIONANO_CMAP_REF, true, "bionano cmap reference file");
        bionanoCmapRef.setRequired(true);
        bionanoCmapRef.setArgName("cmap file");
        bionanoCmapRef.setType(String.class);
        options.addOption(bionanoCmapRef);

        Option bionanoXmap = new Option("xmap", ARG_BIONANO_XMAP, true, "bionano xmap file");
        bionanoXmap.setRequired(true);
        bionanoXmap.setArgName("cmap file");
        bionanoXmap.setType(String.class);
        options.addOption(bionanoXmap);

        Option htsBam = new Option("bam", ARG_HTS_BAM, true, "hts bam file");
        htsBam.setRequired(true);
        htsBam.setArgName("bam file");
        htsBam.setType(String.class);
        options.addOption(htsBam);

        Option htsBai = new Option("bai", ARG_HTS_BAI, true, "hts bam index file");
        htsBai.setRequired(true);
        htsBai.setArgName("bai file");
        htsBai.setType(String.class);
        options.addOption(htsBai);

        Option threads = new Option("t", ARG_THREADS, true, "number of threads for parallel processing");
        threads.setRequired(false);
        threads.setArgName("threads");
        threads.setType(Integer.class);
        options.addOption(threads);

        Option region = new Option("r", ARG_REGION, true, "chromosomal region of interest (e.g. chr1 or chr1:1-1000)");
        region.setRequired(false);
        region.setArgName("chromosomal region");
        region.setType(String.class);
        options.addOption(region);

        Option regionFile = new Option("rf", ARG_REGION_FILE, true, "file with chromosomal regions of interest (e.g. chr1 or chr1:1-1000) - one per line");
        regionFile.setRequired(false);
        regionFile.setArgName("chromosomal regions file");
        regionFile.setType(String.class);
        options.addOption(regionFile);

        Option outputCsv = new Option("csv", ARG_OUTPUT_CSV, true, "output result file - CSV");
        outputCsv.setRequired(true);
        outputCsv.setArgName("output csv file");
        outputCsv.setType(String.class);
        options.addOption(outputCsv);

        Option outputImg = new Option("img", ARG_OUTPUT_IMG, true, "output result file - Image");
        outputImg.setRequired(true);
        outputImg.setArgName("output png file");
        outputImg.setType(String.class);
        options.addOption(outputImg);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("\nSVC - Bionano Genomics (OM) and HTS coverage information tool, v" + version() + "\n");
            System.out.println(e.getMessage());
            System.out.println();
            formatter.printHelp(
                    300,
                    "\njava -jar om-hts-coverage.jar ",
                    "\noptions:",
                    options,
                    "\nTomas Novosad, VSB-TU Ostrava, 2022" +
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


}
