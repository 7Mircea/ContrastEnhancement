package sample.utils;

public class ChangeType {
    /**
     * change a byte to a short. Also, changes from [-128,127] to [0,255]
     * @return
     */
    public static int btoS (byte arg) {
        return ((short)arg) + 128;
    }
}
