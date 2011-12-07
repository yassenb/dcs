package dcs.cod;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.junit.Test;

import dcs.cod.CodParameters.DistributionRow;

public class CodParametersTest {
    @Test
    public void testBasicParsing() throws IOException {
        String input =
                "2,1\n" +
                "0,0,0,1,0.25\n" +
                "0,1,0,1,0.013\n" +
                "1,0,1,0,0.1\n" +
                "1,1,1,1,0.637";
        CodParameters codParameters = CodParameters.parse(new StringReader(input));
        assertEquals(2, codParameters.getMaxPredictors());
        assertEquals(1, codParameters.getTargetGene());
        List<DistributionRow> distribution = codParameters.getDistribution();
        assertEquals(getBitSet(), distribution.get(0).getState());
        assertEquals(getBitSet(1), distribution.get(0).getNextState());
        assertEquals(0.25, distribution.get(0).getProbability(), 0);

        assertEquals(getBitSet(1), distribution.get(1).getState());
        assertEquals(getBitSet(1), distribution.get(1).getNextState());
        assertEquals(0.013, distribution.get(1).getProbability(), 0);

        assertEquals(getBitSet(0, 1), distribution.get(3).getState());
        assertEquals(getBitSet(0, 1), distribution.get(3).getNextState());
        assertEquals(0.637, distribution.get(3).getProbability(), 0);
    }

    private BitSet getBitSet(Integer ... indexes) {
        return BitUtilities.indexesToBitSet(Arrays.<Integer>asList(indexes));
    }
}