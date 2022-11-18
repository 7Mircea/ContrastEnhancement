package sample.contrastenhancement;

import com.sun.istack.internal.NotNull;

import java.util.SortedMap;

import static sample.utils.ChangeType.btoS;

public class TSIHE {
    public static void he(int height, int width, @NotNull Histogram histogramObj) {
        histogramObj.calculatePDF_CDF();
        byte[] partitionThresholdPoints = getGrayLevels(histogramObj);

        clippTheHistogram(histogramObj,partitionThresholdPoints);

        histogramObj.calculatePDF_CDFForPartitions(partitionThresholdPoints);

        byte[] arr = histogramObj.getArr();
        assert arr != null && arr.length > 0;
        assert histogramObj.getCdf().size() == 3;
        assert histogramObj.getPdf().size() == 3;
        SortedMap<Byte, Double> cdf1 = histogramObj.getCdf().get(0);
        assert cdf1.size() > 0;
        SortedMap<Byte, Double> cdf2 = histogramObj.getCdf().get(1);
        assert cdf2.size() > 0;
        SortedMap<Byte, Double> cdf3 = histogramObj.getCdf().get(2);
        assert cdf3.size() > 0;
        SortedMap<Byte, Double> pdf1 = histogramObj.getPdf().get(0);
        assert pdf1.size() > 0;
        SortedMap<Byte, Double> pdf2 = histogramObj.getPdf().get(1);
        assert pdf2.size() > 0;
        SortedMap<Byte, Double> pdf3 = histogramObj.getPdf().get(2);
        assert pdf3.size() > 0;

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

    private static void clippTheHistogram(Histogram histogramObj,byte[] partitionThresholdPoints) {
        long[] medians = histogramObj.getMediansInPartitions(partitionThresholdPoints);
        histogramObj.clippTheHistogram(partitionThresholdPoints,medians);
    }

    private static byte[] getGrayLevels(final Histogram histogram) {
        final double cdfThreshold1 = 1D / 3D;
        final double cdfThreshold2 = 2D / 3D;
        byte[] partitionThresholdPoints = new byte[4];
        partitionThresholdPoints[0] = -128;
        partitionThresholdPoints[1] = getGrayLevelForCDF(histogram, cdfThreshold1);
        partitionThresholdPoints[2] = getGrayLevelForCDF(histogram, cdfThreshold2);
        partitionThresholdPoints[3] = 127;
        if (partitionThresholdPoints[0] == partitionThresholdPoints[1]
                || partitionThresholdPoints[1] == partitionThresholdPoints[2]
                || partitionThresholdPoints[2] == partitionThresholdPoints[3])
            System.out.println("you've got problems at selecting gray levels for partitioning");
        return partitionThresholdPoints;
    }

    private static byte getGrayLevelForCDF(Histogram hist, double cdfThreshold) {
        SortedMap<Byte, Double> cdf = hist.getCdf().get(0);

        for (SortedMap.Entry<Byte, Double> element : cdf.entrySet()) {
            if (element.getValue() >= cdfThreshold) {
                return element.getKey();
            }
        }
        return -128;
    }
}
