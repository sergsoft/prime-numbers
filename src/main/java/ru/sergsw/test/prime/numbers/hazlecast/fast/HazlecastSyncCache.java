package ru.sergsw.test.prime.numbers.hazlecast.fast;

import ru.sergsw.test.prime.numbers.calculators.Context;
import ru.sergsw.test.prime.numbers.hazlecast.HazelcastGlobalContext;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;

public class HazlecastSyncCache implements Callable<Void>, Serializable {
    private final int[] cacheDiff;

    public HazlecastSyncCache(int[] cacheDiff) {
        this.cacheDiff = cacheDiff;
    }

    public HazlecastSyncCache(List<int[]> diff) {
        int cnt = diff.stream().mapToInt(ints -> ints.length).sum();
        cacheDiff = new int[cnt];
        int i = 0;
        for (int[] ints : diff) {
            for (int anInt : ints) {
                cacheDiff[i] = anInt;
                i++;
            }
        }
    }

    @Override
    public Void call() throws Exception {
        Context sharedContext = HazelcastGlobalContext.SHARED_CONTEXT.get();
        sharedContext.flush();
        for (int i : cacheDiff) {
            sharedContext.getSimpleNums().add(i);
        }
        return null;
    }
}
