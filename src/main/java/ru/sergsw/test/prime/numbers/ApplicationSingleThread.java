package ru.sergsw.test.prime.numbers;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import ru.sergsw.test.prime.numbers.calculators.Calculator;
import ru.sergsw.test.prime.numbers.calculators.LocalContext;
import ru.sergsw.test.prime.numbers.calculators.Task;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Set;

@Slf4j
public class ApplicationSingleThread extends AbstractApplication {
    @Override
    protected String getExecutorName() {
        return "SingleThread";
    }

    @Inject
    private Set<Calculator> calculatorList;

    @Override
    public void execute(Task task, Statistic statistic, TestScenario testScenario) {
        log.info("---------------------------------------------Single thread processing------------------------------------------------------");
        for (Calculator calculator : calculatorList) {
            log.info("===================={}===================", calculator.name());
            log.info("Start calculate");
            try {
                configureAndRun(testScenario, calculator, blockSize -> runTest(task, calculator), statistic);
            } catch (Exception e) {
                log.error("Error on execution", e);
            }
        }
    }

    @Override
    public Logger getLog() {
        return log;
    }

    private TestResult runTest(Task task, Calculator calculator) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            LocalContext context = new LocalContext();
            context.getSimpleNums().add(2);
            int calc = calculator.calc(task, context);

            Duration elapsed = stopwatch.elapsed();
            log.info("Result: {}", calc);
            return TestResult.builder()
                    .duration(elapsed)
                    .result(calc)
                    .taskSize(task.getTo())
                    .build();
        } finally {
            log.info("Processed in {}", stopwatch.toString());
        }
    }
}
