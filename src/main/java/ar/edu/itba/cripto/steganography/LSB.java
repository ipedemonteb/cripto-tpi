package ar.edu.itba.cripto.steganography;

import ar.edu.itba.cripto.bmp.BMPFile;

public class LSB {

    private LSB() { }

    /**
     * Hides shadowPixels in carrier using LSB-1 (1 bit per carrier byte, 8 carrier bytes per shadow byte).
     * Stores seed in header bytes 6-7 and shadowNumber in bytes 8-9.
     */
    public static void hide(BMPFile carrier, int[] shadowPixels, int shadowNumber, int seed) {
        byte[] carrierData = carrier.getData();

        int requiredBytes = shadowPixels.length * 8;
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
            for (int bit = 7; bit >= 0; bit--) {
                int bitValue = (shadowPixel >> bit) & 1;
                carrierData[carrierIndex] = (byte)((carrierData[carrierIndex] & 0xFE) | bitValue);
                carrierIndex++;
            }
        }
    }

    /**
     * Extracts numValues shadow bytes from carrier using LSB-1 (1 bit per carrier byte).
     */
    public static int[] extract(BMPFile carrier, int numValues) {
        byte[] carrierData = carrier.getData();
        if (numValues * 8 > carrierData.length) {
            throw new IllegalArgumentException(
                    "Not enough carrier bytes to extract " + numValues + " shadow values.");
        }

        int[] result = new int[numValues];
        int carrierIndex = 0;
        for (int i = 0; i < numValues; i++) {
            int value = 0;
            for (int bit = 7; bit >= 0; bit--) {
                int lsb = carrierData[carrierIndex] & 1;
                value |= (lsb << bit);
                carrierIndex++;
            }
            result[i] = value;
        }
        return result;
    }
}
