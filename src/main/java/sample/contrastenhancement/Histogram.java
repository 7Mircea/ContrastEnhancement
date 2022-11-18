package sample.contrastenhancement;

import com.sun.istack.internal.NotNull;
import lombok.Getter;

import java.util.*;

@Getter
public class Histogram {
    private final SortedMap<Byte, Long> hist;
    private byte[] partitionThresholdPoints = null;//[0,pct,,pct2,final]
    private final List<SortedMap<Byte, Double>> pdf = new LinkedList<>(); //pdf - probabilty density function
    private final List<SortedMap<Byte, Double>> cdf = new LinkedList<>(); //cdf - cummulative density function
    //    private BufferedImage image;
    private final int width;
    private final int height;
    private final int processors = Runtime.getRuntime().availableProcessors();
    private final byte[] arr;

    public Histogram(@NotNull byte[] arr, int height, int width) {
        this.arr = arr;
        hist = new TreeMap<Byte, Long>(Byte::compareTo);//loadFactor(second argument) - represents when the HashMap allocate
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
            threads[i] = new TaskCalculateHistogram(i, arr, i * chunkSize, (i + 1) * chunkSize - 1, height, width, this);
        }
        int lastThreadIndex = threads.length - 1;
        threads[lastThreadIndex] = new TaskCalculateHistogram(lastThreadIndex, arr, lastThreadIndex * chunkSize, height - 1, height, width, this);
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

    /**
     * calculates probability density function(pdf) on selected partition if partition >= 1 or
     * on the entire histogram if partition equals -1
     */
    private void calculatePDF(byte partition, long numberOfPixelsInThisPartition) {
        if (partition != -1 && partition < 1) {
            System.out.println("attempt to calculate pdf on non-existing partition " + partition);
            return;
        }
        SortedMap<Byte, Double> localPDF = selectLocal(pdf, partition);
        if (partition == -1) {//calculate pdf for entire histogram
            for (byte key : hist.keySet()) {
                localPDF.put(key, hist.get(key) / (double) numberOfPixelsInThisPartition);
            }
            return;
        }
        short upperLimitGrayLevel = partitionThresholdPoints[partition];
        if (upperLimitGrayLevel == 127)
            upperLimitGrayLevel = 128;
        for (byte key : hist.keySet()) {//calculate pdf only on partition i
            if (key >= partitionThresholdPoints[partition - 1] && key < upperLimitGrayLevel) {
                localPDF.put(key, hist.get(key) / (double) numberOfPixelsInThisPartition);
            }
        }
    }

    private SortedMap<Byte, Double> selectLocal(List<SortedMap<Byte, Double>> list, byte partition) {
        return (partition != -1) ? list.get(partition - 1) : list.get(0);
    }

    private void calculatePDFOnPartition() {
        for (byte i = 1; i < partitionThresholdPoints.length; ++i) {
            calculatePDF(i, calculateNumberOfPixels(i));
        }
    }

    private long calculateNumberOfPixels(byte partition) {
        if (partition == -1) {
            return ((long) width) * height;
        }
        long nrOfPixels = 0L;

        short upperLimitGrayLevel = partitionThresholdPoints[partition];
        if (upperLimitGrayLevel == 127)
            upperLimitGrayLevel = 128;
        for (SortedMap.Entry<Byte, Long> element : hist.entrySet()) {
            //<grayLevel,nrOfPixelsWithThisGrayLevel>
            if (element.getKey() >= partitionThresholdPoints[partition - 1] && element.getKey() < upperLimitGrayLevel) {
                nrOfPixels += element.getValue();
            }
        }
        return nrOfPixels;
    }

    /**
     * should be called after calling calculatePDF. Calculates Cumulative Density Function(CDF).
     */
    private void calculateCDF(byte partition) {
        SortedMap<Byte, Double> localPDF = selectLocal(pdf, partition);
        SortedMap<Byte, Double> localCDF = selectLocal(cdf, partition);
        Double value = 0D;
        if (partition == -1) {
            for (SortedMap.Entry<Byte, Double> el : localPDF.entrySet()) {
                value += el.getValue();
                if (value > 1)
                    value = 1D;
                localCDF.put(el.getKey(), value);
            }
        } else {
            short upperLimitGrayLevel = partitionThresholdPoints[partition];
            if (upperLimitGrayLevel == 127)
                upperLimitGrayLevel = 128;
            for (SortedMap.Entry<Byte, Double> el : localPDF.entrySet()) {

                assert el.getKey() >= partitionThresholdPoints[partition - 1] && el.getKey() < upperLimitGrayLevel;
                value += el.getValue();
                if (value > 1) {
                    value = 1D;
                }
                localCDF.put(el.getKey(), value);

            }
        }
    }

    /**
     * should be called after calling calculatePDFOnPartition. Calculates Cumulative Density Function(CDF).
     */
    private void calculateCDFOnPartition() {
        for (byte i = 1; i < partitionThresholdPoints.length; ++i) {
            calculateCDF(i);
        }
    }

    public final void calculatePDF_CDF() {
        final byte partition = -1;
        initPDFAndCDF((byte) 1);
        calculatePDF(partition, calculateNumberOfPixels(partition));
        calculateCDF(partition);
    }

    public final void calculatePDF_CDFForPartitions(byte[] partitionThresholdPoints) {
        this.partitionThresholdPoints = partitionThresholdPoints;
        initPDFAndCDF((byte) (partitionThresholdPoints.length - 1));
        assert pdf.size() == partitionThresholdPoints.length - 1;
        assert cdf.size() == partitionThresholdPoints.length - 1;
        calculatePDFOnPartition();
        calculateCDFOnPartition();
    }

    private void initPDFAndCDF(byte nrOfPartitions) {
        pdf.clear();
        cdf.clear();
        for (byte i = 0; i < nrOfPartitions; ++i) {
            pdf.add(new TreeMap<>(Byte::compareTo));
            cdf.add(new TreeMap<>(Byte::compareTo));
        }
    }

    public void clippTheHistogram(byte[] partitionThresholdPoints,long[] clippingLevel) {
        assert clippingLevel.length + 1 == partitionThresholdPoints.length;
        this.partitionThresholdPoints = partitionThresholdPoints;
        for (byte i = 1; i < partitionThresholdPoints.length; ++i) {
            clippPartition(i,clippingLevel[i-1]);
        }
    }

    private void clippPartition(byte partition, long clippingLevel) {
        short upperLimitGrayLevel = partitionThresholdPoints[partition];
        if (upperLimitGrayLevel == 127)
            upperLimitGrayLevel = 128;
        for (SortedMap.Entry<Byte, Long> element : hist.entrySet()) {
            if (element.getKey() >= partitionThresholdPoints[partition - 1] && element.getKey() < upperLimitGrayLevel) {
                if (element.getValue() > clippingLevel) {
                    element.setValue(clippingLevel);
                }
            }
        }
    }

    public long[] getMediansInPartitions(byte[] partitionThresholdPoints) {
        this.partitionThresholdPoints = partitionThresholdPoints;
        long[] medians = new long[3];
        for (byte partition = 1; partition < partitionThresholdPoints.length; ++partition) {
            medians[partition - 1] = getMedianInPartition(partition);
        }
        return medians;
    }

    private long getMedianInPartition(byte partition) {
        List<Long> arr = new LinkedList<>();
        short upperLimitGrayLevel = partitionThresholdPoints[partition];
        if (upperLimitGrayLevel == 127)
            upperLimitGrayLevel = 128;
        for (SortedMap.Entry<Byte, Long> element : hist.entrySet()) {
            //<grayLevel,nrOfPixelsWithThisGrayLevel>
            if (element.getKey() >= partitionThresholdPoints[partition - 1] && element.getKey() < upperLimitGrayLevel) {
                arr.add(element.getValue());
            }
        }
        Collections.sort(arr);
        return arr.get(arr.size() / 2);
    }
}
