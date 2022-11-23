package sample.contrastenhancement;

import com.sun.istack.internal.NotNull;

import java.util.Iterator;
import java.util.SortedMap;

import static sample.utils.ChangeType.btoS;
import static sample.utils.ChangeType.dtoB;

public class FPBHE {
    public static void he(@NotNull Histogram histogramObj) {
        double eps = calculateEps(histogramObj);
        System.out.println("eps is " + eps);
        double gamma = eps >= 0.5D ? (1D - eps) : eps;
        System.out.println("gamma is " + gamma);
        gammaCorrection(histogramObj, gamma);
        short splitThreshold = getSplitThreshold(eps);
        System.out.println("split threshold " + splitThreshold);
        short U = getU(histogramObj, gamma, splitThreshold);
        Integer Su = 0;
        Integer So = 0;
        getSuSo(histogramObj, U, Su, So);
        System.out.println("Su " + Su + " So " + So);

        he(histogramObj, new short[]{0, splitThreshold, 255});
    }

    private static void he(Histogram histogramObj, short[] partitionThresholdPoints) {
        int width = histogramObj.getWidth();
        int height = histogramObj.getHeight();
        histogramObj.calculatePDF_CDFForPartitions(partitionThresholdPoints);

        byte[] arr = histogramObj.getArr();
        assert arr != null && arr.length > 0;
        assert histogramObj.getCdf().size() == 2;
        assert histogramObj.getPdf().size() == 2;
        SortedMap<Short, Double> cdf1 = histogramObj.getCdf().get(0);
        assert cdf1.size() > 0;
        SortedMap<Short, Double> cdf2 = histogramObj.getCdf().get(1);
        assert cdf2.size() > 0;
        SortedMap<Short, Double> pdf1 = histogramObj.getPdf().get(0);
        assert pdf1.size() > 0;
        SortedMap<Short, Double> pdf2 = histogramObj.getPdf().get(1);
        assert pdf2.size() > 0;

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                short grayLevel = btoS(arr[j + i * width]);
                if (grayLevel >= partitionThresholdPoints[0] && grayLevel < partitionThresholdPoints[1]) {
                    arr[j + i * width] = dtoB(partitionThresholdPoints[1] * cdf1.get(grayLevel));
                } else if (grayLevel >= partitionThresholdPoints[1]) {
                    arr[j + i * width] = dtoB(partitionThresholdPoints[1] + (partitionThresholdPoints[2] - partitionThresholdPoints[1]) * cdf2.get(grayLevel));
                }
            }
        }
    }

    private static void getSuSo(final Histogram histogramObj, final short U, Integer Su, Integer So) {
        int[] histogramMeans = histogramObj.getMeansInHistogram(new short[]{0, U, 255});
        int histogramMean = histogramMeans[0];
        int nrOfPixels = histogramObj.calculateNumberOfPixels((byte) 1);
        SortedMap<Short, Integer> hist = histogramObj.getHist();
        long sum = 0;
        for (SortedMap.Entry<Short, Integer> el : hist.entrySet()) {
            sum += Math.pow(el.getValue() - histogramMean, 2);
        }
        Su = (int) Math.sqrt(sum / (double) nrOfPixels);
        So = (int) Math.sqrt(sum / (double) nrOfPixels);
    }

    private static short getU(final Histogram histogramObj, final double gamma, final short Ts) {
        Iterator<SortedMap.Entry<Short, Integer>> it = histogramObj.getHist().entrySet().iterator();
        int sum = 0;
        SortedMap.Entry<Short, Integer> el = it.next();
        while (it.hasNext() && el.getKey() < Ts) {
            sum += el.getValue();
            el = it.next();
        }
        int sum2 = 0;
        while (it.hasNext()) {
            sum2 += el.getValue();
            el = it.next();
        }
        double correctedSum = Math.pow(sum, gamma);
        double correctedSum2 = Math.pow(sum2, gamma);
        return (short) (255D * (correctedSum / (correctedSum + correctedSum2)));
    }

    private static short getSplitThreshold(double eps) {
        return (short) (255 * eps);
    }

    private static void gammaCorrection(Histogram histogram, final double gamma) {
        for (SortedMap.Entry<Short, Integer> el : histogram.getHist().entrySet()) {
            el.setValue((int) Math.pow(el.getValue(), gamma));
        }
    }

    private static double calculateEps(final Histogram histogramObj) {
        short[] means = histogramObj.getMeansGrayLevelInHistogram(new short[]{0, 255});
        assert means != null && means.length == 1;
        short mean = means[0];
        System.out.println("mean is " + mean);
        return (double) mean / 255D;
    }


}
