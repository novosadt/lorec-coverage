package cz.vsb.genetics.coverage.main;

import java.util.HashMap;
import java.util.Map;

public enum ImageFormat {
    JPG("jpg"),
    PNG("png"),
    PDF("pdf"),
    SVG("svg"),
    ;

    public final String value;

    private static final Map<String, ImageFormat> map = new HashMap<>();

    static {
        for (ImageFormat item : ImageFormat.values())
            map.put(item.value, item);
    }

    ImageFormat(String value) {
        this.value = value;
    }

    public static ImageFormat of(String value) {
        ImageFormat format = map.get(value);

        return format == null ? PNG : format;
    }
}
