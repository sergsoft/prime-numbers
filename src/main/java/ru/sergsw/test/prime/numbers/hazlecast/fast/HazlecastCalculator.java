package ru.sergsw.test.prime.numbers.hazlecast.fast;

import lombok.extern.slf4j.Slf4j;
import ru.sergsw.test.prime.numbers.calculators.Calculator;
import ru.sergsw.test.prime.numbers.calculators.SharedContext;
import ru.sergsw.test.prime.numbers.hazlecast.HazelcastGlobalContext;
import ru.sergsw.test.prime.numbers.hazlecast.Input;

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
        ExecutorService executor = HazelcastGlobalContext.getExecutor();
        Calculator calculator = (Calculator) HazelcastGlobalContext.getInjector().getInstance(input.getCalculator());
        SharedContext context = HazelcastGlobalContext.SHARED_CONTEXT.get();

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
        log.debug("calculated block: {}", ret);
        return Output.builder()
                .result(ret)
                .resultBlock(context.getBlockArray().stream().mapToInt(Integer::intValue).toArray())
                .build();
    }
}
