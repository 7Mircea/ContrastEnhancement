package sample.contrastenhancement;

import com.sun.istack.internal.NotNull;

import java.util.Iterator;
import java.util.Map;

public class HistogramEqualization {

    public static void he(int height, int width, @NotNull Histogram histogramObj) {
        histogramObj.calculatePDF_CDF();
        Map<Byte, Double> cdf = histogramObj.getCdf().get(0);
        byte[] arr = histogramObj.getArr();
        if (cdf == null || cdf.isEmpty()) {
            System.out.println("cdf is empty in HistogramEqualization.he()");
            return;
        }
        if (arr == null) {
            System.out.println("arr is null");
            return;
        }
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                byte grayLevel = arr[j + i * width];
                double value = cdf.get(grayLevel);
                arr[j + i * width] = (byte) (255 * value - 128);
            }
        }

    }

    private static void showEntriesOfCDF(Map<Byte, Double> cdf) {
        Iterator<Map.Entry<Byte, Double>> it = cdf.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Byte, Double> el = it.next();
            System.out.println("gray level " + el.getKey() + " value " + el.getValue());
        }
        System.out.println("-----------------------------");
    }
}
