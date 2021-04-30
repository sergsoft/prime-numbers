package ru.sergsw.test.prime.numbers.hazlecast.partition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sergsw.test.prime.numbers.calculators.Calculator;
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
@RequiredArgsConstructor
public class SpecialFastCalculatorTask implements Callable<Integer>, Serializable {
    private final Input input;

    @Override
    public Integer call() throws Exception {
        ExecutorService executor = HazelcastGlobalContext.getExecutor();
        Calculator calculator = SpecialFastCalculator.CALCULATOR;

        Set<Callable<Integer>> set = input.getTasks().stream()
                .map(task -> (Callable<Integer>) () -> calculator.calc(task, null))
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
        return ret;
    }
}
