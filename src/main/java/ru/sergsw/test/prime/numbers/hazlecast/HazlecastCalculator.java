package ru.sergsw.test.prime.numbers.hazlecast;

import lombok.extern.slf4j.Slf4j;
import ru.sergsw.test.prime.numbers.calculators.Calculator;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
public class HazlecastCalculator implements Callable<Output>, Serializable {
    private final Input input;

    public HazlecastCalculator(Input input) {
        this.input = input;
    }

    @Override
    public Output call() throws Exception {
        ExecutorService executor = HazelcastContext.getExecutor();
        Calculator calculator = (Calculator) HazelcastContext.GUICE_INJECTOR.get().getInstance(input.getCalculator());
        SharedContext context = new SharedContext(input.getCache());

        Set<Callable<Integer>> set = input.getTasks().stream()
                .map(task -> (Callable<Integer>) () -> calculator.calc(task, context))
                .collect(Collectors.toSet());
        List<Future<Integer>> futureList = executor.invokeAll(set);

        int ret = futureList.stream()
                .mapToInt(value -> {
                    try {
                        return value.get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Error", e);
                        throw new RuntimeException(e);
                    }
                })
                .sum();
        return Output.builder()
                .result(ret)
                .resultBlock(context.getBlockArray())
                .build();
    }
}
