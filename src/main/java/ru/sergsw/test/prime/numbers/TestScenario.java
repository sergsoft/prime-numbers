package ru.sergsw.test.prime.numbers;

import lombok.*;
import ru.sergsw.test.prime.numbers.calculators.Calculators;

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

    private Set<Calculators> calculators;
}
