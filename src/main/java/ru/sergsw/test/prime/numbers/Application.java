package ru.sergsw.test.prime.numbers;

import org.slf4j.Logger;
import ru.sergsw.test.prime.numbers.calculators.Calculator;
import ru.sergsw.test.prime.numbers.calculators.ConfigurableCalculator;
import ru.sergsw.test.prime.numbers.calculators.Task;

public interface Application {
    void execute(Task task, Statistic statistic, TestScenario testScenario);

    default void configureAndRun(TestScenario testScenario, Calculator calculator, TestRunner testRunner) throws Exception {
        if (calculator instanceof ConfigurableCalculator) {
            for (Integer blockSize : testScenario.getBlockSizes()) {
                getLog().info("Configure with block size: {} <------------------------------", blockSize);
                ConfigurableCalculator configurableCalculator = (ConfigurableCalculator) calculator;
                configurableCalculator.setBlockSize(blockSize);
                testRunner.runTest(blockSize);
            }
        } else {
            testRunner.runTest(null);
        }
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        getLog().info("Free memory: " + runtime.freeMemory());
    }

    Logger getLog();

    interface TestRunner {
        void runTest(Integer blockSize) throws Exception;
    }
}
