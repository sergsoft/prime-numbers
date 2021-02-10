package ru.sergsw.test.prime.numbers.hazlecast;

import lombok.Builder;
import lombok.Value;
import ru.sergsw.test.prime.numbers.calculators.Task;

import java.io.Serializable;
import java.util.List;

@Value
@Builder
public class Input implements Serializable {
    Class<?> calculator;
    List<Task> tasks;
}
