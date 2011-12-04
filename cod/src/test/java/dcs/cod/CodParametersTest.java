package dcs.cod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

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
        DistributionRow[] distribution = codParameters.getDistribution();
        assertArrayEquals(distribution[0].getState(), new boolean[] { false, false });
        assertArrayEquals(distribution[0].getNextState(), new boolean[] { false, true });
        assertEquals(distribution[0].getProbability(), 0.25, 0);

        assertArrayEquals(distribution[1].getState(), new boolean[] { false, true });
        assertArrayEquals(distribution[1].getNextState(), new boolean[] { false, true });
        assertEquals(distribution[1].getProbability(), 0.013, 0);

        assertArrayEquals(distribution[3].getState(), new boolean[] { true, true });
        assertArrayEquals(distribution[3].getNextState(), new boolean[] { true, true });
        assertEquals(distribution[3].getProbability(), 0.637, 0);
    }

    /**
     * Oh, JUnit, JUnit, how hard you suck
     * @param expecteds
     * @param actuals
     */
    private static void assertArrayEquals(boolean[] expecteds, boolean[] actuals) {
        assertEquals("arrays are of different length", expecteds.length, actuals.length);
        for (int i = 0; i < expecteds.length; ++i) {
            if (expecteds[i] != actuals[i]) {
                fail("arrays differ");
            }
        }
    }
}