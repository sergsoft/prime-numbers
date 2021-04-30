package ru.sergsw.test.prime.numbers.hazlecast.partition;

import com.hazelcast.cluster.Member;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.sergsw.test.prime.numbers.calculators.Context;
import ru.sergsw.test.prime.numbers.calculators.FastCalculator;
import ru.sergsw.test.prime.numbers.calculators.Task;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

@Slf4j
public class SpecialFastCalculator extends FastCalculator {
    public static final SpecialFastCalculator CALCULATOR = new SpecialFastCalculator();

    @Override
    public int calc(Task task, Context context) {
        SmartContext smartContext = SmartContext.SMART_CONTEXT.get();

        int from = task.getFrom() % 2 == 1 ? task.getFrom() : task.getFrom() + 1;
        return checkNums(smartContext, from, task.getTo());
    }

    @SneakyThrows
    int checkNums(SmartContext smartContext, int from, int to) {
        Set<Member> members = smartContext.getOtherMembers();
        PrimeNumCheck check = new PrimeNumCheck(from, to);
        if (!members.isEmpty()) {
            Map<Member, Future<int[]>> futureMap = smartContext.getExecutorService().submitToMembers(check, members);

            int[][] rest = new int[members.size() + 1][];
            rest[0] = check.call();
            int i = 1;
            for (Future<int[]> value : futureMap.values()) {
                rest[i++] = value.get();
            }

            int[] indexes = new int[members.size() + 1];

            int cnt = 0;

            boolean done = false;
            while (!done) {
                int min = rest[0][indexes[0]];
                int max = rest[0][indexes[0]];
                for (int j = 1; j < indexes.length; j++) {
                    min = Math.min(min, rest[j][indexes[j]]);
                    max = Math.max(max, rest[j][indexes[j]]);
                }
                if (min == max) {
                    smartContext.addValue(min);
                    cnt++;
                    for (int k = 0; k < indexes.length; k++) {
                        indexes[k]++;
                        if (indexes[k] >= rest[k].length) {
                            done = true;
                            break;
                        }
                    }
                } else {
                    for (int j = 0; j < indexes.length; j++) {
                        while (indexes[j] < rest[j].length && rest[j][indexes[j]] < max) {
                            indexes[j]++;
                            if (indexes[j] >= rest[j].length) {
                                done = true;
                                break;
                            }
                        }
                    }
                }
            }
            return cnt;
        } else {
            return smartContext.addValues(check.call());
        }
    }

    @Override
    public int getBlockSize() {
        return -1;
    }
}
