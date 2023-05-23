package cz.vsb.genetics.coverage.main;

public enum PlotType {
    HISTOGRAM,
    LINE,

    ;

    public static PlotType of(String value) {
        if (value == null)
            return HISTOGRAM;

        value = value.trim().toLowerCase();

        switch (value) {
            case "histogram" : return HISTOGRAM;
            case "line" : return LINE;
            default: return HISTOGRAM;
        }
    }
}
