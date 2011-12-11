package dcs.cod;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import dcs.cod.CodParameters.DistributionRow;

public class CodSequential {
    static class Transition {
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
            if (args.length < 1 || args.length > 3) {
                System.out.println("Usage: dcs-cod input_file [try_less_predictors]\n" +
                                   "  input_file - path to a file containing data in the format described in " +
                                       "input-format.txt\n" +
                                   "  try_less_predictors - \"true\" if you want to also try and find the best CoD " +
                                       "with less than the specified number of predictors");
                return;
            }
            boolean tryLessPredictors = false;
            if (args.length > 1) {
                 tryLessPredictors = Boolean.parseBoolean(args[1]);
            }
            CodForCombination bestCod =
                    (new CodSequential()).computeBestCod(new FileReader(args[0]), tryLessPredictors);
            System.out.println(String.format("The best CoD is for %s", bestCod));
        } catch (Exception e) {
            System.err.println("error: " + e.getMessage());
        }
    }

    private CodForCombination computeBestCod(Reader input, boolean tryLessPredictors) throws IOException {
        CodParameters parameters = CodParameters.parse(input);
        Transition[] transitions = getTransitions(parameters);
        double bestCod = 0;
        List<Integer> bestCodCombination = null;
        CodAlgorithm algorithm = new CodAlgorithm(transitions);
        int maxPredictors = parameters.getMaxPredictors();
        int minPredictors = tryLessPredictors ? 1 : maxPredictors;
        if (minPredictors < 1) {
            minPredictors = 1;
        }
        for (int i = maxPredictors; i >= minPredictors; --i) {
            CombinationsGenerator<Integer> combinationsGenerator =
                    new CombinationsGenerator<Integer>(getAllButTargetGene(parameters), i);
            List<Integer> combination;
            while ((combination = combinationsGenerator.next()) != null) {
                double cod = algorithm.getCod(BitUtilities.indexesToBitSet(combination));
                if (cod > bestCod) {
                    bestCod = cod;
                    bestCodCombination = combination;
                }
            }
        }
        return new CodForCombination(bestCodCombination, bestCod);
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