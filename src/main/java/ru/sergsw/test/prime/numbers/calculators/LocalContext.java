package ru.sergsw.test.prime.numbers.calculators;

import lombok.Value;

import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

@Value
public class LocalContext implements Context {
   SortedSet<Integer> simpleNums = new ConcurrentSkipListSet<>();

   @Override
   public long calcSize() {
      return simpleNums.size();
   }

   @Override
   public void addValue(int val) {
      simpleNums.add(val);
   }

   @Override
   public void flush() {

   }
}
