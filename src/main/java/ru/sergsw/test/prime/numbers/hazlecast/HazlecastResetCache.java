package ru.sergsw.test.prime.numbers.hazlecast;

import ru.sergsw.test.prime.numbers.calculators.Context;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Callable;

public class HazlecastResetCache implements Callable<Void>, Serializable {
    private final Collection<Integer> cacheInit;

    public HazlecastResetCache(Collection<Integer> cacheInit) {
        this.cacheInit = cacheInit;
    }

    @Override
    public Void call() throws Exception {
        Context sharedContext = HazelcastGlobalContext.SHARED_CONTEXT.get();
        sharedContext.reset();
        sharedContext.getSimpleNums().addAll(cacheInit);
        return null;
    }
}
