package dcs.cod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

class CodParameters {
    private int numberOfGenes;
    private int maxPredictors;
    private int targetGene;
    private List<DistributionRow> distribution;

    static class DistributionRow {
        BitSet state;
        BitSet nextState;
        double probability;

        DistributionRow(BitSet state, BitSet nextState, double probability) {
            this.state = state;
            this.nextState = nextState;
            this.probability = probability;
        }

        BitSet getState() {
            return state;
        }

        BitSet getNextState() {
            return nextState;
        }

        double getProbability() {
            return probability;
        }
    }

    CodParameters(int numberOfGenes, int maxPredictors, int targetGene, List<DistributionRow> distribution) {
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

    List<DistributionRow> getDistribution() {
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
        input.reset();

        List<DistributionRow> distribution = new ArrayList<DistributionRow>();
        String line;
        while ((line = input.readLine()) != null) {
            String[] row = line.split(separator);
            BitSet state = new BitSet(numberOfGenes);
            for (int i = 0; i < numberOfGenes; ++i) {
                state.set(i, Integer.parseInt(row[i]) != 0);
            }
            BitSet nextState = new BitSet(numberOfGenes);
            for (int i = 0; i < numberOfGenes; ++i) {
                nextState.set(i, Integer.parseInt(row[numberOfGenes + i]) != 0);
            }
            double probability = Double.parseDouble(row[2 * numberOfGenes]);
            distribution.add(new DistributionRow(state, nextState, probability));
        }

        return new CodParameters(numberOfGenes, maxPredictors, targetGene, distribution);
    }
}