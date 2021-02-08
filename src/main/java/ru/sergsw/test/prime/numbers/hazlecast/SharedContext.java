package ru.sergsw.test.prime.numbers.hazlecast;

import lombok.Getter;
import ru.sergsw.test.prime.numbers.calculators.Context;

import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

public class SharedContext implements Context {
    private final SortedSet<Integer> sharedContext;
    @Getter
    private final Set<Integer> blockArray = new ConcurrentSkipListSet<>();

    public SharedContext(SortedSet<Integer> sharedContext) {
        this.sharedContext = sharedContext;
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
