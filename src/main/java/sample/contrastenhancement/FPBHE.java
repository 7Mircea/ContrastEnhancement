package sample.contrastenhancement;

import com.sun.istack.internal.NotNull;

import java.util.SortedMap;

import static sample.utils.ChangeType.btoS;

public class FPBHE {
    public static void he(int height, int width, @NotNull Histogram histogramObj) {
        double eps = calculateEps(histogramObj);
        double gamma = eps >= 0.5D ? (1D - eps) : eps;
        System.out.println("gamma is " + gamma);
        gammaCorrection(histogramObj, gamma);
        byte splitThreshold = getSplitThreshold(eps);
        System.out.println("split threshold " + splitThreshold);
        byte U = getU(histogramObj, gamma, splitThreshold);
        Long Su = 0L;
        Long So = 0L;
        getSuSo(histogramObj, U, Su, So);

        he(histogramObj, new byte[]{-128, splitThreshold, 127});
    }

    private static final void he(Histogram histogramObj, byte[] partitionThresholdPoints) {
        int width = histogramObj.getWidth();
        int height = histogramObj.getHeight();
        histogramObj.calculatePDF_CDFForPartitions(partitionThresholdPoints);

        byte[] arr = histogramObj.getArr();
        assert arr != null && arr.length > 0;
        assert histogramObj.getCdf().size() == 3;
        assert histogramObj.getPdf().size() == 3;
        SortedMap<Byte, Double> cdf1 = histogramObj.getCdf().get(0);
        assert cdf1.size() > 0;
        SortedMap<Byte, Double> cdf2 = histogramObj.getCdf().get(1);
        assert cdf2.size() > 0;
        SortedMap<Byte, Double> pdf1 = histogramObj.getPdf().get(0);
        assert pdf1.size() > 0;
        SortedMap<Byte, Double> pdf2 = histogramObj.getPdf().get(1);
        assert pdf2.size() > 0;

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                byte grayLevel = arr[j + i * width];
                if (grayLevel >= partitionThresholdPoints[0] && grayLevel < partitionThresholdPoints[1]) {
                    arr[j + i * width] = (byte) (btoS(partitionThresholdPoints[1]) * cdf1.get(grayLevel) - 128);
                } else if (grayLevel >= partitionThresholdPoints[1] && grayLevel < partitionThresholdPoints[2]) {
                    arr[j + i * width] = (byte) (btoS(partitionThresholdPoints[1]) + (partitionThresholdPoints[2] - partitionThresholdPoints[1]) * cdf2.get(grayLevel) - 128);
                }
            }
        }
    }

    private static long getSuSo(Histogram histogramObj, byte U, Long Su, Long So) {
        long[] mean = histogramObj.getMeansInHistogram(new byte[]{-128, U, 127});
        long nrOfBytes = histogramObj.calculateNumberOfPixels((byte) 1);
        SortedMap<Byte, Long> hist = histogramObj.getHist();
        long sum = 0;
        for (byte i = -128; i <= U; ++i) {
            sum += Math.pow(hist.get(i) - mean[0], 2);
        }
        return (long) Math.sqrt(sum / (double) nrOfBytes);
    }

    private static byte getU(Histogram histogramObj, double gamma, byte Ts) {
        SortedMap<Byte, Long> hist = histogramObj.getHist();
        long sum = 0;
        for (byte i = -128; i < Ts; ++i) {
            sum += hist.get(i);
        }
        long sum2 = 0;
        for (byte i = (byte) (Ts + (byte) 1); i <= Byte.MAX_VALUE; ++i) {
            sum2 += hist.get(i);
        }
        double correctedSum = Math.pow(sum, gamma);
        double correctedSum2 = Math.pow(sum2, gamma);
        return (byte) (255D * (correctedSum / (correctedSum + correctedSum2)));
    }

    private static byte getSplitThreshold(double eps) {
        return (byte) (255 * eps - 128);
    }

    private static void gammaCorrection(Histogram histogram, double gamma) {
        int height = histogram.getHeight();
        int width = histogram.getWidth();
        byte[] arr = histogram.getArr();
        SortedMap<Byte, Double> cdf = histogram.getCdf().get(0);
        for (SortedMap.Entry<Byte, Long> el : histogram.getHist().entrySet()) {
            el.setValue((long) Math.pow(el.getValue(), gamma));
        }
    }

    private static double calculateEps(final Histogram histogramObj) {
        byte[] means = histogramObj.getMeansGrayLevelInHistogram(new byte[]{-128, 127});
        assert means != null && means.length == 1;
        byte mean = means[0];
        return (double) mean / 255D;
    }


}
