package dcs.cod;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class CombinationsGeneratorTest {
    @Test
    public void test3of4Integers() {
        CombinationsGenerator<Integer> generator =
                new CombinationsGenerator<Integer>(Arrays.asList(1, 2, 3, 4), 3, false);
        Set<Set<Integer>> combinations = new HashSet<Set<Integer>>();
        List<Integer> combination = null;
        while ((combination = generator.next()) != null) {
            combinations.add(new HashSet<Integer>(combination));
        }

        @SuppressWarnings("serial")
        Set<Set<Integer>> expectedCombinations = new HashSet<Set<Integer>>() {{
            add(new HashSet<Integer>(Arrays.asList(1, 2, 3)));
            add(new HashSet<Integer>(Arrays.asList(1, 2, 4)));
            add(new HashSet<Integer>(Arrays.asList(1, 3, 4)));
            add(new HashSet<Integer>(Arrays.asList(2, 3, 4)));
        }};
        assertEquals(expectedCombinations, combinations);
    }

    @Test
    public void test4of8RepeatingIntegers() {
        CombinationsGenerator<Integer> generator =
                new CombinationsGenerator<Integer>(Arrays.asList(1, 1, 1, 1, 2, 2, 2, 2), 4, false);
        int numberOfCombinations = 0;
        while (generator.next() != null) {
            ++numberOfCombinations;
        }
        assertEquals(70, numberOfCombinations);
    }
}