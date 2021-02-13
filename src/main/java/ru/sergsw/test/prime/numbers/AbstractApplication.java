package ru.sergsw.test.prime.numbers;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.slf4j.Logger;
import ru.sergsw.test.prime.numbers.calculators.Calculator;
import ru.sergsw.test.prime.numbers.calculators.ConfigurableCalculator;

import java.time.Duration;

public abstract class AbstractApplication implements Application {
    protected void addStatisticRecord(Statistic statistic,
                                    Calculator calculator,
                                    TestResult testResult,
                                    Integer blockSize,
                                    JvmTool tool) {
        Statistic.Record record = Statistic.Record.builder()
                .calculatorName(calculator.name())
                .executor(getExecutorName())
                .result(testResult.getResult())
                .taskSize(testResult.getTaskSize())
                .blockSize(blockSize)
                .build();
        record.addDuration(testResult.getDuration());
        record.addMemoryConsumption(tool.getMemoryConsumption());

        statistic.add(record);
    }

    protected void configureAndRun(TestScenario testScenario,
                                 Calculator calculator,
                                 TestRunner testRunner,
                                 Statistic statistic) throws Exception {
        JvmTool tool = new JvmTool();
        if (calculator instanceof ConfigurableCalculator) {
            for (Integer blockSize : testScenario.getBlockSizes()) {
                getLog().info("Configure with block size: {} <------------------------------", blockSize);
                ConfigurableCalculator configurableCalculator = (ConfigurableCalculator) calculator;
                configurableCalculator.setBlockSize(blockSize);
                TestResult result = testRunner.runTest(blockSize);
                tool.finish();
                addStatisticRecord(statistic, calculator, result, blockSize, tool);
            }
        } else {
            TestResult result = testRunner.runTest(null);
            tool.finish();
            addStatisticRecord(statistic, calculator, result, null, tool);
        }
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        getLog().info("Free memory: " + runtime.freeMemory());
    }

    protected abstract Logger getLog();

    protected String getExecutorName() {
        return getClass().getSimpleName();
    }

    public interface TestRunner {
        TestResult runTest(Integer blockSize) throws Exception;
    }

    @Value
    @Builder
    public static class TestResult{
        Duration duration;
        int result;
        int taskSize;
    }

    private static class JvmTool {
        private final long memoryUsed;
        @Getter
        private long memoryConsumption;

        public JvmTool() {
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();
            memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        }

        public void finish() {
            Runtime runtime = Runtime.getRuntime();
            memoryConsumption = runtime.totalMemory() - runtime.freeMemory() - memoryUsed;
            runtime.gc();
        }
    }
}
