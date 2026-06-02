package ar.edu.itba.cripto.steganography;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import ar.edu.itba.cripto.bmp.BMPFile;
import ar.edu.itba.cripto.utils.Config;

public class SteganographyEngine {

    private final Config config;
    private final BMPFile secretImage; // null during recovery
    private final List<BMPFile> shadowImages;

    public SteganographyEngine(Config config, BMPFile secretImage, List<BMPFile> shadowImages) {
        this.config = config;
        this.secretImage = secretImage;
        this.shadowImages = shadowImages;
    }

    public void distribute() throws IOException {
        int k = config.getK();
        int n = shadowImages.size();

        int seed = new Random().nextInt(65536);

        int[] pixels = secretImage.getDataAsIntArray();
        PermutationTable pt = new PermutationTable(seed, pixels.length);
        int[] permutedData = pt.apply(pixels);

        int sections = permutedData.length / k;
        int[][] shadowPixels = buildShadowPixels(permutedData, n, k, sections);

        for (int i = 0; i < n; i++) {
            BMPFile carrier = shadowImages.get(i);
            int shadowNumber = i + 1;

            if (k == 8) {
                LSB.hide(carrier, shadowPixels[i], shadowNumber, seed);
            } else {
                byte[] shadowData = new byte[sections];
                for (int j = 0; j < sections; j++) {
                    shadowData[j] = (byte) shadowPixels[i][j];
                }
                carrier.setData(shadowData);
                carrier.setSeed(seed);
                carrier.setShadowNumber(shadowNumber);
            }

            carrier.save(carrier.getPath());
        }

        System.out.println("Distributed secret into " + n + " shadow images (k=" + k + ").");
    }

    public void recover() throws IOException {
        int k = config.getK();
        List<BMPFile> carriers = shadowImages;

        int seed = carriers.get(0).getSeed();

        int[] xs = new int[k];
        for (int i = 0; i < k; i++) {
            xs[i] = carriers.get(i).getShadowNumber();
        }

        int sections;
        int[][] shadowValues = new int[k][];

        if (k == 8) {
            // Shadow values are embedded 1 bit per carrier pixel
            sections = carriers.get(0).getData().length / 8;
            for (int i = 0; i < k; i++) {
                shadowValues[i] = LSB.extract(carriers.get(i), sections);
            }
        } else {
            // Shadow values ARE the carrier pixels
            sections = carriers.get(0).getData().length;
            for (int i = 0; i < k; i++) {
                shadowValues[i] = carriers.get(i).getDataAsIntArray();
            }
        }

        // Recover permuted pixels via Gauss-Jordan mod 257
        int totalPixels = sections * k;
        int[] permutedPixels = new int[totalPixels];

        for (int j = 0; j < sections; j++) {
            int[] ys = new int[k];
            for (int i = 0; i < k; i++) {
                ys[i] = shadowValues[i][j];
            }
            int[] coefficients = WuLoPolynomial.recoverCoefficients(xs, ys);
            System.arraycopy(coefficients, 0, permutedPixels, j * k, k);
        }

        // Invert permutation (XOR is self-inverse with same table)
        PermutationTable pt = new PermutationTable(seed, totalPixels);
        int[] recoveredPixels = pt.apply(permutedPixels);

        byte[] pixelData = new byte[totalPixels];
        for (int i = 0; i < totalPixels; i++) {
            pixelData[i] = (byte) recoveredPixels[i];
        }

        // Determine output dimensions
        BMPFile carrier0 = carriers.get(0);
        int outWidth  = (k == 8) ? carrier0.getWidth() : carrier0.getWidth() * k;
        int outHeight = carrier0.getHeight();

        BMPFile output = BMPFile.createOutput(carrier0, outWidth, outHeight, pixelData);
        output.save(Paths.get(config.getSecretImagePath()));

        System.out.println("Secret image recovered: " + config.getSecretImagePath());
    }

    private int[][] buildShadowPixels(int[] permutedData, int n, int k, int sections) {
        int[][] shadowPixels = new int[n][sections];
        for (int j = 0; j < sections; j++) {
            int[] coefficients = Arrays.copyOfRange(permutedData, j * k, j * k + k);
            WuLoPolynomial shamir = new WuLoPolynomial(coefficients);
            int[] shares = shamir.computeShares(n);
            for (int i = 0; i < n; i++) {
                shadowPixels[i][j] = shares[i];
            }
        }
        return shadowPixels;
    }
}
