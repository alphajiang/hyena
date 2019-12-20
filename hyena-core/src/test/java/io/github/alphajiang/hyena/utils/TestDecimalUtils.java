package io.github.alphajiang.hyena.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TestDecimalUtils {


    @Test
    public void test_lt() {
        Assertions.assertTrue(DecimalUtils.lt(BigDecimal.valueOf(100), BigDecimal.valueOf(123)));
        Assertions.assertFalse(DecimalUtils.lt(BigDecimal.valueOf(100), BigDecimal.valueOf(100)));
        Assertions.assertFalse(DecimalUtils.lt(BigDecimal.valueOf(123), BigDecimal.valueOf(100)));
    }

    @Test
    public void test_lte() {
        Assertions.assertTrue(DecimalUtils.lte(BigDecimal.valueOf(100), BigDecimal.valueOf(123)));
        Assertions.assertTrue(DecimalUtils.lte(BigDecimal.valueOf(100), BigDecimal.valueOf(100)));
        Assertions.assertFalse(DecimalUtils.lte(BigDecimal.valueOf(123), BigDecimal.valueOf(100)));
    }

    @Test
    public void test_gt() {
        Assertions.assertFalse(DecimalUtils.gt(BigDecimal.valueOf(100), BigDecimal.valueOf(123)));
        Assertions.assertFalse(DecimalUtils.gt(BigDecimal.valueOf(100), BigDecimal.valueOf(100)));
        Assertions.assertTrue(DecimalUtils.gt(BigDecimal.valueOf(123), BigDecimal.valueOf(100)));
    }

    @Test
    public void test_gte() {
        Assertions.assertFalse(DecimalUtils.gte(BigDecimal.valueOf(100), BigDecimal.valueOf(123)));
        Assertions.assertTrue(DecimalUtils.gte(BigDecimal.valueOf(100), BigDecimal.valueOf(100)));
        Assertions.assertTrue(DecimalUtils.gte(BigDecimal.valueOf(123), BigDecimal.valueOf(100)));
    }
}
