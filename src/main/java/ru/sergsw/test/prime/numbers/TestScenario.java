package ru.sergsw.test.prime.numbers;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Set;

@Value
@Builder
public class TestScenario {
    @Singular
    List<Integer> maxValues;

    @Singular
    List<Integer> blockSizes;

    Set<TestExecutors> executors;
}
