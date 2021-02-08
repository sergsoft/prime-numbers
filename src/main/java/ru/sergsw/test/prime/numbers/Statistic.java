package ru.sergsw.test.prime.numbers;

import lombok.*;

import java.io.FileWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Statistic {
    private final List<Record> records = new ArrayList<>();

    public void add(Record record) {
        records.add(record);
    }

    @SneakyThrows
    public void writeToCsv(String csvFile) {
        try(FileWriter fileWriter = new FileWriter(csvFile, false)) {
            fileWriter.write("executor;calculator;executeTime(ns);result;contextSize(in bytes);taskSize" + System.lineSeparator());
            for (Record record : records) {
                fileWriter.write(record.getExecutor() + ";");
                fileWriter.write(record.getCalculatorName() + ";");
                fileWriter.write(record.getExecTime().toNanos() + ";");
                fileWriter.write(record.getResult() + ";");
                fileWriter.write(record.getContextSize() + ";");
                fileWriter.write(record.getTaskSize() + System.lineSeparator());
            }
        }
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
    }
}
