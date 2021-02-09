package ru.sergsw.test.prime.numbers;

import lombok.*;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.io.FileWriter;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class Statistic {
    public static final String LINE_SEPARATOR = System.lineSeparator();

    private final List<Record> records = new ArrayList<>();

    public void add(Record record) {
        records.add(record);
    }

    @SneakyThrows
    public void writeToCsv(String csvFile) {
        try(FileWriter fileWriter = new FileWriter(csvFile, false)) {
            fileWriter.write("executor;calculator;executeTime(ns);result;contextSize(in bytes);taskSize;blockSize" + System.lineSeparator());
            for (Record record : records) {
                fileWriter.write(record.getExecutor() + ";");
                fileWriter.write(record.getCalculatorName() + ";");
                fileWriter.write(record.getExecTime().toNanos() + ";");
                fileWriter.write(record.getResult() + ";");
                fileWriter.write(record.getContextSize() + ";");
                fileWriter.write(record.getTaskSize() + ";");
                fileWriter.write(Objects.toString(record.getBlockSize()));
                fileWriter.write(LINE_SEPARATOR);
            }
        }
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder(5_000);

        Map<Integer, List<Record>> mapBySize = records.stream()
                .collect(Collectors.groupingBy(Record::getTaskSize, Collectors.toList()));

        sb.append("-------------------------------Execution summary----------------------------------").append(LINE_SEPARATOR);
        mapBySize.forEach((maxSize, records) -> getSummary(maxSize, records, sb));

        return sb.toString();
    }

    private void getSummary(Integer maxSize, List<Record> records, StringBuilder sb) {
        long minExec = records.stream().map(Record::getExecTime).mapToLong(Duration::toNanos).min().getAsLong();
        records.stream()
                .filter(record -> record.getExecTime().toNanos() == minExec)
                .forEach(record -> {
                    sb
                            .append(record.getTaskSize()).append(" | ").append(record.getExecutor()).append("-").append(record.getCalculatorName())
                            .append("[").append(record.getBlockSize()).append("]")
                            .append(formatDuration(record.getExecTime()))
                            .append(LINE_SEPARATOR);
                });
        boolean wrongRes = records.stream().mapToInt(Record::getResult).distinct().count() > 1;
        if (wrongRes) {
            sb.append("Wrong answer!!!").append(LINE_SEPARATOR);
            records.forEach(record -> sb.append(record.toString()).append(LINE_SEPARATOR));
        }
    }

    private static String formatDuration(Duration execTime) {
        return DurationFormatUtils.formatDuration(execTime.toMillis(), "s.SSS") + "s";
    }

    @Value
    @Builder
    public static class Record {
        String calculatorName;
        String executor;
        Duration execTime;
        int result;
        long contextSize;
        int taskSize;
        Integer blockSize;

        @Override
        public String toString() {
            return String.format("%s-%s[%d]= %d(in %s)", executor, calculatorName, blockSize, result, formatDuration(execTime));
        }
    }
}
