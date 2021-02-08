package ru.sergsw.test.prime.numbers.calculators;

public class SlowCalculator implements Calculator {
    private static final int SKIP_MORE_THAN = 500_000;
    @Override
    public int calc(Task task, Context context) {
        if (task.getTo() > SKIP_MORE_THAN) {
            return -1;
        }
        int ret = 0;
        for (int i = task.getFrom(); i < task.getTo(); i++) {
            if (checkNum(i)) {
                ret++;
            }
        }
        return ret;
    }

    private boolean checkNum(int num) {
        for (int i = 2; i < num; i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }
}
