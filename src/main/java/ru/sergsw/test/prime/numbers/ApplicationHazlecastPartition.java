package ru.sergsw.test.prime.numbers;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import ru.sergsw.test.prime.numbers.calculators.Calculator;
import ru.sergsw.test.prime.numbers.calculators.FastCalculator;
import ru.sergsw.test.prime.numbers.calculators.Task;
import ru.sergsw.test.prime.numbers.calculators.splitterators.TaskSpliterator;
import ru.sergsw.test.prime.numbers.hazlecast.HazelcastGlobalContext;
import ru.sergsw.test.prime.numbers.hazlecast.Input;
import ru.sergsw.test.prime.numbers.hazlecast.partition.SmartContext;
import ru.sergsw.test.prime.numbers.hazlecast.partition.SpecialFastCalculator;
import ru.sergsw.test.prime.numbers.hazlecast.partition.SpecialFastCalculatorTask;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static ru.sergsw.test.prime.numbers.hazlecast.HazelcastGlobalContext.TASK_SIZE_DEF;

@Slf4j
public class ApplicationHazlecastPartition extends AbstractApplication {
    private static final int TASK_SIZE = TASK_SIZE_DEF;
    private final HazelcastInstance hzInstance;

    public ApplicationHazlecastPartition() {
        hzInstance = HazelcastGlobalContext.getHazelcastInstance();
    }

    @Override
    protected String getExecutorName() {
        return "Hazlecast-partition";
    }

    @Override
    protected void execute(Task task, Statistic statistic, TestScenario testScenario, List<? extends Calculator> calculatorList) {
        SmartContext smartContext = SmartContext.get();

        calculatorList = calculatorList.stream()
                .filter(calculator -> calculator instanceof FastCalculator)
                .map(c -> new SpecialFastCalculator()) // replace
                .collect(Collectors.toList());

        log.info("---------------------------------------------Hazelcast-Partition processing------------------------------------------------------");
        Set<Member> members = hzInstance.getCluster().getMembers();
        log.info("Nodes: {}", members.size());
        log.info("Node details: {}", members.stream()
                .map(Member::getAddress)
                .map(Objects::toString)
                .collect(Collectors.joining(", ")));


        try {
            for (Calculator calculator : calculatorList) {
                log.info("===================={}===================", calculator.name());
                TaskSpliterator spliterator = calculator.spliterator(task);
                if (spliterator == null) {
                    log.warn("Not supported spliterator. Skip Hazelcast execution.");
                    break;
                }
                log.info("Start calculate");

                simpleRun(testScenario, calculator, blockSize ->
                        runTest(task, calculator, smartContext), statistic);
            }
        } catch (Throwable t) {
            log.error("Error", t);
        }
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    private TestResult runTest(Task task,
                               Calculator calculator,
                               SmartContext smartContext) {
        TaskSpliterator spliterator = calculator.spliterator(task);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            int ret = 0;
            List<Set<Task>> taskSchedule = spliterator.getSchedule();

            for (Set<Task> tasks : taskSchedule) {

                List<Future<Integer>> futureList = new ArrayList<>();
                for (List<Task> taskList : Iterables.partition(tasks, TASK_SIZE)) {
                    Input input = Input.builder()
                            .calculator(calculator.getClass())
                            .tasks(new ArrayList<>(taskList))
                            .build();
                    futureList.add(smartContext.getExecutorService().submit(new SpecialFastCalculatorTask(input)));
                }

                ret += futureList.stream()
                        .mapToInt(integerFuture -> {
                            try {
                                return integerFuture.get();
                            } catch (InterruptedException | ExecutionException e) {
                                log.error("Error", e);
                                throw new RuntimeException(e);
                            }
                        }).sum();
            }
            Duration elapsed = stopwatch.elapsed();
            log.info("Result: {}", ret);
            return TestResult.builder()
                    .duration(elapsed)
                    .result(ret)
                    .taskSize(task.getTo())
                    .build();
        } finally {
            log.info("Processed in {}", stopwatch.toString());
        }
    }
}
