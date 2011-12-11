package dcs.cod;

import java.util.ArrayList;
import java.util.List;

class CombinationsGenerator<T> {
    private int combinationSize;
    private List<T> objects;
    private int[] currentCombination;
    private boolean noMoreCombinations;

    CombinationsGenerator(List<T> objects, int combinationSize) {
        if (objects.size() < combinationSize) {
            throw new IllegalArgumentException("you can't pick more objects than you have at once");
        }

        this.combinationSize = combinationSize;
        this.objects = objects;
        currentCombination = new int[combinationSize];
        for (int i = 0; i < combinationSize; ++i) {
            currentCombination[i] = i;
        }
    }

    List<T> next() {
        if (noMoreCombinations) {
            return null;
        }

        List<T> result = getCurrentObjects();

        int i = combinationSize - 1;
        while (i >= 0 && currentCombination[i] == objects.size() - combinationSize + i) {
            --i;
        }
        if (i < 0) {
            noMoreCombinations = true;
            return result;
        }

        currentCombination[i] += 1;
        for (int j = i + 1; j < combinationSize; ++j) {
            currentCombination[j] = currentCombination[i] + j - i;
        }

        return result;
    }

    private List<T> getCurrentObjects() {
        List<T> result = new ArrayList<T>();
        for (int i : currentCombination) {
            result.add(objects.get(i));
        }
        return result;
    }
}