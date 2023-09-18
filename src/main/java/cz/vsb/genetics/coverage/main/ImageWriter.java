package cz.vsb.genetics.coverage.main;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.DOMImplementation;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class ImageWriter {
    public static void saveImage(String outputFile, JFreeChart chart, int width, int height, ImageFormat format) throws Exception {
        switch (format) {
            case JPG: saveAsJPEG(outputFile, chart, width, height); break;
            case PNG: saveAsPNG(outputFile, chart, width, height); break;
            case PDF: saveAsPDF(outputFile, chart, width, height); break;
            case SVG: saveAsSVG(outputFile, chart, width, height);
        }
    }

    private static void saveAsJPEG(String outputFile, JFreeChart chart, int width, int height) throws Exception {
        File lineChart = new File(outputFile);
        ChartUtils.saveChartAsJPEG(lineChart , chart, width ,height);
    }

    private static void saveAsPNG(String outputFile, JFreeChart chart, int width, int height) throws Exception {
        File lineChart = new File(outputFile);
        ChartUtils.saveChartAsPNG(lineChart , chart, width ,height);
    }

    private static void saveAsPDF(String outputFile, JFreeChart chart, int width, int height) throws Exception {
        Rectangle pagesize = new Rectangle(width, height);
        Document document = new Document(pagesize, 50, 50, 50, 50);
        PdfWriter writer = PdfWriter.getInstance( document, new FileOutputStream(outputFile) );
        document.open();
        PdfContentByte content = writer.getDirectContent();
        PdfTemplate template = content.createTemplate(width, height);
        Graphics2D graphics2D = template.createGraphics(width, height, new DefaultFontMapper() );
        Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, width, height);
        chart.draw(graphics2D, rectangle2D);
        graphics2D.dispose();
        content.addTemplate(template, 0, 0);
        document.close();
    }

    private static void saveAsSVG(String outputFile, JFreeChart chart, int width, int height) throws Exception {
        DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();
        org.w3c.dom.Document document = domImpl.createDocument(null, "svg", null);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, width, height);
        chart.draw(svgGenerator, rectangle2D);

        Writer out = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
        svgGenerator.stream(out, true);
        out.close();
    }
}
