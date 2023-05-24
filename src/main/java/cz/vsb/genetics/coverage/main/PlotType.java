package cz.vsb.genetics.coverage.main;

public enum PlotType {
    HISTOGRAM,
    LINE,
    SPLINE,

    ;

    public static PlotType of(String value) {
        if (value == null)
            return HISTOGRAM;

        value = value.trim().toLowerCase();

        switch (value) {
            case "histogram" : return HISTOGRAM;
            case "line" : return LINE;
            case "spline" : return SPLINE;
            default: return HISTOGRAM;
        }
    }
}
