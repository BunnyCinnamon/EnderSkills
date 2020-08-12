package arekkuusu.enderskills.api.helper;

public final class MathUtil {

    public final static double Epsilon = 1E-5D;

    public static boolean fuzzyEqual(float a, float b) {
        return Math.abs(a - b) <= Epsilon;
    }

    public static boolean fuzzyEqual(double a, double b) {
        return Math.abs(a - b) <= Epsilon;
    }
}
