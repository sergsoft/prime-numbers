package ru.sergsw.test.prime.numbers.calculators;

import java.util.SortedSet;

public interface Context {
    default void warmup(int blockSize) {
        addValue(2);
        for (int i = 3; i <= Math.sqrt(blockSize); i+=2) {
            if (PlainCalculator.checkNum(i)) {
                addValue(i);
            }
        }
        flush();
    }

    long calcSize();

    SortedSet<Integer> getSimpleNums();

    void addValue(int val);

    void flush();
}
