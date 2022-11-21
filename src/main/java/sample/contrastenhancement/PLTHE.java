package sample.contrastenhancement;

import com.sun.istack.internal.NotNull;

import java.util.SortedMap;

import static sample.utils.ChangeType.btoS;

public class PLTHE {
    public static void he(int height, int width, @NotNull Histogram histogramObj) {
        byte[] partitionThresholdPoints = new byte[]{(byte) -128, (byte) 127};
        long[] arrMedians = histogramObj.getMediansInPartitions(partitionThresholdPoints);
        assert arrMedians != null && arrMedians.length == 1;
        long median = arrMedians[0];
        long[] arrMeans = histogramObj.getMeansInHistogram(partitionThresholdPoints);
        assert arrMeans != null && arrMeans.length == 1;
        long mean = arrMeans[0];
        long clippingLimit = (median + mean) / 2;
        System.out.println("clipping limit : " + clippingLimit);
        histogramObj.clippTheHistogram(partitionThresholdPoints, new long[]{clippingLimit});
        partitionThresholdPoints = getGrayLevels(histogramObj, mean);
        System.out.println("partition points ");
        for (byte el:partitionThresholdPoints) {
            System.out.print(el + " ");
        }

        histogramObj.calculatePDF_CDFForPartitions(partitionThresholdPoints);

        byte[] arr = histogramObj.getArr();
        assert arr != null && arr.length > 0;
        assert histogramObj.getCdf().size() == 3;
        assert histogramObj.getPdf().size() == 3;
        SortedMap<Byte, Double> cdf1 = histogramObj.getCdf().get(0);
        SortedMap<Byte, Double> cdf2 = histogramObj.getCdf().get(1);
        SortedMap<Byte, Double> cdf3 = histogramObj.getCdf().get(2);
        SortedMap<Byte, Double> pdf1 = histogramObj.getPdf().get(0);
        SortedMap<Byte, Double> pdf2 = histogramObj.getPdf().get(1);
        SortedMap<Byte, Double> pdf3 = histogramObj.getPdf().get(2);

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                byte grayLevel = arr[j + i * width];
                if (grayLevel >= partitionThresholdPoints[0] && grayLevel < partitionThresholdPoints[1]) {
                    arr[j + i * width] = (byte) (btoS(partitionThresholdPoints[1]) * (cdf1.get(grayLevel) - 0.5D * pdf1.get(grayLevel)) - 128);
                } else if (grayLevel >= partitionThresholdPoints[1] && grayLevel < partitionThresholdPoints[2]) {
                    arr[j + i * width] = (byte) (btoS(partitionThresholdPoints[1]) + (partitionThresholdPoints[2] - partitionThresholdPoints[1]) * (cdf2.get(grayLevel) - 0.5D * pdf2.get(grayLevel)) - 128);
                } else if (grayLevel >= partitionThresholdPoints[2] && grayLevel <= partitionThresholdPoints[3]) {
                    arr[j + i * width] = (byte) (btoS(partitionThresholdPoints[2]) + (partitionThresholdPoints[3] - partitionThresholdPoints[2]) * (cdf3.get(grayLevel) - 0.5D * pdf3.get(grayLevel)) - 128);
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
    private static byte[] getGrayLevels(final Histogram histogram, long mean) {
        byte[] partitionThresholdPoints = new byte[4];
        partitionThresholdPoints[0] = -128;
        byte[] partitionInnerThresholdPoints = getInnerGrayLevels(histogram, mean);
        assert partitionInnerThresholdPoints.length == 2;
        partitionThresholdPoints[1] = partitionInnerThresholdPoints[0];
        partitionThresholdPoints[2] = partitionInnerThresholdPoints[1];
        partitionThresholdPoints[3] = 127;
        return partitionThresholdPoints;
    }

    private static byte[] getInnerGrayLevels(final Histogram hist, long mean) {
        long nrOfPixels = hist.calculateNumberOfPixels((byte) -1);
        long sum = 0;
        for (SortedMap.Entry<Byte, Long> element : hist.getHist().entrySet()) {
            sum += (element.getKey() - mean) * (element.getKey() - mean) * element.getValue();
        }
        byte std_dev = (byte) (sum / nrOfPixels);

        return new byte[]{(byte) (((byte) -128) + std_dev), (byte) (((byte) 127) - std_dev)};
    }
}
