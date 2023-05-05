package cz.vsb.genetics.coverage.main;

public enum SamplingType {
    NONE,
    RANDOM,
    MEAN,
    MEDIAN

    ;

    public static SamplingType of(String value) {
        if (value == null)
            return NONE;

        value = value.trim().toLowerCase();

        switch (value) {
            case "none" : return NONE;
            case "random" : return RANDOM;
            case "mean" : return MEAN;
            case "median" : return MEDIAN;
            default: return RANDOM;
        }
    }
}
