package ar.edu.itba.cripto.steganography;

import ar.edu.itba.cripto.bmp.BMPFile;

public class LSB {

    private LSB() { }

    public static void hide(BMPFile carrier, int[] shadowPixels, int shadowNumber, int seed) {
        byte[] carrierData = carrier.getData();

        int requiredBytes = shadowPixels.length * 8;
        if (requiredBytes > carrierData.length) {
            throw new IllegalArgumentException(
                    "Carrier image is too small to embed the shadow. " +
                        "Required: " + requiredBytes + " bytes, available: " +
                            carrierData.length + " bytes."
            );
        }

        carrier.setSeed(seed);
        carrier.setShadowNumber(shadowNumber);

        int carrierIndex = 0;
        for (int shadowPixel : shadowPixels) {
            for (int bit = 7; bit >= 0; bit--) {
                int bitValue = (shadowPixel >> bit) & 1;
                carrierData[carrierIndex] = (byte) ((carrierData[carrierIndex] & 0xFE) | bitValue);
                carrierIndex++;
            }
        }
    }

    public static void extract() {

    }
}
