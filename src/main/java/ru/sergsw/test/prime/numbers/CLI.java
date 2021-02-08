package ru.sergsw.test.prime.numbers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import ru.sergsw.test.prime.numbers.calculators.Task;
import ru.sergsw.test.prime.numbers.config.MainModule;
import ru.sergsw.test.prime.numbers.hazlecast.HazelcastContext;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CLI {
    private static final TestScenario SCENARIO = TestScenario.builder()
            .maxValue(100_000)
            .maxValue(500_000)
            .maxValue(1_000_000)
            .maxValue(5_000_000)
            .maxValue(10_000_000)
            .executors(EnumSet.of(TestExecutors.MULTI_THREAD, TestExecutors.SINGLE_THREAD, TestExecutors.HAZLECAST))
            .blockSize(10)
            .blockSize(100)
            .blockSize(1_000)
            .blockSize(10_000)
            .blockSize(100_000)
            .build();

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new MainModule());
        HazelcastContext.GUICE_INJECTOR.set(injector);
        TestScenario testScenario = SCENARIO;

        List<Application> executors =
                testScenario.getExecutors().stream()
                        .map(TestExecutors::getAClass)
                        .map(injector::getInstance)
                        .collect(Collectors.toList());

        try {
            Statistic statistic = new Statistic();
            for (Integer max : testScenario.getMaxValues()) {
                Task mainTask = Task.builder()
                        .from(3)
                        .to(max)
                        .build();

                log.info("Task: {}", mainTask);

                executors.forEach(application -> application.execute(mainTask, statistic));
            }

            String csvFile = "stat.csv";
            statistic.writeToCsv(csvFile);
            log.info("Save result into: {}", csvFile);
        } finally {
            HazelcastContext.shutdown();
        }
    }
}
