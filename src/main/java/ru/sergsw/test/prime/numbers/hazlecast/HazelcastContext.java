package ru.sergsw.test.prime.numbers.hazlecast;

import com.google.inject.Injector;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import ru.sergsw.test.prime.numbers.calculators.SharedContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class HazelcastContext {
    public static final AtomicReference<Injector> GUICE_INJECTOR = new AtomicReference<>();
    public static final AtomicReference<ExecutorService> EXECUTOR = new AtomicReference<>();
    public static final AtomicReference<HazelcastInstance> HAZELCAST_INSTANCE = new AtomicReference<>();
    public static final AtomicReference<SharedContext> SHARED_CONTEXT = new AtomicReference<>(new SharedContext());

    public static void shutdown() {
        ExecutorService executor = EXECUTOR.get();
        if (executor != null) {
            executor.shutdown();
        }
        HazelcastInstance hazelcastInstance = HAZELCAST_INSTANCE.get();
        if (hazelcastInstance != null) {
            hazelcastInstance.getCluster().shutdown();
            hazelcastInstance.shutdown();
        }
    }

    public static ExecutorService getExecutor() {
        ExecutorService executor = EXECUTOR.get();
        if (executor == null) {
            int cores = Runtime.getRuntime().availableProcessors();
            log.info("Thread count: {}", cores);
            executor = Executors.newFixedThreadPool(cores);
            EXECUTOR.set(executor);
        }
        return executor;
    }
}
