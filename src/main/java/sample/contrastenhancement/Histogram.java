package sample.contrastenhancement;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Histogram {
    private final Map<Byte, Long> hist;
    //    private BufferedImage image;
    private final int width;
    private final int height;
    private final int processors = Runtime.getRuntime().availableProcessors();
    private final byte[] arr;

//    public Histogram() {
//        hist = new HashMap<>(256);
//        image = new BufferedImage(512,512, TYPE_BYTE_GRAY);
//    }
//
//    public Histogram(int grayLevels) {
//        hist = new HashMap<>(grayLevels);
//        image = new BufferedImage(512,512,TYPE_BYTE_GRAY);
//    }
//
//    public Histogram(BufferedImage image) {
//        if (image.getType() != TYPE_BYTE_GRAY)
//            this.image = new BufferedImage(512,512,TYPE_BYTE_GRAY);
//        else
//            this.image = image;
//        hist = new HashMap<>(256);
//        createHistogram();
//    }

    public Histogram(byte[] arr, int height, int width) {
        this.arr = arr;
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
            threads[i] = new Task(i, arr, i * chunkSize, (i + 1) * chunkSize - 1, height, width, this);
        }
        int lastThreadIndex = threads.length - 1;
        threads[lastThreadIndex] = new Task(lastThreadIndex, arr, lastThreadIndex * chunkSize, height - 1, height, width, this);
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
    }
}
