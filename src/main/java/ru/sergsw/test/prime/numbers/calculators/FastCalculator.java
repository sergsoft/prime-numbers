package ru.sergsw.test.prime.numbers.calculators;

import ru.sergsw.test.prime.numbers.calculators.splitterators.TaskSpliterator;

import java.util.*;

public class FastCalculator implements Calculator {
    private static final int BATCH_SIZE = 10_000;

    @Override
    public int calc(Task task, Context context) {
        int from = task.getFrom() % 2 == 1 ? task.getFrom() : task.getFrom() + 1;
        int ret = 0;
        for (int i = from; i < task.getTo(); i += 2) {
            if (checkNum(i, context)) {
                ret++;
            }
        }
        return ret;
    }

    @Override
    public int getBlockSize() {
        return BATCH_SIZE;
    }

    @Override
    public TaskSpliterator spliterator(Task mainTask) {
        return new FastCalcSpliterator(mainTask);
    }

    private boolean checkNum(int num, Context context) {
        for (Integer simpleNum : context.getSimpleNums()) {
            if (simpleNum > Math.sqrt(num)) {
                break;
            }
            if (num % simpleNum == 0) {
                return false;
            }
        }
        context.addValue(num);
        return true;
    }

    private static class FastCalcSpliterator implements TaskSpliterator {
        private final List<Set<Task>> schedule;

        public FastCalcSpliterator(Task mainTask) {
            List<Set<Task>> list;
            if (mainTask.getTo() < BATCH_SIZE) {
                list = Collections.singletonList(Collections.singleton(mainTask));
            } else {
                list = new ArrayList<>();
                list.add(Collections.singleton(Task.builder()
                        .from(mainTask.getFrom())
                        .to(BATCH_SIZE)
                        .build()));
                int start = BATCH_SIZE;
                int nextWindow = start * start;
                int end = Math.min(nextWindow, mainTask.getTo());
                list.add(calcSet(start, end));
                while (mainTask.getTo() > nextWindow) {
                    start = nextWindow;
                    nextWindow = start * start;
                    end = Math.min(nextWindow, mainTask.getTo());
                    list.add(calcSet(start, end));
                }
            }
            schedule = list;
        }

        private Set<Task> calcSet(int start, int end) {
            int st = start;
            Set<Task> taskSet = new HashSet<>();
            while (st < end) {
                taskSet.add(Task.builder()
                        .from(st)
                        .to(Math.min(end, st + BATCH_SIZE))
                        .build());
                st += BATCH_SIZE;
            }
            return taskSet;
        }

        @Override
        public List<Set<Task>> getSchedule() {
            return schedule;
        }
    }
}
