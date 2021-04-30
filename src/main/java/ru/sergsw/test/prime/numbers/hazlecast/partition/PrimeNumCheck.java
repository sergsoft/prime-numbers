package ru.sergsw.test.prime.numbers.hazlecast.partition;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.Callable;

@ToString
@EqualsAndHashCode
public class PrimeNumCheck implements Callable<int[]>, Serializable {
    public static final int BLOCK_SIZE = 10_000;
    private final int from;
    private final int to;

    public PrimeNumCheck(int from, int to) {
        this.from = from;
        this.to = to;
    }

    /**
     *
     * @return must return sorted candidates
     * @throws Exception
     */
    @Override
    public int[] call() throws Exception {
        SmartContext smartContext = SmartContext.get();
        int[] ret = new int[(to - from) / 2];
        int cnt = 0;
        for (int i = from; i < to; i+=2) {
            double sqrt = Math.sqrt(i);
            boolean candidate = true;
            for (Integer primeNum : smartContext.getSimpleNums()) {
                if (primeNum > sqrt) {
                    break;
                }
                if ((i % primeNum) == 0) {
                    candidate = false;
                    break;
                }
            }
            if (candidate) {
                ret[cnt] = i;
                cnt++;
            }
        }
        return Arrays.copyOf(ret, cnt);
    }
}
