package ru.sergsw.test.prime.numbers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CLITest {

    @ParameterizedTest
    @CsvSource({
            "c:\\path1\\,c:\\path1\\f.csv",
            "c:\\path1,c:\\path1\\f.csv",
            ",f.csv",
            "'',f.csv"
    })
    void preparePath(String path, String exp) {
        assertEquals(exp, CLI.preparePath("f.csv", path));
    }
}