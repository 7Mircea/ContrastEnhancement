package sample.contrastenhancement2;

import com.sun.istack.internal.NotNull;

import java.util.Iterator;
import java.util.Map;

public class HistogramEqualization {

    public static void he(byte[] arr,int height, int width,@NotNull Histogram histogramObj){
        histogramObj.calculateHE();
        Map<Byte,Double> cdf = histogramObj.getCdf();
        if (cdf == null) return;
        if (arr == null) {
            System.out.println("arr is null");
            return;
        }
        for (int i = 0; i < height; ++i){
            for (int j = 0; j < width;++j) {
                arr[j+i*width] = (byte) ((byte)(255*cdf.get(arr[j+i*width]) - 127));
            }
        }

    }

    private static void showEntriesOfCDF(Map<Byte, Double> cdf) {
        Iterator<Map.Entry<Byte, Double>> it = cdf.entrySet().iterator();

        while(it.hasNext()){
            Map.Entry<Byte, Double> el = it.next();
            System.out.println("gray level " + el.getKey() + " value " + el.getValue());
        }
        System.out.println("-----------------------------");
    }
}
