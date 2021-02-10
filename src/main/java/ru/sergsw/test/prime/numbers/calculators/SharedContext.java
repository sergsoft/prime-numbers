package ru.sergsw.test.prime.numbers.calculators;

import lombok.Getter;

import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

public class SharedContext implements Context {
    private final SortedSet<Integer> sharedContext = new ConcurrentSkipListSet<>();
    @Getter
    private final Set<Integer> blockArray = new ConcurrentSkipListSet<>();

    public void reset() {
        blockArray.clear();
        sharedContext.clear();
    }

    @Override
    public long calcSize() {
        return sharedContext.size();
    }

    @Override
    public SortedSet<Integer> getSimpleNums() {
        return sharedContext;
    }

    @Override
    public void addValue(int val) {
        blockArray.add(val);
    }

    @Override
    public void flush() {
        sharedContext.addAll(blockArray);
        blockArray.clear();
    }
}
