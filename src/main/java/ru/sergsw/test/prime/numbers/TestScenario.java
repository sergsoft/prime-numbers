package ru.sergsw.test.prime.numbers;

import lombok.*;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestScenario {
    @Singular
    private List<Integer> maxValues;

    @Singular
    private List<Integer> blockSizes;

    private Set<TestExecutors> executors;
}
