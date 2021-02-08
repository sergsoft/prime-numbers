package ru.sergsw.test.prime.numbers.calculators.splitterators;

import ru.sergsw.test.prime.numbers.calculators.Task;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleSpliterator implements TaskSpliterator {
    private static final int TASK_SIZE = 10_000;
    private final List<Set<Task>> schedule;

    public SimpleSpliterator(Task mainTask) {
        int start = mainTask.getFrom();
        Set<Task> tasks = new HashSet<>();
        while (start <= mainTask.getTo()) {
            Task task = Task.builder()
                    .from(start)
                    .to(Math.min(mainTask.getTo(), start + TASK_SIZE))
                    .build();
            tasks.add(task);
            start += TASK_SIZE;
        }
        schedule = Collections.singletonList(tasks);
    }

    @Override
    public List<Set<Task>> getSchedule() {
        return schedule;
    }
}
