package dcs.cod;

import java.util.BitSet;
import java.util.List;

class BitUtilities {
    /**
     * @param indexes
     * @return a {@link BitSet} with all bits at positions in <code>indexes</code> set
     */
    static BitSet indexesToBitSet(List<Integer> indexes) {
        BitSet result = new BitSet();
        for (int i : indexes) {
            result.set(i);
        }
        return result;
    }
}