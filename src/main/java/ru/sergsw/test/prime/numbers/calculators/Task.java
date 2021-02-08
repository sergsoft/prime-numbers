package ru.sergsw.test.prime.numbers.calculators;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

@Value
@Builder
public class Task implements Serializable {
    int from;
    int to;
}
