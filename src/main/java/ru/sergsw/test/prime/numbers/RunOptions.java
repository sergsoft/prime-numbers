package ru.sergsw.test.prime.numbers;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RunOptions {
    int repeatCnt;
    String outPath;
    TestScenario testScenario;
}
