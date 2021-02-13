package ru.sergsw.test.prime.numbers;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import ru.sergsw.test.prime.numbers.calculators.Calculator;
import ru.sergsw.test.prime.numbers.calculators.LocalContext;
import ru.sergsw.test.prime.numbers.calculators.Task;
import ru.sergsw.test.prime.numbers.calculators.splitterators.TaskSpliterator;
import ru.sergsw.test.prime.numbers.hazlecast.*;

import javax.inject.Inject;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
public class ApplicationHazlecast extends AbstractApplication {
    @Override
    protected String getExecutorName() {
        return "Hazlecast";
    }

    private static final int TASK_SIZE = 8;
    @Inject
    private Set<Calculator> calculatorList;

    private final HazelcastInstance hzInstance;

    public ApplicationHazlecast() {
        hzInstance = Hazelcast.newHazelcastInstance();
    }

    @Override
    public void execute(Task task, Statistic statistic, TestScenario testScenario) {
        IExecutorService executor = hzInstance.getExecutorService("calculator");
        HazelcastGlobalContext.HAZELCAST_INSTANCE.set(hzInstance);
        log.info("---------------------------------------------Hazelcast processing------------------------------------------------------");
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

                configureAndRun(testScenario, calculator, blockSize ->
                        runTest(task, executor, calculator), statistic);
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
                         IExecutorService executor,
                         Calculator calculator) {
        TaskSpliterator spliterator = calculator.spliterator(task);
        LocalContext localContext = new LocalContext();
        localContext.warmup(calculator.getBlockSize());
        executor.submitToAllMembers(new HazlecastResetCache(localContext.getSimpleNums()))
                .forEach((member, voidFuture) -> {
                    try {
                        voidFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Error on cache reset", e);
                    }
                });

        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            int ret = 0;
            List<Set<Task>> taskSchedule = spliterator.getSchedule();
            for (Set<Task> tasks : taskSchedule) {
                List<Future<Output>> futureList = new ArrayList<>();

                for (List<Task> taskList : Iterables.partition(tasks, TASK_SIZE)) {
                    Input input = Input.builder()
                            .calculator(calculator.getClass())
                            .tasks(new ArrayList<>(taskList))
                            .build();
                    futureList.add(executor.submit(new HazlecastCalculator(input)));
                }

                List<int[]> diff = new ArrayList<>();
                ret += futureList.stream()
                        .mapToInt(outputFuture -> {
                            try {
                                Output output = outputFuture.get();
                                diff.add(output.getResultBlock());
                                return output.getResult();
                            } catch (InterruptedException | ExecutionException e) {
                                log.error("Error", e);
                                throw new RuntimeException(e);
                            }
                        }).sum();

                Collection<Future<Void>> allSync = executor.submitToAllMembers(new HazlecastSyncCache(diff)).values();
                allSync.forEach(voidFuture -> {
                    try {
                        voidFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Error on cache sync", e);
                    }
                });
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
