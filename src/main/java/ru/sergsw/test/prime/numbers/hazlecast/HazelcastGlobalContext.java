package ru.sergsw.test.prime.numbers.hazlecast;

import com.google.inject.Injector;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import ru.sergsw.test.prime.numbers.calculators.SharedContext;
import ru.sergsw.test.prime.numbers.config.GuiceContextAware;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class HazelcastGlobalContext {
    public static final int TASK_SIZE_DEF = 1024;
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

    public static HazelcastInstance getHazelcastInstance() {
        HazelcastInstance hazelcastInstance = HAZELCAST_INSTANCE.get();
        if (hazelcastInstance == null) {
            hazelcastInstance = Hazelcast.newHazelcastInstance();
            HAZELCAST_INSTANCE.set(hazelcastInstance);
        }
        return hazelcastInstance;
    }

    public static Injector getInjector() {
        return GuiceContextAware.INJECTOR.get();
    }
}
