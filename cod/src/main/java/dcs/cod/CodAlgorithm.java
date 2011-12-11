package dcs.cod;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dcs.cod.CodSequential.Transition;

class CodAlgorithm {
    private Transition[] transitions;
    private Double error;

    CodAlgorithm(Transition[] transitions) {
        this.transitions = transitions;
    }

    double getCod(BitSet predictors) {
        Map<BitSet, double[]> probabilityTable = getProbabilityTable(predictors);
        double eOpt = 0;
        for (double[] probability : probabilityTable.values()) {
            eOpt += Math.min(probability[0], probability[1]);
        }
        double e = getError();
        return (e - eOpt) / e;
    }

    private double getError() {
        if (error == null) {
            Collection<double[]> values = getProbabilityTable(new BitSet()).values();
            assert values.size() == 1 : "The probability table for no predictors should have only one row";
            double[] p = values.iterator().next();
            error = Math.min(p[0], p[1]);
        }
        return error;
    }

    private Map<BitSet, double[]> getProbabilityTable(BitSet predictors) {
        Map<BitSet, double[]> result = new HashMap<BitSet, double[]>();
        for (Transition transition : transitions) {
            BitSet key = (BitSet) predictors.clone();
            key.and(transition.getState());
            double[] probabilities = result.get(key);
            if (probabilities == null) {
                probabilities = new double[2];
            }
            probabilities[transition.isTargetGeneExpressed() ? 1 : 0] += transition.getProbability();
            result.put(key, probabilities);
        }
        return result;
    }
}