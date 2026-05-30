package ar.edu.itba.cripto.steganography;

import ar.edu.itba.cripto.bmp.BMPFile;
import ar.edu.itba.cripto.utils.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public void distribute() throws IOException {
        int k = config.getK();
        int n = shadowImages.size();
        int seed = secretImage.getSeed();

        int[] permutedData = permutationTable.apply(secretImage.getDataAsIntArray());
        int[][] shadowPixels = buildShadowPixels(permutedData, n, k);

        Path outputDir = Paths.get("output");
        Files.createDirectories(outputDir);

        for (int i = 0; i < n; i++) {
            BMPFile carrier = shadowImages.get(i);
            LSB.hide(carrier, shadowPixels[i], i + 1, seed);
            Path outputPath = outputDir.resolve(carrier.getPath().getFileName());
            carrier.save(outputPath);
        }
    }

    public void recover() {

    }

    private int[][] buildShadowPixels(int[] permutedData, int n, int k) {
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

        return shadowPixels;
    }
}
