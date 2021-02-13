package ru.sergsw.test.prime.numbers;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;
import ru.sergsw.test.prime.numbers.utils.ConvertUtils;

import java.io.FileWriter;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Statistic {
    public static final String LINE_SEPARATOR = System.lineSeparator();

    private final Set<Record> records = new HashSet<>();

    public void add(Record record) {
        Optional<Record> first = records.stream()
                .filter(rec -> Objects.equals(rec, record))
                .findFirst();
        if (first.isPresent()) {
            Record r = first.get();
            r.getExecTime().merge(record.getExecTime());
            r.getMemoryConsumption().merge(record.getMemoryConsumption());
        } else {
            records.add(record);
        }
    }

    @SneakyThrows
    public void writeToCsv(String csvFile) {
        try(FileWriter fileWriter = new FileWriter(csvFile, false)) {
            fileWriter.write("executor;calculator;result;taskSize;blockSize;" +
                      LongSeries.getCsvHeader("execTime") + ";" +
                    LongSeries.getCsvHeader("mem") + System.lineSeparator());
            for (Record record : records) {
                fileWriter.write(record.getExecutor() + ";");
                fileWriter.write(record.getCalculatorName() + ";");
                fileWriter.write(record.getResult() + ";");
                fileWriter.write(record.getTaskSize() + ";");
                fileWriter.write(record.getBlockSize() + ";");

                record.getExecTime().writeCsvColumns(fileWriter, false);
                record.getMemoryConsumption().writeCsvColumns(fileWriter, true);

                //fileWriter.write(LINE_SEPARATOR);
            }
        }
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder(5_000);

        Map<Integer, List<Record>> mapBySize = records.stream()
                .collect(Collectors.groupingBy(Record::getTaskSize, Collectors.toList()));

        sb.append("-------------------------------Execution summary----------------------------------").append(LINE_SEPARATOR);
        mapBySize
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach((e) -> getSummary(e.getKey(), e.getValue(), sb));

        sb.append("===================================All records=======================================").append(LINE_SEPARATOR);
        records.stream()
                .sorted(Comparator.comparing(Record::getTaskSize).thenComparing(Record::getExecutor).thenComparing(Record::getCalculatorName).thenComparing(Record::getBlockSize))
                .forEach(record -> sb.append(record.toString()).append(LINE_SEPARATOR));

        return sb.toString();
    }

    private void getSummary(Integer maxSize, List<Record> records, StringBuilder sb) {
        records.stream()
                .min(Comparator.comparing(record -> record.getExecTime().getRMS()))
                .ifPresent(record -> {
                    sb.append("MaxNum = ").append(maxSize).append(" - ")
                            .append(record.toString())
                            .append(LINE_SEPARATOR);
                });


        boolean wrongRes = records.stream().mapToInt(Record::getResult).distinct().count() > 1;
        if (wrongRes) {
            sb.append("Wrong answer!!!").append(LINE_SEPARATOR);
            records.forEach(record -> sb.append(record.toString()).append(LINE_SEPARATOR));
        }
    }

    @Value
    @Builder
    public static class Record {
        String calculatorName;
        String executor;

        @EqualsAndHashCode.Exclude @Builder.Default
        LongSeries execTime = new LongSeries();

        @EqualsAndHashCode.Exclude @Builder.Default
        LongSeries memoryConsumption = new LongSeries();

        int result;
        int taskSize;
        Integer blockSize;

        @Override
        public String toString() {
            return String.format("%s-%s[%d]= %d, Time: %s, Mem: %s, Count: %d"
                    , executor, calculatorName, blockSize, result
                    , execTime.toString(ConvertUtils::toMs)
                    , memoryConsumption.toString(ConvertUtils::toMb)
                    , execTime.getCount()
            );
        }

        void addDuration(Duration duration) {
            execTime.add(duration.toNanos());
        }

        void addMemoryConsumption(long memDiff) {
            memoryConsumption.add(memDiff);
        }
    }

    @Value
    static class LongSeries {
        List<Long> series = new ArrayList<>();

        double getRMS() {
            int size = series.size();
            return Math.sqrt(series.stream()
                    .mapToDouble(Long::doubleValue)
                    .map(d -> d *d)
                    .sum() / size);
        }

        public LongSeriesStatistics getStatistic() {
            return LongSeriesStatistics.builder()
                    .rms(getRMS())
                    .longSummaryStatistics(series.stream().mapToLong(Long::longValue).summaryStatistics())
                    .build();
        }


        int getCount() {
            return series.size();
        }

        @Override
        public String toString() {
            LongSeriesStatistics stat = getStatistic();
            return String.format("Min: %d, Max: %d, Avg: %.0f, RMS: %.0f", stat.getMin(), stat.getMax(), stat.getAvg(), stat.getRms());
        }

        public String toString(Function<Double, String> dataToStr) {
            LongSeriesStatistics stat = getStatistic();
            return String.format("Min: %s, Max: %s, Avg: %s, RMS: %s"
                    , dataToStr.apply((double) stat.getMin())
                    , dataToStr.apply((double) stat.getMax())
                    , dataToStr.apply(stat.getAvg())
                    , dataToStr.apply(stat.getRms()));
        }

        public void add(long value) {
            series.add(value);
        }

        public void merge(LongSeries from) {
            series.addAll(from.getSeries());
        }

        public static String getCsvHeader(String prefix) {
            return String.format("%1$sMin;%1$sMax;%1$sRMS,%1$sAvg,%1$sCnt", prefix);
        }

        @SneakyThrows
        public void writeCsvColumns(FileWriter fileWriter, boolean lastCols) {
            LongSeriesStatistics stat = getStatistic();

            fileWriter.write(stat.getMin() + ";");
            fileWriter.write(stat.getMax() + ";");
            fileWriter.write(stat.getRms() + ";");
            fileWriter.write(stat.getAvg() + ";");
            fileWriter.write(stat.getCount() + "");
            if (lastCols) {
                fileWriter.write(LINE_SEPARATOR);
            } else {
                fileWriter.write(";");
            }
        }

        @Value
        @Builder
        public static class LongSeriesStatistics {
            LongSummaryStatistics longSummaryStatistics;
            double rms;

            public long getMin() {
                return longSummaryStatistics.getMin();
            }

            public long getMax() {
                return longSummaryStatistics.getMax();
            }

            public double getAvg() {
                return longSummaryStatistics.getAverage();
            }

            public int getCount() {
                return (int) longSummaryStatistics.getCount();
            }
        }
    }
}
