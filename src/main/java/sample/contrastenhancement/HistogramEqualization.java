package sample.contrastenhancement;

import com.sun.istack.internal.NotNull;

import java.util.Iterator;
import java.util.Map;

import static sample.utils.ChangeType.*;

public class HistogramEqualization {

    public static void he(int height, int width, @NotNull Histogram histogramObj) {
        histogramObj.calculatePDF_CDF();
        Map<Short, Double> cdf = histogramObj.getCdf().get(0);
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
                short grayLevel = btoS(arr[j + i * width]) ;
                if (grayLevel == 255) {
                    System.out.println("gray level 255 found in HE");
                }
                double value = cdf.get(grayLevel);
                assert value >=0 && value <=1;
                arr[j + i * width] = dtoB(255 * value);
            }
        }

    }

    private static void showMappingsOfHE(byte[] arr, Map<Byte, Double> cdf) {
        for (Map.Entry<Byte, Double> el : cdf.entrySet()) {
//            System.out.println("old:" + el.getKey() + " new:" + ((byte) (255 * el.getValue() - 128)));
            short old = btoS(el.getKey());
            double newValue = 255 * el.getValue();
            byte cast = (byte) newValue;

            if (Math.abs(old - newValue) > 50) {
                if (newValue != cast) {
                    System.out.println("old value " + old + " new value : " + newValue + " new value cast to byte: " + cast);
//                    System.out.println("cdf for   " + old + " converted " + stoB(old) + " is " + cdf.get(stoB(old)));
                }
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
