package sample.contrastenhancement2;

import javafx.scene.image.Image;
import lombok.Getter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Getter
public class Histogram {
    private final Map<Byte, Long> hist;
    private final Map<Byte, Double> pdf = new HashMap<>(256, 1); //pdf - probabilty density function
    private final Map<Byte, Double> cdf = new HashMap<>(256, 1); //pdf - probabilty density function
    //    private BufferedImage image;
    private final int width;
    private final int height;
    private final int processors = Runtime.getRuntime().availableProcessors();
    private final Image image;

    public Histogram(Image image, int height, int width) {
        this.image = image;
        hist = new HashMap<>(256, 1);//loadFactor(second argument) - represents when the HashMap allocate
        //more space and has values between 0 and 1. If you put 1 it reallocates space when the size(number of actual buckets)
        //is the same with the capacity(the space allocated for buckets).
        this.width = width;
        this.height = height;
        createHistogram();
    }

    private void createHistogram() {
        Thread[] threads = new Thread[processors];
        int chunkSize; //chunk - number of pixel rows used by one thread for computing the histogram
        chunkSize = height / processors;
        for (int i = 0; i < threads.length - 1; ++i) {
            threads[i] = new TaskCalculateHistogram(i, image, i * chunkSize, (i + 1) * chunkSize - 1, height, width, this);
        }
        int lastThreadIndex = threads.length - 1;
        threads[lastThreadIndex] = new TaskCalculateHistogram(lastThreadIndex, image, lastThreadIndex * chunkSize, height - 1, height, width, this);
        for (Thread thread : threads) {
            thread.start();
        }
        try {
            for (Thread thread : threads) {
                thread.join();//waits for this thread to die
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("histogram capacity" + hist.size());
    }

    private void calculatePDF() {
        long N = ((long) width) * height;
        for (byte i = 0; i < 255; ++i) {
            if (hist.containsKey(i))
                pdf.put(i, hist.get(i) / (double) N);
        }
    }

    /**
     * should be called after calling calculatePDF. Calculates Cumulative Density Function(CDF).
     */
    private void calculateCDF() {
        Iterator<Map.Entry<Byte, Double>> it = pdf.entrySet().iterator();
        Double value = 0D;
        while (it.hasNext()) {
            Map.Entry<Byte, Double> el = it.next();
            value += el.getValue();
            if (value > 1)
                value = 1D;
            cdf.put(el.getKey(), value);
        }
    }

    public final void calculateHE() {
        calculatePDF();
        calculateCDF();

    }
}
