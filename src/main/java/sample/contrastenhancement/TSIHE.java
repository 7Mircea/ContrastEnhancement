package sample.contrastenhancement;

import com.sun.istack.internal.NotNull;

import java.util.SortedMap;

import static sample.utils.ChangeType.btoS;
import static sample.utils.ChangeType.dtoB;

/**
 * Tripartite Sub-Image Histogram Equalization
 */
public class TSIHE {
    public static void he(int height, int width, @NotNull Histogram histogramObj) {
        histogramObj.calculatePDF_CDF();
        short[] partitionThresholdPoints = getGrayLevels(histogramObj);
        System.out.println(partitionThresholdPoints[0] + " " + partitionThresholdPoints[1] + " " + partitionThresholdPoints[2] + " " + partitionThresholdPoints[3]);

        clippTheHistogram(histogramObj, partitionThresholdPoints);

        histogramObj.calculatePDF_CDFForPartitions(partitionThresholdPoints);

        byte[] arr = histogramObj.getArr();
        assert arr != null && arr.length > 0;
        assert histogramObj.getCdf().size() == 3;
        assert histogramObj.getPdf().size() == 3;
        SortedMap<Short, Double> cdf1 = histogramObj.getCdf().get(0);
        assert cdf1.size() > 0;
        SortedMap<Short, Double> cdf2 = histogramObj.getCdf().get(1);
        if (cdf2.size() > 0) {
            System.out.println("You might have problems");
        }
        SortedMap<Short, Double> cdf3 = histogramObj.getCdf().get(2);
        assert cdf3.size() > 0;
        SortedMap<Short, Double> pdf1 = histogramObj.getPdf().get(0);
        assert pdf1.size() > 0;
        SortedMap<Short, Double> pdf2 = histogramObj.getPdf().get(1);
        if (pdf2.size() > 0) {
            System.out.println("You might have a problem");
        }
        SortedMap<Short, Double> pdf3 = histogramObj.getPdf().get(2);
        assert pdf3.size() > 0;

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                short grayLevel = btoS(arr[j + i * width]);

                if (grayLevel >= partitionThresholdPoints[0] && grayLevel < partitionThresholdPoints[1]) {
                    arr[j + i * width] = dtoB(partitionThresholdPoints[1] * (cdf1.get(grayLevel) - 0.5D * pdf1.get(grayLevel)));
                } else if (grayLevel >= partitionThresholdPoints[1] && grayLevel < partitionThresholdPoints[2]) {
                    arr[j + i * width] = dtoB(partitionThresholdPoints[1] + (partitionThresholdPoints[2] - partitionThresholdPoints[1]) * (cdf2.get(grayLevel) - 0.5D * pdf2.get(grayLevel)));
                } else if (grayLevel >= partitionThresholdPoints[2] && grayLevel <= partitionThresholdPoints[3]) {
                    arr[j + i * width] = dtoB(partitionThresholdPoints[2] + (partitionThresholdPoints[3] - partitionThresholdPoints[2]) * (cdf3.get(grayLevel) - 0.5D * pdf3.get(grayLevel)));
                } else {
                    System.out.println("gray level not in interval [0,255] in TSIHE at HE step");
                    return;
                }
            }
        }

    }

    private static void clippTheHistogram(Histogram histogramObj, short[] partitionThresholdPoints) {
        int[] medians = histogramObj.getMediansInPartitions(partitionThresholdPoints);
        histogramObj.clippTheHistogram(partitionThresholdPoints, medians);
    }

    private static short[] getGrayLevels(final Histogram histogram) {
        final double cdfThreshold1 = 1D / 3D;
        final double cdfThreshold2 = 2D / 3D;
        short[] partitionThresholdPoints = new short[4];
        partitionThresholdPoints[0] = 0;
        partitionThresholdPoints[1] = getGrayLevelForCDF(histogram, cdfThreshold1);
        partitionThresholdPoints[2] = getGrayLevelForCDF(histogram, cdfThreshold2);
        partitionThresholdPoints[3] = 255;
        if (partitionThresholdPoints[0] == partitionThresholdPoints[1]
                || partitionThresholdPoints[1] == partitionThresholdPoints[2]
                || partitionThresholdPoints[2] == partitionThresholdPoints[3])
            System.out.println("you've got problems at selecting gray levels for partitioning");
        return partitionThresholdPoints;
    }

    private static short getGrayLevelForCDF(Histogram hist, double cdfThreshold) {
        SortedMap<Short, Double> cdf = hist.getCdf().get(0);

        for (SortedMap.Entry<Short, Double> element : cdf.entrySet()) {
            if (element.getValue() >= cdfThreshold) {
                return element.getKey();
            }
        }
        return 0;
    }
}
