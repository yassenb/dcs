package dcs.cod;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import dcs.cod.CodParameters.DistributionRow;

public class CodSequential {
    private static class Transition {
        private BitSet state;
        private boolean isTargetGeneExpressed;
        private double probability;

        Transition(BitSet state, boolean isTargetGeneExpressed, double probability) {
            this.state = state;
            this.isTargetGeneExpressed = isTargetGeneExpressed;
            this.probability = probability;
        }

        BitSet getState() {
            return state;
        }

        boolean isTargetGeneExpressed() {
            return isTargetGeneExpressed;
        }

        double getProbability() {
            return probability;
        }
    }

    private static class CodForCombination {
        private List<Integer> combination;
        private double cod;

        CodForCombination(List<Integer> combination, double cod) {
            if (combination == null) {
                throw new IllegalArgumentException("combination can't be null");
            }
            this.combination = combination;
            this.cod = cod;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder("genes ");
            for (int geneIndex : combination) {
                result.append(String.format("%d, ", geneIndex));
            }
            result.append(String.format("CoD %f", cod));
            return result.toString();
        }
    }

    public static void main(String args[]) {
        try {
            CodForCombination bestCod = (new CodSequential()).computeBestCod(new FileReader(args[0]));
            System.out.println(String.format("The best CoD is for %s", bestCod));
        } catch (Exception e) {
            System.err.println("error: " + e.getMessage());
        }
    }

    private CodForCombination computeBestCod(Reader input) throws IOException {
        CodParameters parameters = CodParameters.parse(input);
        Transition[] transitions = getTransitions(parameters);
        CombinationsGenerator<Integer> combinationsGenerator =
                new CombinationsGenerator<Integer>(
                        getAllButTargetGene(parameters), parameters.getMaxPredictors(), false);
        double bestCod = 0;
        List<Integer> bestCodCombination = null;
        List<Integer> combination;
        while ((combination = combinationsGenerator.next()) != null) {
            double cod = getCod(BitUtilities.indexesToBitSet(combination), transitions);
            if (cod > bestCod) {
                bestCod = cod;
                bestCodCombination = combination;
            }
        }
        return new CodForCombination(bestCodCombination, bestCod);
    }

    private double getCod(BitSet predictors, Transition[] transitions) {
        // TODO
        return 0;
    }

    private List<Integer> getAllButTargetGene(CodParameters parameters) {
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < parameters.getNumberOfGenes(); ++i) {
            if (i != parameters.getTargetGene()) {
                result.add(i);
            }
        }
        return result;
    }

    private Transition[] getTransitions(CodParameters parameters) {
        List<DistributionRow> distribution = parameters.getDistribution();
        Transition[] transitions = new Transition[distribution.size()];
        for (int i = 0; i < distribution.size(); ++i) {
            DistributionRow d = distribution.get(i);
            transitions[i] =
                    new Transition(d.getState(), d.getNextState().get(parameters.getTargetGene()), d.getProbability());
        }
        return transitions;
    }
}