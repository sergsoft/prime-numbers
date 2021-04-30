package ru.sergsw.test.prime.numbers.calculators;

import lombok.Getter;

import java.util.SortedSet;
import java.util.TreeSet;

public class LocalContext implements Context {
   @Getter
   private final SortedSet<Integer> simpleNums = new TreeSet<>();

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
