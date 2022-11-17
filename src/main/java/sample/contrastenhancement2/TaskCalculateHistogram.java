package sample.contrastenhancement2;



import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

import java.util.Map;

public class TaskCalculateHistogram extends Thread {

    Image image;
    int startRow;
    int lastRow;
    private Histogram hist;

    int width;
    int height;

    int threadNr;

    /**
     * @param image    image to be calculated histogram from
     * @param startRow first row to be used for computation of histogram
     * @param lastRow  last row to be used for computation of histogram
     */
    public TaskCalculateHistogram(int threadNr, Image image, int startRow, int lastRow, int height, int width, final Histogram hist) {
        if (startRow > lastRow || startRow > height || lastRow > height){
            return;
        }
        this.image = image;
        this.startRow = startRow;
        this.lastRow = lastRow;
        this.hist = hist;
        this.width = width;
        this.height = height;
        this.threadNr  = threadNr;
    }

    @Override
    public void run() {
        PixelReader reader= image.getPixelReader();
        for (int row = startRow; row <= lastRow; ++row) {
            for (int column = 0; column < width; ++column) {
                byte grayLevel = (byte) reader.getArgb(column,row);
                long val = 0;
                synchronized (hist) {
                    Map<Byte,Long> localHist = hist.getHist();
                    if (localHist.containsKey(grayLevel))
                        val = localHist.get(grayLevel);
                    localHist.put(grayLevel, val + 1);
                }
            }

        }
    }

    @Override
    public String toString() {
        return "Task{" +
                ", startRow=" + startRow +
                ", lastRow=" + lastRow +
                ", width=" + width +
                ", height=" + height +
                ", threadNr=" + threadNr +
                '}';
    }
}
