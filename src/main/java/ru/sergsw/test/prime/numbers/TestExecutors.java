package ru.sergsw.test.prime.numbers;

import lombok.Getter;

@Getter
public enum TestExecutors {
    SINGLE_THREAD(ApplicationSingleThread.class),
    MULTI_THREAD(ApplicationMultiThread.class),
    HAZLECAST(ApplicationHazlecast.class);

    private final Class<? extends Application> aClass;

    TestExecutors(Class<? extends Application> aClass) {
        this.aClass = aClass;
    }
}
