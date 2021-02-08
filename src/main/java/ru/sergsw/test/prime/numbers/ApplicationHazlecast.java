package ru.sergsw.test.prime.numbers;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import lombok.extern.slf4j.Slf4j;
import ru.sergsw.test.prime.numbers.calculators.Calculator;
import ru.sergsw.test.prime.numbers.calculators.LocalContext;
import ru.sergsw.test.prime.numbers.calculators.Task;
import ru.sergsw.test.prime.numbers.calculators.splitterators.TaskSpliterator;
import ru.sergsw.test.prime.numbers.hazlecast.HazelcastContext;
import ru.sergsw.test.prime.numbers.hazlecast.HazlecastCalculator;
import ru.sergsw.test.prime.numbers.hazlecast.Input;
import ru.sergsw.test.prime.numbers.hazlecast.Output;

import javax.inject.Inject;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
public class ApplicationHazlecast implements Application {
    private static final int TASK_SIZE = 8;
    @Inject
    private Set<Calculator> calculatorList;

    private final HazelcastInstance hzInstance;

    public ApplicationHazlecast() {
        hzInstance = Hazelcast.newHazelcastInstance();
    }

    @Override
    public void execute(Task task, Statistic statistic) {
        IExecutorService executor = hzInstance.getExecutorService("calculator");
        HazelcastContext.HAZELCAST_INSTANCE.set(hzInstance);
        log.info("---------------------------------------------Hazelcast processing------------------------------------------------------");
        Set<Member> members = hzInstance.getCluster().getMembers();
        log.info("Nodes: {}", members.size());
        log.info("Node details: {}", members.stream().map(Member::getAddress).map(Objects::toString).collect(Collectors.joining(", ")));
        try {
            for (Calculator calculator : calculatorList) {
                log.info("===================={}===================", calculator.name());
                TaskSpliterator spliterator = calculator.spliterator(task);
                if (spliterator == null) {
                    log.warn("Not supported spliterator. Skip Hazelcast execution.");
                    break;
                }
                log.info("Start calculate");

                SortedSet<Integer> cache = new TreeSet<>();
                LocalContext localContext = new LocalContext();
                localContext.warmup(calculator.getBlockSize());
                cache.addAll(localContext.getSimpleNums());

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
                                    .cache(cache)
                                    .build();
                            futureList.add(executor.submit(new HazlecastCalculator(input)));
                        }

                        ret += futureList.stream()
                                .mapToInt(outputFuture -> {
                                    try {
                                        Output output = outputFuture.get();
                                        cache.addAll(output.getResultBlock());
                                        return output.getResult();
                                    } catch (InterruptedException | ExecutionException e) {
                                        log.error("Error", e);
                                        throw new RuntimeException(e);
                                    }
                                }).sum();
                    }
                    Duration elapsed = stopwatch.elapsed();
                    statistic.add(Statistic.Record.builder()
                            .calculatorName(calculator.name())
                            .executor("Hazlecast")
                            .execTime(elapsed)
                            .result(ret)
                            .contextSize(cache.size())
                            .taskSize(task.getTo())
                            .build());
                    log.info("Result: {}", ret);
                } finally {
                    log.info("Processed in {}", stopwatch.toString());
                }
            }
        } catch (Throwable t) {
            log.error("Error", t);
        }
    }
}
