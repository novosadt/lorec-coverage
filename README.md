# LoReC - Coverage: long read, high-throughput and optical mapping coverage comparison tool 

This software tool is designed for the visual comparison of sequencing data produced by various whole genome sequencing 
technologies, based on coverage calculation. It can also calculate various coverage statistics for the given input data.

The tool generates coverage charts calculated from either the standard Sequence/Binary Alignment and Map format (SAM/BAM) 
with an index file (BAI), or from Bionano Genomics optical mapping technology. In the case of optical mapping, coverage 
information is calculated from the location of label sites within a genome map (CMAP) and through cross-comparison 
between the query and reference CMAP (XMAP).

This tool is available as a platform-independent CLI application and is part of the Long-Read-Checker (LoReC) toolkit.


## Current stable version
<b>Current stable version is 1.0 - branch 1.0.</b> Main/master branch is dedicated for further development and should not be used in production environment.

## Requirements
Java Runtime Environment 8 or higher.

## Command line arguments
| Parameter | Long                    | Type     | Default   | Description                                                                                                   |
|-----------|-------------------------|----------|-----------|---------------------------------------------------------------------------------------------------------------|
| -cmap_q   | --bionano_cmap_qry      | String   |           | Bionano Genomics analysis pipeline result cmap query file path.                                               |
| -cmap_r   | --bionano_cmap_ref      | String   |           | Bionano Genomics analysis pipeline result cmap reference file path.                                           |
| -xmap     | --bionano_xmap          | String   |           | Bionano Genomics analysis pipeline result xmap file path.                                                     |
| -bss      | --bionano_sampling_step | Integer  | 10        | Number of marks used for Bionano optical maps sampling.                                                       |
| -bam      | --hts_bam               | String   |           | Binary alignment and map files path separated by semicolon. BAM index file (BAI) must right next to BAM file. |
| -hss      | --hts_sampling_step     | Integer  | 100       | Region size (number of bases) used for HTS (BAM) sampling.                                                    |
| -t        | --threads               | Integer  | 1         | Number of threads used for parallel coverage calculation.                                                     |
| -mq       | --mapping_quality       | Integer  | 0         | Minimum read mapping quality filter - BAM only.                                                               |
| -hcl      | --coverage_limit_hts    | Integer  |           | Coverage limit for plotting of HTS data (BAM) (maximum y axis value).                                         |
| -bcl      | --coverage_limit_om     | Integer  |           | Coverage limit for plotting Bionano optical maps (maximum y axis value).                                      |
| -r        | --region                | String   |           | Chromosomal region of interest (e.g. chr1:1-1000).                                                            |
| -rf       | --region                | String   |           | File with chromosomal regions of interest in format: contig_name region (e.g. TP53 chr17:7571739-7590808)     |
| -ti       | --title                 | String   |           | Plot/Image title.                                                                                             |
| -st       | --sampling_type         | String   | random    | Sampling type [random \| mean \| median \| none].                                                             |
| -pt       | --plot_type             | String   | histogram | Plot/Chart type [histogram \| line \|spline].                                                                 |
| -si       | --single_image          | String   |           | Whether to plot HTS and OM coverage information in single image.                                              |
| -stats    | --statistics            | String   |           | File path for statistics calculated for region file (--rf) (min, q1, median, q3, max)                         |
| -img      | --output_img            | String   |           | Output joint OM/HGS coverage plot file path.                                                                  |
| -img_hts  | --output_hts_img        | String   |           | Output HTS coverage plot file path.                                                                           |
| -img_om   | --output_om_img         | String   |           | Output OM coverage plot file path.                                                                            |
| -od       | --output_dir            | String   |           | Output directory for OM/WGS coverage plots.                                                                   |
| -sn       | --sample_name           | String   |           | Sample name for prefixing OM/WGS coverage plot titles and image names.                                        |
| -of       | --output_format         | String   | png       | Output image format [jpg \| png \| pdf \| svg].                                                               |             |


## Example of usage
Some basic example usage of structural variant comparator follows. More detailed usage with sample data and results are presented in sample package in <b>./example</b> directory in this repository. Each directory in sample package contains README.txt file where can be found detailed description of each file. Runnable binary version of the application is presented in <b>./bin</b> directory of the repository.

### Basic usage
In basic setup, application creates coverage plot for given sequencing input files. No filters are applied here.

```console
java -jar lorec-coverage.jar -cmap_q /home/lorec/coverage/om_query.cmap -cmap_r /home/lorec/coverage/om_ref.cmap -xmap /home/lorec/coverage/om.xmap -title "Optical Map" -img_om /home/lorec/coverage/om.png
java -jar lorec-coverage.jar -bam /home/lorec/coverage/hts.bam -title "High-Througput Sequeincing" -img_hts /home/lorec/coverage/hts.png
```

### Statistics
Following command calculates minimum, Q1, Median, Q3, Maximum coverage statistics for given sequencing input data.

```consolev
java -jar lorec-coverage.jar -bam /home/lorec/coverage/hts.bam -stats /home/lorec/coverage/hts-statistics.png 
```

### Multiple coverage plot based on region file
Following command creates coverage plots based on region file and save images with given prefix (sample name) to specified output folder

```console
java -jar lorec-coverage.jar -bam /home/lorec/coverage/hts.bam -title "High-Througput Sequeincing" -sn "james_smith" -rf /home/lorec/coverage/james_smith_regions.txt -od /home/lorec/coverage/hts 
```

## Contact
If you have any problem or questions about the software tool, please contact us.

Tomáš Novosád (tomas.novosad@vsb.cz)

Jakub Savara (jakub.savara@vsb.cz)

