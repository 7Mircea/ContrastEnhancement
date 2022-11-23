package sample.contrastenhancement;

import com.sun.istack.internal.NotNull;
import lombok.Getter;

import java.util.*;

@Getter
public class Histogram {
    private final SortedMap<Short, Integer> hist;
    private short[] partitionThresholdPoints = null;//[0,pct,,pct2,final]
    private final List<SortedMap<Short, Double>> pdf = new LinkedList<>(); //pdf - probabilty density function
    private final List<SortedMap<Short, Double>> cdf = new LinkedList<>(); //cdf - cummulative density function
    //    private BufferedImage image;
    private final int width;
    private final int height;
    private final int processors = Runtime.getRuntime().availableProcessors();
    private final byte[] arr;

    public Histogram(@NotNull byte[] arr, int height, int width) {
        this.arr = arr;
        hist = new TreeMap<>(Short::compareTo);//loadFactor(second argument) - represents when the HashMap allocate
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
    private void calculatePDF(byte partition, int numberOfPixelsInThisPartition) {
        if (partition != -1 && partition < 1) {
            System.out.println("attempt to calculate pdf on non-existing partition " + partition);
            return;
        }
        SortedMap<Short, Double> localPDF = selectLocal(pdf, partition);
        if (partition == -1) {//calculate pdf for entire histogram
            for (Short key : hist.keySet()) {
                localPDF.put(key, hist.get(key) / (double) numberOfPixelsInThisPartition);
            }
            return;
        }
        short upperLimmit = partitionThresholdPoints[partition];
        upperLimmit = (upperLimmit == 255) ? (short)(256) : upperLimmit;
        for (Short key : hist.keySet()) {//calculate pdf only on partition i
            if (key >= partitionThresholdPoints[partition - 1] && key < upperLimmit) {
                localPDF.put(key, hist.get(key) / (double) numberOfPixelsInThisPartition);
            }
        }
    }

    private SortedMap<Short, Double> selectLocal(List<SortedMap<Short, Double>> list, byte partition) {
        return (partition != -1) ? list.get(partition - 1) : list.get(0);
    }

    private void calculatePDFOnPartition() {
        for (byte i = 1; i < partitionThresholdPoints.length; ++i) {
            calculatePDF(i, calculateNumberOfPixels(i));
        }
    }

    /**
     * works only for pictures with resolution max 2GPixeli
     *
     * @param partition
     * @return
     */
    public int calculateNumberOfPixels(byte partition) {
        if (partition == -1) {
            return width * height;
        }
        int nrOfPixels = 0;


        short upperLimmit = partitionThresholdPoints[partition];
        upperLimmit = (upperLimmit == 255) ? (short)(256) : upperLimmit;
        for (SortedMap.Entry<Short, Integer> element : hist.entrySet()) {
            //<grayLevel,nrOfPixelsWithThisGrayLevel>
            if (element.getKey() >= partitionThresholdPoints[partition - 1] && element.getKey() < upperLimmit) {
                nrOfPixels += element.getValue();
            }
        }
        return nrOfPixels;
    }

    /**
     * should be called after calling calculatePDF. Calculates Cumulative Density Function(CDF).
     */
    private void calculateCDF(byte partition) {
        SortedMap<Short, Double> localPDF = selectLocal(pdf, partition);
        SortedMap<Short, Double> localCDF = selectLocal(cdf, partition);
        Double value = 0D;
        if (partition == -1) {
            for (SortedMap.Entry<Short, Double> el : localPDF.entrySet()) {
                value += el.getValue();
                if (value > 1)
                    value = 1D;
                localCDF.put(el.getKey(), value);
            }
        } else {
            short upperLimmit = partitionThresholdPoints[partition];
            upperLimmit = (upperLimmit == 255) ? (short)(256) : upperLimmit;
            for (SortedMap.Entry<Short, Double> el : localPDF.entrySet()) {
                assert el.getKey() >= partitionThresholdPoints[partition - 1] && el.getKey() < upperLimmit ;
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

    public final void calculatePDF_CDFForPartitions(short[] partitionThresholdPoints) {
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
            pdf.add(new TreeMap<>(Short::compareTo));
            cdf.add(new TreeMap<>(Short::compareTo));
        }
    }

    public void clippTheHistogram(short[] partitionThresholdPoints, int[] clippingLevel) {
        assert clippingLevel.length + 1 == partitionThresholdPoints.length;
        this.partitionThresholdPoints = partitionThresholdPoints;
        for (byte i = 1; i < partitionThresholdPoints.length; ++i) {
            clippPartition(i, clippingLevel[i - 1]);
        }
    }

    private void clippPartition(byte partition, int clippingLevel) {
        short upperLimmit = partitionThresholdPoints[partition];
        upperLimmit = (upperLimmit == 255) ? (short)(256) : upperLimmit;
        for (SortedMap.Entry<Short, Integer> element : hist.entrySet()) {
            if (element.getKey() >= partitionThresholdPoints[partition - 1] && element.getKey() < upperLimmit) {
                if (element.getValue() > clippingLevel) {
                    element.setValue(clippingLevel);
                }
            }
        }
    }

    /**
     * calculates the median of a given histogram. Thus it returns the median from an array of numbers
     * each number representing the number of pixels for a given gray
     *
     * @param partitionThresholdPoints
     * @return
     */
    public int[] getMediansInPartitions(short[] partitionThresholdPoints) {
        this.partitionThresholdPoints = partitionThresholdPoints;
        int[] medians = new int[partitionThresholdPoints.length - 1];
        for (byte partition = 1; partition < partitionThresholdPoints.length; ++partition) {
            medians[partition - 1] = getMedianInPartition(partition);
        }
        return medians;
    }

    private int getMedianInPartition(byte partition) {
        List<Integer> arr = new LinkedList<>();
        short upperLimmit = partitionThresholdPoints[partition];
        upperLimmit = (upperLimmit == 255) ? (short)(256) : upperLimmit;
        for (SortedMap.Entry<Short, Integer> element : hist.entrySet()) {
            //<grayLevel,nrOfPixelsWithThisGrayLevel>
            if (element.getKey() >= partitionThresholdPoints[partition - 1] && element.getKey() < upperLimmit) {
                arr.add(element.getValue());
            }
        }
        Collections.sort(arr);
        return arr.get(arr.size() / 2);
    }


    /**
     * calculates the median of a given histogram. Thus, it returns the median from an array of numbers
     * each number representing the number of pixels for a given gray
     *
     * @param partitionThresholdPoints
     * @return
     */
    public short[] getMediansGrayLevelInPartitions(short[] partitionThresholdPoints) {
        this.partitionThresholdPoints = partitionThresholdPoints;
        short[] medians = new short[partitionThresholdPoints.length - 1];
        for (byte partition = 1; partition < partitionThresholdPoints.length; ++partition) {
            medians[partition - 1] = getMediansGrayLevelInPartition(partition);
        }
        return medians;
    }

    /**
     * returns the grayLevel that represents the median value of an array consisting of all values
     * for pixels of that specific partition. Do not confuse with getMedianInPartition(T partition)
     *
     * @param partition
     * @return gray level
     */
    private short getMediansGrayLevelInPartition(byte partition) {
        int nrOfPixels = 0;
        int nrOfPixelsInPartition = calculateNumberOfPixels(partition);
        short upperLimmit = partitionThresholdPoints[partition];
        upperLimmit = (upperLimmit == 255) ? (short)(256) : upperLimmit;
        for (SortedMap.Entry<Short, Integer> element : hist.entrySet()) {
            //<grayLevel,nrOfPixelsWithThisGrayLevel>
            if (element.getKey() >= partitionThresholdPoints[partition - 1] && element.getKey() < upperLimmit) {
                nrOfPixels += element.getValue();
                if (nrOfPixels >= nrOfPixelsInPartition / 2)
                    return element.getKey();
            }
        }
        throw new RuntimeException("nrOfPixels never equals totalNumberObFixels/2 although the algorithm iterates through all gray levels for this partition of histogram");
    }

    public short[] getMeansGrayLevelInHistogram(short[] partitionThresholdPoints) {
        this.partitionThresholdPoints = partitionThresholdPoints;
        short[] means = new short[partitionThresholdPoints.length - 1];
        for (byte partition = 1; partition < partitionThresholdPoints.length; ++partition) {
            means[partition - 1] = getMeansGrayLevelInPartition(partition);
        }
        return means;
    }

    private short getMeansGrayLevelInPartition(byte partition) {
        int sum = 0;
        int nrOfPixelsInThisPartition = calculateNumberOfPixels(partition);
        short upperLimmit = partitionThresholdPoints[partition];
        upperLimmit = (upperLimmit == 255) ? (short)(256) : upperLimmit;
        for (SortedMap.Entry<Short, Integer> element : hist.entrySet()) {
            //<grayLevel,nrOfPixelsWithThisGrayLevel>
            if (element.getKey() >= partitionThresholdPoints[partition - 1] && element.getKey() < upperLimmit) {
                sum += element.getValue() * element.getKey();
            }
        }
        return (short) (sum / (double) nrOfPixelsInThisPartition);
    }

    /**
     * calculates the mean of a given histogram. Thus it returns the median from an array of numbers
     * each number representing the number of pixels for a given gray
     *
     * @param partitionThresholdPoints
     * @return
     */
    public int[] getMeansInHistogram(short[] partitionThresholdPoints) {
        this.partitionThresholdPoints = partitionThresholdPoints;
        int[] means = new int[partitionThresholdPoints.length - 1];
        for (byte partition = 1; partition < partitionThresholdPoints.length; ++partition) {
            means[partition - 1] = getMeansInPartition(partition);
        }
        return means;
    }

    private int getMeansInPartition(byte partition) {
        int sum = 0;
        int nrOfPixelsInHistogram = calculateNumberOfPixels(partition);
        short upperLimmit = partitionThresholdPoints[partition];
        upperLimmit = (upperLimmit == 255) ? (short)(256) : upperLimmit;
        for (SortedMap.Entry<Short, Integer> element : hist.entrySet()) {
            //<grayLevel,nrOfPixelsWithThisGrayLevel>
            if (element.getKey() >= partitionThresholdPoints[partition - 1] && element.getKey() < upperLimmit) {
                sum += element.getValue();
            }
        }
        return nrOfPixelsInHistogram;
    }
}
