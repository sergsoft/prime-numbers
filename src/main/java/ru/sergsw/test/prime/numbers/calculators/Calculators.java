package ru.sergsw.test.prime.numbers.calculators;

import lombok.Getter;

@Getter
public enum Calculators {
    SLOW(SlowCalculator.class),
    PLAIN(PlainCalculator.class),
    FAST(FastCalculator.class);

    private final Class<? extends Calculator> calculatorClass;

    Calculators(Class<? extends Calculator> calculatorClass) {
        this.calculatorClass = calculatorClass;
    }
}
