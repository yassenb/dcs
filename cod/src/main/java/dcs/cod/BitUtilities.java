package dcs.cod;

class BitUtilities {
    static int bitsToInteger(boolean[] bits) {
        int result = 0;
        for (boolean bit : bits) {
            result <<= 1;
            if (bit) {
                ++result;
            }
        }
        return result;
    }
}