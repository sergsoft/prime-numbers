package ru.sergsw.test.prime.numbers.calculators.splitterators;

import ru.sergsw.test.prime.numbers.calculators.Task;

import java.util.List;
import java.util.Set;

public interface TaskSpliterator {
    List<Set<Task>> getSchedule();
}
