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
public class ApplicationSingleThread implements Application {
    @Inject
    private Set<Calculator> calculatorList;

    @Override
    public void execute(Task task, Statistic statistic, TestScenario testScenario) {
        log.info("---------------------------------------------Single thread processing------------------------------------------------------");
        for (Calculator calculator : calculatorList) {
            ApplicationSingleThread.log.info("===================={}===================", calculator.name());
            ApplicationSingleThread.log.info("Start calculate");
            try {
                configureAndRun(testScenario, calculator, blockSize -> runTest(blockSize, task, statistic, calculator));
            } catch (Exception e) {
                log.error("Error on execution", e);
            }
        }
    }

    @Override
    public Logger getLog() {
        return log;
    }

    private void runTest(Integer blockSize, Task task, Statistic statistic, Calculator calculator) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            LocalContext context = new LocalContext();
            context.getSimpleNums().add(2);
            int calc = calculator.calc(task, context);

            Duration elapsed = stopwatch.elapsed();
            ApplicationSingleThread.log.info("Result: {}", calc);
            statistic.add(Statistic.Record.builder()
                    .calculatorName(calculator.name())
                    .executor("SingleThread")
                    .execTime(elapsed)
                    .result(calc)
                    .contextSize(context.calcSize())
                    .taskSize(task.getTo())
                    .blockSize(blockSize)
                    .build());
        } finally {
            ApplicationSingleThread.log.info("Processed in {}", stopwatch.toString());
        }
    }
}
