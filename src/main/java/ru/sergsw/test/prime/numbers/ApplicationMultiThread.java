package ru.sergsw.test.prime.numbers;

import com.google.common.base.Stopwatch;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import ru.sergsw.test.prime.numbers.calculators.Calculator;
import ru.sergsw.test.prime.numbers.calculators.Context;
import ru.sergsw.test.prime.numbers.calculators.SharedContext;
import ru.sergsw.test.prime.numbers.calculators.Task;
import ru.sergsw.test.prime.numbers.calculators.splitterators.TaskSpliterator;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
public class ApplicationMultiThread implements Application {
    @Inject
    private Set<Calculator> calculatorList;

    @SneakyThrows
    @Override
    public void execute(Task task, Statistic statistic, TestScenario testScenario) {
        log.info("---------------------------------------------Multi thread processing------------------------------------------------------");
        int cores = Runtime.getRuntime().availableProcessors();
        log.info("Thread count: {}", cores);
        ExecutorService executor = Executors.newFixedThreadPool(cores);
        try {
            for (Calculator calculator : calculatorList) {
                ApplicationMultiThread.log.info("===================={}===================", calculator.name());
                TaskSpliterator spliterator = calculator.spliterator(task);
                if (spliterator == null) {
                    log.warn("Not supported spliterator. Skip multi-thread execution.");
                    break;
                }
                ApplicationMultiThread.log.info("Start calculate");
                configureAndRun(testScenario, calculator, blockSize ->
                        runTest(blockSize, task, statistic, executor, calculator));
            }
        } finally {
            executor.shutdown();
        }
    }

    @Override
    public Logger getLog() {
        return log;
    }

    private void runTest(Integer blockSize,
                         Task task,
                         Statistic statistic,
                         ExecutorService executor,
                         Calculator calculator)
            throws InterruptedException {

        TaskSpliterator spliterator = calculator.spliterator(task);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            int ret = 0;
            Context context = new SharedContext();
            context.warmup(calculator.getBlockSize());
            List<Set<Task>> taskSchedule = spliterator.getSchedule();
            for (Set<Task> tasks : taskSchedule) {
                Set<Callable<Integer>> execTasks = tasks.stream()
                        .map(t -> (Callable<Integer>) () -> calculator.calc(t, context))
                        .collect(Collectors.toSet());
                List<Future<Integer>> futureList = executor.invokeAll(execTasks);
                ret += futureList.stream()
                        .mapToInt(integerFuture -> {
                            try {
                                return integerFuture.get();
                            } catch (InterruptedException | ExecutionException e) {
                                log.error("Error", e);
                                throw new RuntimeException(e);
                            }
                        }).sum();
                context.flush();
            }
            Duration elapsed = stopwatch.elapsed();
            statistic.add(Statistic.Record.builder()
                    .calculatorName(calculator.name())
                    .executor("MultiThread")
                    .execTime(elapsed)
                    .result(ret)
                    .contextSize(context.calcSize())
                    .taskSize(task.getTo())
                    .blockSize(blockSize)
                    .build());
            ApplicationMultiThread.log.info("Result: {}", ret);
        } finally {
            ApplicationMultiThread.log.info("Processed in {}", stopwatch.toString());
        }
    }
}
