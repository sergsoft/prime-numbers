package ru.sergsw.test.prime.numbers;

import ru.sergsw.test.prime.numbers.calculators.Task;

public interface Application {
    void execute(Task task, Statistic statistic, TestScenario testScenario);
}
