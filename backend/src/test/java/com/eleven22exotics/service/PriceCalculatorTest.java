package com.eleven22exotics.service;

import com.eleven22exotics.entity.Car;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PriceCalculatorTest {
    private final LocalDate start = LocalDate.of(2030, 1, 1);

    private Car car() {
        Car c = new Car();
        c.setDailyPrice(new BigDecimal("1000"));
        c.setMonthlyPrice(new BigDecimal("20000"));
        return c;
    }

    private BigDecimal price(int days) { return PriceCalculator.calculate(car(), start, start.plusDays(days)); }

    @Test void usesDailyPriceUnderThirtyDays() {
        assertEquals(0, new BigDecimal("1000").compareTo(price(1)));
        assertEquals(0, new BigDecimal("29000").compareTo(price(29)));
    }

    @Test void usesMonthlyPriceFromThirtyDays() { assertEquals(0, new BigDecimal("20000").compareTo(price(30))); }

    @Test void combinesMonthlyAndDailyPrice() {
        assertEquals(0, new BigDecimal("25000").compareTo(price(35)));
        assertEquals(0, new BigDecimal("45000").compareTo(price(65)));
    }

    @Test void rejectsEmptyOrNegativeRanges() {
        assertThrows(IllegalArgumentException.class, () -> price(0));
        assertThrows(IllegalArgumentException.class, () -> price(-3));
    }
}
