package sample.utils;

import java.util.Objects;

import static sample.utils.Utils.min;

public class ErrorCalculation {
    public static float calculateCER(String reference, String second) {
        //declaration
        int[][] D = new int[reference.length() + 1][second.length() + 1];
        char[] r = new char[reference.length()+1];
        char[] s = new char[second.length()+1];
        int i, j;

        //initialization
        reference.getChars(0, reference.length(), r, 0);
        second.getChars(0, second.length(), s, 0);
        for (j = 0; j <= second.length(); ++j) {
            D[0][j] = j;
        }
        for (i = 0; i < reference.length(); ++i) {
            D[i][0] = i;
        }

        //calculation
        for (i = 1; i <= reference.length(); ++i) {
            for (j = 1; j <= second.length(); ++j) {
                if (r[i - 1] == s[j - 1])
                    D[i][j] = D[i - 1][j - 1];
                else {
                    int sub = D[i-1][j-1]+1;
                    int ins = D[i][j-1]+1;
                    int del = D[i-1][j]+1;
                    D[i][j] = Utils.min(sub,ins,del);
                }
            }
        }
        return D[reference.length()][second.length()]/(float)reference.length();
    }

    public static float calculateWER(String reference, String second) {
        //declaration
        String[] r = reference.split("[ \\t\\n\\x0B\\f\\r]+");//split on white space
        String[] s = second.split("[ \\t\\n\\x0B\\f\\r]+");//split on white space
        int[][] D = new int[r.length + 1][s.length + 1];
        int i, j;

        //initialization
        for (j = 0; j <= s.length; ++j) {
            D[0][j] = j;
        }
        for (i = 0; i < r.length; ++i) {
            D[i][0] = i;
        }

        //calculation
        for (i = 1; i <= r.length; ++i) {
            for (j = 1; j <= s.length; ++j) {
                if (Objects.equals(r[i - 1], s[j - 1]))
                    D[i][j] = D[i - 1][j - 1];
                else {
                    int sub = D[i-1][j-1]+1;
                    int ins = D[i][j-1]+1;
                    int del = D[i-1][j]+1;
                    D[i][j] = Utils.min(sub,ins,del);
                }
            }
        }
        return D[r.length][s.length]/(float)r.length;
    }
}
