package com.eleven22exotics.service;

import com.eleven22exotics.entity.Car;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Under 30 days: daily price. From 30 days: monthly price for each full 30-day block plus daily price for the rest. */
public final class PriceCalculator {
    private PriceCalculator() {}

    public static BigDecimal calculate(Car car, LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end);
        if (days <= 0) throw new IllegalArgumentException("End date must be after start date");
        if (days >= 30) {
            long months = days / 30, rest = days % 30;
            return car.getMonthlyPrice().multiply(BigDecimal.valueOf(months))
                    .add(car.getDailyPrice().multiply(BigDecimal.valueOf(rest)));
        }
        return car.getDailyPrice().multiply(BigDecimal.valueOf(days));
    }
}
