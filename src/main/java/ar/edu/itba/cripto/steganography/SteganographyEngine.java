package ar.edu.itba.cripto.steganography;

import ar.edu.itba.cripto.bmp.BMPFile;
import ar.edu.itba.cripto.utils.Config;

import java.util.Arrays;
import java.util.List;

public class SteganographyEngine {

    private final Config config;
    private final BMPFile secretImage;
    private final PermutationTable permutationTable;
    private final List<BMPFile> shadowImages;

    public SteganographyEngine(Config config, BMPFile secretImage, List<BMPFile> shadowImages) {
        this.config = config;
        this.secretImage = secretImage;
        this.permutationTable = new PermutationTable(secretImage.getSeed(), secretImage.getData().length);
        this.shadowImages = shadowImages;
    }

    public void distribute() {
        int k = config.getK();
        int n = config.getN();

        int[] permutedData = permutationTable.apply(secretImage.getDataAsIntArray());

        int sections = permutedData.length / k;
        int[][] shadowPixels = new int[n][sections];

        for (int j = 0; j < sections; j++) {
            int[] coefficients = Arrays.copyOfRange(permutedData, j * k, j * k + k);
            WuLoPolynomial shamir = new WuLoPolynomial(coefficients);
            int[] shares = shamir.computeShares(n);
            for (int i = 0; i < n; i++) {
                shadowPixels[i][j] = shares[i];
            }
        }
    }

    public void recover() {

    }
}
