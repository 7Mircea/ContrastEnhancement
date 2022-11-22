package sample.controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;

import static sample.utils.HistogramDraw.displayHistogram;
import static sample.utils.Utils.changeBufferedImageToJavaFxImage;

public class BaseController {
    @FXML
    protected ImageView image_selected;
    @FXML
    protected ImageView image_selected_hist;

    public void setImage(BufferedImage image) {
        image_selected.setImage(changeBufferedImageToJavaFxImage(image));
    }
    public void setHistogram(BufferedImage image) {
        displayHistogram(image,image_selected_hist);
    }
}
