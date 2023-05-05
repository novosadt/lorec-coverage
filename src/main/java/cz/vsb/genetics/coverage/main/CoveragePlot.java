package cz.vsb.genetics.coverage.main;

import cz.vsb.genetics.coverage.CoverageInfo;

public interface CoveragePlot {
    void plotCoverage(String title, String xLabel, String yLabel, String outputFile,
                      SamplingType samplingType, CoverageInfo... coverageInfos) throws Exception;
}
