package cz.vsb.genetics.coverage.main;

import cz.vsb.genetics.common.ChromosomeRegion;

import java.util.List;

public class RegionAnnotation {
    private ChromosomeRegion region;
    private List<String> annotations;

    public ChromosomeRegion getRegion() {
        return region;
    }

    public void setRegion(ChromosomeRegion region) {
        this.region = region;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }
}
