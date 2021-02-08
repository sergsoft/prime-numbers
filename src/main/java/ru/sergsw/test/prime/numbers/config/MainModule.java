package ru.sergsw.test.prime.numbers.config;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import ru.sergsw.test.prime.numbers.calculators.Calculator;
import ru.sergsw.test.prime.numbers.calculators.FastCalculator;
import ru.sergsw.test.prime.numbers.calculators.PlainCalculator;

public class MainModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<Calculator> calculatorMultibinder = Multibinder.newSetBinder(binder(), Calculator.class);
        calculatorMultibinder.addBinding().to(PlainCalculator.class);
        //calculatorMultibinder.addBinding().to(SlowCalculator.class);
        calculatorMultibinder.addBinding().to(FastCalculator.class);
    }
}
