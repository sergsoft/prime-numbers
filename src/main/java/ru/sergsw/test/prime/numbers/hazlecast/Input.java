package ru.sergsw.test.prime.numbers.hazlecast;

import lombok.Builder;
import lombok.Value;
import ru.sergsw.test.prime.numbers.calculators.Task;

import java.io.Serializable;
import java.util.List;
import java.util.SortedSet;

@Value
@Builder
public class Input implements Serializable {
    SortedSet<Integer> cache;
    Class<?> calculator;
    List<Task> tasks;
}
