package ru.sergsw.test.prime.numbers;

import lombok.Getter;

@Getter
public enum TestExecutors {
    SINGLE_THREAD(ApplicationSingleThread.class),
    MULTI_THREAD(ApplicationMultiThread.class),
    HAZLECAST(ApplicationHazlecastFast.class),
    HAZLECAST_SIMPLE(ApplicationHazlecastSimple.class),
    HAZLECAST_PARTITION(ApplicationHazlecastPartition.class);

    private final Class<? extends Application> aClass;

    TestExecutors(Class<? extends Application> aClass) {
        this.aClass = aClass;
    }
}
