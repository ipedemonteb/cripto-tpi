package ar.edu.itba.cripto.steganography;

import ar.edu.itba.cripto.bmp.BMPFile;

public class LSB {

    private LSB() { }

    /**
     * Hides shadowPixels in carrier using LSB-n (bits per carrier byte, 8/bits carrier bytes per shadow byte).
     * Stores seed in header bytes 6-7 and shadowNumber in bytes 8-9.
     */
    public static void hide(BMPFile carrier, int[] shadowPixels, int shadowNumber, int seed, int bits) {
        byte[] carrierData = carrier.getData();
        int carrierBytesPerShadow = 8 / bits;
        int mask = ~((1 << bits) - 1) & 0xFF;

        int requiredBytes = shadowPixels.length * carrierBytesPerShadow;
        if (requiredBytes > carrierData.length) {
            throw new IllegalArgumentException(
                    "Carrier image is too small to embed the shadow. " +
                    "Required: " + requiredBytes + " bytes, available: " +
                    carrierData.length + " bytes.");
        }

        carrier.setSeed(seed);
        carrier.setShadowNumber(shadowNumber);

        int carrierIndex = 0;
        for (int shadowPixel : shadowPixels) {
            for (int shift = 8 - bits; shift >= 0; shift -= bits) {
                int bitsValue = (shadowPixel >> shift) & ((1 << bits) - 1);
                carrierData[carrierIndex] = (byte)((carrierData[carrierIndex] & mask) | bitsValue);
                carrierIndex++;
            }
        }
    }

    /**
     * Extracts numValues shadow bytes from carrier using LSB-n (bits per carrier byte).
     */
    public static int[] extract(BMPFile carrier, int numValues, int bits) {
        byte[] carrierData = carrier.getData();
        int carrierBytesPerShadow = 8 / bits;
        if (numValues * carrierBytesPerShadow > carrierData.length) {
            throw new IllegalArgumentException(
                    "Not enough carrier bytes to extract " + numValues + " shadow values.");
        }

        int[] result = new int[numValues];
        int carrierIndex = 0;
        for (int i = 0; i < numValues; i++) {
            int value = 0;
            for (int shift = 8 - bits; shift >= 0; shift -= bits) {
                int bitsValue = carrierData[carrierIndex] & ((1 << bits) - 1);
                value |= (bitsValue << shift);
                carrierIndex++;
            }
            result[i] = value;
        }
        return result;
    }
}
