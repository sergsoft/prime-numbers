package ru.sergsw.test.prime.numbers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.sergsw.test.prime.numbers.config.MainModule;
import ru.sergsw.test.prime.numbers.hazlecast.HazelcastContext;

@Slf4j
public class CLI_slave {
    @SneakyThrows
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new MainModule());
        HazelcastContext.GUICE_INJECTOR.set(injector);
        HazelcastInstance hzInstance = Hazelcast.newHazelcastInstance();
        HazelcastContext.HAZELCAST_INSTANCE.set(hzInstance);
        while (true) {
            log.info("Slave echo");
            Thread.sleep(10_000);
        }
    }
}
