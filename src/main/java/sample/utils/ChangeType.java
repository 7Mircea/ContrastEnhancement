package sample.utils;

public class ChangeType {
    /**
     * change a byte to a short. Also, changes from [-128,127] to [0,255]
     *
     * @return
     */
    public static short btoS(byte arg) {
        return (short) (((short) arg) + (short)128);
    }

    /**
     * convert short to byte. short should have values between [0,255]
     *
     * @param arg
     * @return
     */
    public static byte stoB(short arg) {
        if (arg < 0 || arg > 255) {
            System.out.println("arg " + arg + " not in [0,255]");
            System.exit(120);
        }
        return (byte) (arg - 128);
    }

    /**
     * convert short to byte. double should have values between [0,255]
     *
     * @param arg
     * @return values between [-128,127]
     */
    public static byte dtoB(double arg) {
        if (arg < 0 || arg > 255){
            System.out.println("arg " + arg + " not in [0,255]");
            System.exit(120);
        }
        return (byte) (arg - 128);
    }
}
