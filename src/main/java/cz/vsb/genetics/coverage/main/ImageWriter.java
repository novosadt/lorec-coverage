/*
 * Copyright (C) 2025  Tomas Novosad
 * VSB-TUO, Faculty of Electrical Engineering and Computer Science
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


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
import java.nio.charset.StandardCharsets;

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

        Writer out = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
        svgGenerator.stream(out, true);
        out.close();
    }
}
