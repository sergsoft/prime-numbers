package ru.sergsw.test.prime.numbers.calculators;

public class PlainCalculator implements Calculator {
    @Override
    public int calc(Task task, Context context) {
        int ret = 0;
        int from = task.getFrom() % 2 == 1 ? task.getFrom() : task.getFrom() + 1;
        for (int i = from; i < task.getTo(); i+=2) {
            if (checkNum(i)) {
                ret++;
            }
        }
        return ret;
    }

    public static boolean checkNum(int num) {
        for (int i = 3; i <= Math.sqrt(num); i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }
}
