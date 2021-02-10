package ru.sergsw.test.prime.numbers.calculators;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FastCalculatorTest {
    private FastCalculator sut = new FastCalculator();
    private Calculator example = new PlainCalculator();

    @Test
    void calc() {
        Context context = new LocalContext();
        context.warmup(30);
        sut.setBlockSize(10);
        Task task = Task.builder()
                .from(3)
                .to(30)
                .build();
        int calc = sut.calc(task, context);
        Context context2 = new LocalContext();
        assertEquals(example.calc(task, context2), calc);
    }

    @Test
    void spliterator() {
        Context context = new SharedContext();
        int blockSize = 10;
        context.warmup(blockSize);
        sut.setBlockSize(blockSize);
        Task task = Task.builder()
                .from(3)
                .to(100_000)
                .build();

        Context context2 = new SharedContext();

        List<Set<Task>> schedule = sut.spliterator(task).getSchedule();
        //assertEquals(3, schedule.size());

        AtomicInteger round = new AtomicInteger();
        Function<Task, String> logState = task1 -> task1.toString() + System.lineSeparator()
                + "Context SUT: " + context.toString() + System.lineSeparator()
                + "Context Sample: " + context2.toString() + System.lineSeparator()
                + "Round: " + round.get() + System.lineSeparator();

        for (Set<Task> tasks : schedule) {
            round.incrementAndGet();
            for (Task task1 : tasks) {
                assertEquals(example.calc(task1, context2), sut.calc(task1, context), logState.apply(task1));
            }

            context.flush();
            context2.flush();
        }
        /*assertEquals(1, schedule.get(0).size());
        Task task1 = schedule.get(0).stream().findFirst().get();
        assertEquals(example.calc(task1, context2), sut.calc(task1, context), logState.apply(task1));



        // round2
        assertEquals(9, schedule.get(1).size());
        for (Task task2 : schedule.get(1)) {
            assertEquals(example.calc(task2, context2), sut.calc(task2, context), logState.apply(task2));
        }

        context.flush();
        context2.flush();
        // round3
        assertEquals(1, schedule.get(2).size());
        for (Task task2 : schedule.get(2)) {
            assertEquals(example.calc(task2, context2), sut.calc(task2, context), logState.apply(task2));
        }*/
    }

    @Test
    void getBlockSize() {
    }

    @Test
    void setBlockSize() {
    }
}