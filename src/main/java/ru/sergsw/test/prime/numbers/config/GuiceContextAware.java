package ru.sergsw.test.prime.numbers.config;

import com.google.inject.Injector;

import java.util.concurrent.atomic.AtomicReference;

public interface GuiceContextAware {
    AtomicReference<Injector> INJECTOR = new AtomicReference<>();

    default Injector getInjector() {
        return INJECTOR.get();
    }
}
