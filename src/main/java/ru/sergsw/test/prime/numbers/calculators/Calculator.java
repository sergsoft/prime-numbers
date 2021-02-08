package ru.sergsw.test.prime.numbers.calculators;

import ru.sergsw.test.prime.numbers.calculators.splitterators.SimpleSpliterator;
import ru.sergsw.test.prime.numbers.calculators.splitterators.TaskSpliterator;

public interface Calculator {
    default String name() {
        return getClass().getSimpleName();
    }

    default TaskSpliterator spliterator(Task mainTask) {
        return new SimpleSpliterator(mainTask);
    }

    int calc(Task task, Context context);

    default int getBlockSize() {
        return -1;
    }
}
