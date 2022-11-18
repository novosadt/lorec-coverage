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
    private static final String ARG_OUTPUT_CSV = "output_csv";
    private static final String ARG_OUTPUT_IMG = "output_img";

    public static void main(String[] args) {

    }

    private static CommandLine getCommandLine(String[] args) {
        Options options = new Options();

        Option bionanoCmapQry = new Option("bcq", ARG_BIONANO_CMAP_QRY, true, "bionano cmap query file");
        bionanoCmapQry.setRequired(true);
        bionanoCmapQry.setArgName("cmap file");
        bionanoCmapQry.setType(String.class);
        options.addOption(bionanoCmapQry);

        Option bionanoCmapRef = new Option("bcr", ARG_BIONANO_CMAP_REF, true, "bionano cmap reference file");
        bionanoCmapRef.setRequired(true);
        bionanoCmapRef.setArgName("cmap file");
        bionanoCmapRef.setType(String.class);
        options.addOption(bionanoCmapRef);

        Option bionanoXmap = new Option("bx", ARG_BIONANO_XMAP, true, "bionano xmap file");
        bionanoXmap.setRequired(true);
        bionanoXmap.setArgName("cmap file");
        bionanoXmap.setType(String.class);
        options.addOption(bionanoXmap);





        Option outputCsv = new Option("ocsv", ARG_OUTPUT_CSV, true, "output result file - CSV");
        outputCsv.setRequired(true);
        outputCsv.setArgName("csv file");
        outputCsv.setType(String.class);
        options.addOption(outputCsv);

        Option outputImg = new Option("oimg", ARG_OUTPUT_IMG, true, "output result file - Image");
        outputImg.setRequired(true);
        outputImg.setArgName("png file");
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
