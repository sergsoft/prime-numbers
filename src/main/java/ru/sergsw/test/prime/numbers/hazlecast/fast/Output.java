package ru.sergsw.test.prime.numbers.hazlecast.fast;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

@Value
@Builder
public class Output implements Serializable {
    int result;
    int[] resultBlock;
}
