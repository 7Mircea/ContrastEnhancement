package sample.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public class HistogramDraw {

    public static JFreeChart createGraphicHistogram(BufferedImage image) {
        // dataset
        HistogramDataset dataset = new HistogramDataset();
        final int BINS = 256;
        Raster raster = image.getRaster();
        final int w = image.getWidth();
        final int h = image.getHeight();
        double[] r = new double[w * h];
        r = raster.getSamples(0, 0, w, h, 0, r);
        dataset.addSeries("gray", r, BINS);
        return ChartFactory.createHistogram("Histogram", "Value",
                "Count", dataset, PlotOrientation.VERTICAL, true, true, false);
    }

}
