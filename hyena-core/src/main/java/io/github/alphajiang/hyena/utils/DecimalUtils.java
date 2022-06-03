package io.github.alphajiang.hyena.utils;

import java.math.BigDecimal;

public class DecimalUtils {

    public static final int SCALE_2 = 2;
    public static final BigDecimal ZERO = BigDecimal.valueOf(0, SCALE_2);

    /**
     *
     * @param left left value
     * @param right right value
     * @return true if left is less than right
     */
    public static boolean lt(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) < 0;
    }

    public static boolean ltZero(BigDecimal left) {
        return left.compareTo(BigDecimal.ZERO) < 0;
    }
    public static boolean lte(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) < 1;
    }

    public static boolean gt(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) > 0;
    }
    public static boolean gte(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) > -1;
    }

}
