package sample.utils;

import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.Map;

import static sample.utils.Constants.resolutionHeight;
import static sample.utils.Constants.resolutionWidth;
import static sample.utils.Utils.changeBufferedImageToJavaFxImage;

public class HistogramDraw {
    public static void displayHistogram(BufferedImage image, ImageView imageView) {
        JFreeChart chart = createGraphicHistogram(image);
        BufferedImage chartImage = chart.createBufferedImage(image.getWidth(), image.getHeight());
        imageView.setImage(changeBufferedImageToJavaFxImage(chartImage));
    }

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

    public void createAreaChart(String title, double xLow, double xHigh, boolean xAutorange, double yLow, double yHigh, boolean yAutorange, Map<Short, Double> data) {
        Stage stageGray = new Stage();
        stageGray.setTitle(title);
        AreaChart<Number, Number> areaChart = null;
        if (!xAutorange && !yAutorange)
            areaChart = new AreaChart<>(new NumberAxis(xLow, xHigh, 1), new NumberAxis(yLow, yHigh, 0.1));
        else if (!xAutorange && yAutorange)
            areaChart = new AreaChart<>(new NumberAxis(xLow, xHigh, 1), new NumberAxis());
        else if (xAutorange && !yAutorange)
            areaChart = new AreaChart<>(new NumberAxis(xLow, xHigh, 1), new NumberAxis(yLow, yHigh, 1));
        else
            areaChart = new AreaChart<>(new NumberAxis(), new NumberAxis());
        XYChart.Series<Number,Number> series = new XYChart.Series<>();
        for (Map.Entry<Short,Double> el:data.entrySet())
            series.getData().add(new XYChart.Data<>(el.getKey(),el.getValue()));
        areaChart.getData().addAll(series);
        stageGray.setScene(new Scene(areaChart, resolutionWidth, resolutionHeight));
        stageGray.show();
    }

}
