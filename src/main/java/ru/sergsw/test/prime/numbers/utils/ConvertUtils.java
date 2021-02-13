package ru.sergsw.test.prime.numbers.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ConvertUtils {

    public String toSec(double inNs) {
        return String.format("%.5fs", inNs / 1_000_000_000);
    }

    public String toMs(double inNs) {
        return String.format("%.5fms", inNs / 1_000_000);
    }

    public String toMb(double inNs) {
        return String.format("%.3fMb", inNs / (1_024 * 1_024));
    }
}
