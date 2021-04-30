package ru.sergsw.test.prime.numbers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import ru.sergsw.test.prime.numbers.calculators.Calculators;
import ru.sergsw.test.prime.numbers.calculators.Task;
import ru.sergsw.test.prime.numbers.config.GuiceContextAware;
import ru.sergsw.test.prime.numbers.config.MainModule;
import ru.sergsw.test.prime.numbers.hazlecast.HazelcastGlobalContext;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
            .maxValue(50_000_000)
            .executors(EnumSet.of(TestExecutors.MULTI_THREAD, TestExecutors.SINGLE_THREAD, TestExecutors.HAZLECAST))
            .blockSize(100)
            .blockSize(1_000)
            .blockSize(10_000)
            .blockSize(100_000)
            .calculators(EnumSet.of(Calculators.PLAIN, Calculators.FAST))
            .build();
    private static final DateTimeFormatter CSV_TS_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-hhmmss");

    public static final String MODE_PARAM = "m";
    public static final String TEST_MODE = "TEST";
    public static final String HELP_PARAM = "h";
    public static final String OUT_PATH_PARAM = "op";
    public static final String REPEAT_PARAM = "t";
    public static final String TEST_CASE_PARAM = "tc";

    @SneakyThrows
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(makeOptions(), args);
        if (cmd.hasOption(HELP_PARAM)) {
            printHelp();
            return;
        }

        String mode = cmd.getOptionValue(MODE_PARAM, TEST_MODE);

        RunOptions runOptions = RunOptions.builder()
                .repeatCnt(Integer.parseInt(cmd.getOptionValue(REPEAT_PARAM, "1")))
                .outPath(cmd.getOptionValue(OUT_PATH_PARAM, ""))
                .testScenario(parseScenario(cmd.getOptionValue(TEST_CASE_PARAM), SCENARIO))
                .build();

        if (TEST_MODE.equals(mode)) {
            runTests(runOptions);
        } else {
            runSlave();
        }
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("prime-numbers", makeOptions(), true);
    }

    private static Options makeOptions() {
        return new Options()
                .addOption(Option.builder(MODE_PARAM)
                        .longOpt("mode")
                        .argName("mode")
                        .hasArg()
                        .desc("Mode. Supported: test, slave. (Default: test)")
                        .build())
                .addOption(Option.builder(HELP_PARAM)
                        .longOpt("help")
                        .desc("Show this page")
                        .build())
                .addOption(Option.builder(OUT_PATH_PARAM)
                        .longOpt("out-path")
                        .hasArg()
                        .argName("path")
                        .desc("Define output folder. (Default: {current folder})")
                        .build())
                .addOption(Option.builder(REPEAT_PARAM)
                        .longOpt("repeat")
                        .hasArg()
                        .argName("nTimes")
                        .desc("Run this test case N times. (Default: 1)")
                        .build())
                .addOption(Option.builder(TEST_CASE_PARAM)
                        .longOpt("test-case")
                        .hasArg()
                        .argName("jsonPath")
                        .desc("Run test case from {jsonPath}. (Default: {built-in test case})")
                        .build())
                ;
    }

    private static TestScenario parseScenario(String jsonPath, TestScenario scenario) {
        if (jsonPath == null) {
            return scenario;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new File(jsonPath), TestScenario.class);
        } catch (IOException e) {
            log.error("Error on reading test case from {}", jsonPath, e);
            throw new RuntimeException(e);
        }
    }

    private static void runTests(RunOptions runOptions) {
        Injector injector = Guice.createInjector(new MainModule());
        GuiceContextAware.INJECTOR.set(injector);
        TestScenario testScenario = runOptions.getTestScenario();

        List<Application> executors =
                testScenario.getExecutors().stream()
                        .map(TestExecutors::getAClass)
                        .map(injector::getInstance)
                        .collect(Collectors.toList());

        try {
            Statistic statistic = new Statistic();
            for (int i = 1; i <= runOptions.getRepeatCnt(); i++) {
                log.info("Start {} iteration out of {}", i, runOptions.getRepeatCnt());
                for (Integer max : testScenario.getMaxValues()) {
                    Task mainTask = Task.builder()
                            .from(3)
                            .to(max)
                            .build();

                    log.info("Task: {}", mainTask);

                    executors.forEach(application -> application.execute(mainTask, statistic, testScenario));
                }
            }
            String csvFile = String.format("stat-%s.csv", LocalDateTime.now().format(CSV_TS_FORMATTER));
            statistic.writeToCsv(preparePath(csvFile, runOptions.getOutPath()));
            log.info(statistic.getSummary());
            log.info("Save result into: {}", csvFile);
        } finally {
            HazelcastGlobalContext.shutdown();
        }
    }

    static String preparePath(String csvFile, String outPath) {
        if (outPath == null) {
            return csvFile;
        }
        if (outPath.length() > 0 && !"\\".equals(outPath.substring(outPath.length() - 1))) {
            return outPath + "\\" + csvFile;
        }
        return outPath + csvFile;
    }

    @SneakyThrows
    private static void runSlave() {
        Injector injector = Guice.createInjector(new MainModule());
        GuiceContextAware.INJECTOR.set(injector);
        HazelcastInstance hzInstance = Hazelcast.newHazelcastInstance();
        HazelcastGlobalContext.HAZELCAST_INSTANCE.set(hzInstance);
        while (true) {
            log.info("Slave echo");
            Thread.sleep(10_000);
        }
    }
}
