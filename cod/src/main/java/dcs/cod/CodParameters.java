package dcs.cod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

class CodParameters {
    private int numberOfGenes;
    private int maxPredictors;
    private int targetGene;
    private DistributionRow[] distribution;

    static class DistributionRow {
        DistributionRow(boolean[] state, boolean[] nextState, double probability) {
            this.state = state;
            this.nextState = nextState;
            this.probability = probability;
        }

        boolean[] getState() {
            return state;
        }

        boolean[] getNextState() {
            return nextState;
        }

        double getProbability() {
            return probability;
        }

        boolean[] state;
        boolean[] nextState;
        double probability;
    }

    CodParameters(int numberOfGenes, int maxPredictors, int targetGene, DistributionRow[] distribution) {
        this.numberOfGenes = numberOfGenes;
        this.maxPredictors = maxPredictors;
        this.targetGene = targetGene;
        this.distribution = distribution;
    }

    int getNumberOfGenes() {
        return numberOfGenes;
    }

    int getMaxPredictors() {
        return maxPredictors;
    }

    int getTargetGene() {
        return targetGene;
    }

    DistributionRow[] getDistribution() {
        return distribution;
    }

    static CodParameters parse(Reader reader) throws IOException {
        final String separator = ",";
        BufferedReader input = new BufferedReader(reader);
        String[] firstLineNumbers = input.readLine().split(separator);
        int maxPredictors = Integer.parseInt(firstLineNumbers[0]);
        int targetGene = Integer.parseInt(firstLineNumbers[1]);

        input.mark(1024);
        int numberOfGenes = (input.readLine().split(separator).length - 1) / 2;
        if (numberOfGenes > 30) {
            throw new OutOfMemoryError("too many genes to fit into memory");
        }
        input.reset();

        int combinations = (int) Math.pow(2, numberOfGenes);
        DistributionRow[] distribution = new DistributionRow[combinations];
        for (int i = 0; i < combinations; ++i) {
            String[] row = input.readLine().split(separator);
            boolean[] state = new boolean[numberOfGenes];
            for (int j = 0; j < numberOfGenes; ++j) {
                state[j] = Integer.parseInt(row[j]) != 0;
            }
            boolean[] nextState = new boolean[numberOfGenes];
            for (int j = 0; j < numberOfGenes; ++j) {
                nextState[j] = Integer.parseInt(row[numberOfGenes + j]) != 0;
            }
            double probability = Double.parseDouble(row[2 * numberOfGenes]);
            distribution[i] = new DistributionRow(state, nextState, probability);
        }
        assert input.readLine() == null;

        return new CodParameters(numberOfGenes, maxPredictors, targetGene, distribution);
    }
}