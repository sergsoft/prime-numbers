package ru.sergsw.test.prime.numbers.hazlecast;

import ru.sergsw.test.prime.numbers.calculators.SharedContext;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Callable;

public class HazlecastSyncCache implements Callable<Void>, Serializable {
    private final Collection<Integer> cacheDiff;

    public HazlecastSyncCache(Collection<Integer> cacheDiff) {
        this.cacheDiff = cacheDiff;
    }

    @Override
    public Void call() throws Exception {
        SharedContext sharedContext = HazelcastContext.SHARED_CONTEXT.get();
        sharedContext.flush();
        sharedContext.getSimpleNums().addAll(cacheDiff);
        return null;
    }
}
