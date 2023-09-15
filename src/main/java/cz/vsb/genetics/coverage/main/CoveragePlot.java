package cz.vsb.genetics.coverage.main;

import cz.vsb.genetics.coverage.CoverageInfo;

import java.util.List;

public interface CoveragePlot {
    void plotCoverage(String title, String xLabel, String yLabel, String outputFile,
                      SamplingType samplingType, List<CoverageInfo> coverageInfos) throws Exception;
}
