package sample.contrastenhancement;

import com.sun.istack.internal.NotNull;

import java.util.SortedMap;

import static sample.utils.ChangeType.*;

public class PLTHE {
    public static void he(int height, int width, @NotNull Histogram histogramObj) {
        short[] partitionThresholdPoints = new short[]{0, 255};
        int[] arrHistogramMedians = histogramObj.getMediansInPartitions(partitionThresholdPoints);
        assert arrHistogramMedians != null && arrHistogramMedians.length == 1;
        int histogramMedian = arrHistogramMedians[0];
        int[] arrHistogramMeans = histogramObj.getMeansInHistogram(partitionThresholdPoints);
        assert arrHistogramMeans != null && arrHistogramMeans.length == 1;
        int histogramMean = arrHistogramMeans[0];
        int clippingLimit = (histogramMedian + histogramMean) / 2;
        System.out.println("clipping limit : " + clippingLimit);
        histogramObj.clippTheHistogram(partitionThresholdPoints, new int[]{clippingLimit});
        short[] grayLevelMeans = histogramObj.getMeansGrayLevelInHistogram(new short[]{0, 255});
        short grayLevelMean = grayLevelMeans[0];
        partitionThresholdPoints = getGrayLevels(histogramObj, grayLevelMean);
        System.out.println("partition points ");
        for (short el : partitionThresholdPoints) {
            System.out.print(el + " ");
        }

        histogramObj.calculatePDF_CDFForPartitions(partitionThresholdPoints);

        byte[] arr = histogramObj.getArr();
        assert arr != null && arr.length > 0;
        assert histogramObj.getCdf().size() == 3;
        assert histogramObj.getPdf().size() == 3;
        SortedMap<Short, Double> cdf1 = histogramObj.getCdf().get(0);
        SortedMap<Short, Double> cdf2 = histogramObj.getCdf().get(1);
        SortedMap<Short, Double> cdf3 = histogramObj.getCdf().get(2);
        SortedMap<Short, Double> pdf1 = histogramObj.getPdf().get(0);
        SortedMap<Short, Double> pdf2 = histogramObj.getPdf().get(1);
        SortedMap<Short, Double> pdf3 = histogramObj.getPdf().get(2);

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                short grayLevel = btoS(arr[j + i * width]);
                if (grayLevel >= partitionThresholdPoints[0] && grayLevel < partitionThresholdPoints[1]) {
                    arr[j + i * width] = dtoB(partitionThresholdPoints[1] * cdf1.get(grayLevel));
                } else if (grayLevel >= partitionThresholdPoints[1] && grayLevel < partitionThresholdPoints[2]) {
                    arr[j + i * width] = dtoB(partitionThresholdPoints[1] + (partitionThresholdPoints[2] - partitionThresholdPoints[1]) * cdf2.get(grayLevel));
                } else if (grayLevel >= partitionThresholdPoints[2] && grayLevel <= partitionThresholdPoints[3]) {
                    arr[j + i * width] = dtoB(partitionThresholdPoints[2] + (partitionThresholdPoints[3] - partitionThresholdPoints[2]) * cdf3.get(grayLevel));
                }
            }
        }
    }

    /**
     * returns gray levels for partitioning the original histogram
     *
     * @param histogram
     * @return
     */
    private static short[] getGrayLevels(final Histogram histogram, int mean) {
        short[] partitionThresholdPoints = new short[4];
        partitionThresholdPoints[0] = -128;
        short[] partitionInnerThresholdPoints = getInnerGrayLevels(histogram, mean);
        assert partitionInnerThresholdPoints.length == 2;
        partitionThresholdPoints[1] = partitionInnerThresholdPoints[0];
        partitionThresholdPoints[2] = partitionInnerThresholdPoints[1];
        partitionThresholdPoints[3] = 127;
        return partitionThresholdPoints;
    }

    private static short[] getInnerGrayLevels(final Histogram hist, long mean) {
        int nrOfPixels = hist.calculateNumberOfPixels((byte) -1);
        double sum = 0;
        for (SortedMap.Entry<Short, Integer> element : hist.getHist().entrySet()) {
            sum += (element.getKey() - mean) * (element.getKey() - mean) * element.getValue();
        }
        short std_dev = (short) (sum / nrOfPixels);

        return new short[]{std_dev, (short) (255 - std_dev)};
    }
}
