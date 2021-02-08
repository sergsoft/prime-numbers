package ru.sergsw.test.prime.numbers.hazlecast;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.Collection;

@Value
@Builder
public class Output implements Serializable {
    int result;
    Collection<Integer> resultBlock;
}
