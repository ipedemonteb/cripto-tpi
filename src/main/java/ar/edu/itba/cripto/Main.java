package ar.edu.itba.cripto;

import ar.edu.itba.cripto.bmp.BMPFile;
import ar.edu.itba.cripto.steganography.SteganographyEngine;
import ar.edu.itba.cripto.utils.Config;
import ar.edu.itba.cripto.utils.ParserCLI;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        ParserCLI parserCli = new ParserCLI();

        try {
            CommandLine parsed = parserCli.parse(args);
            Config config = Config.from(parsed);

            if (config.isDistribute()) {
                BMPFile secretImage = new BMPFile(config.getSecretImagePath());
                List<BMPFile> shadowImages = loadShadowImages(config, secretImage);
                new SteganographyEngine(config, secretImage, shadowImages).distribute();
            } else {
                List<BMPFile> shadowImages = loadShadowImagesForRecovery(config);
                new SteganographyEngine(config, null, shadowImages).recover();
            }

        } catch (IllegalArgumentException e) {
            System.err.println("Configuration Error: " + e.getMessage());
            parserCli.printHelp();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("I/O Error: " + e.getMessage());
            parserCli.printHelp();
            System.exit(1);
        } catch (ParseException e) {
            System.err.println("Argument Error: " + e.getMessage());
            parserCli.printHelp();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            parserCli.printHelp();
            System.exit(1);
        }
    }

    private static List<BMPFile> loadShadowImages(Config config, BMPFile secretImage) throws IOException {
        File dir = config.getDirectory();
        File secretFile = new File(config.getSecretImagePath()).getCanonicalFile();
        int k = config.getK();

        File[] candidates = dir.listFiles((parent, name) -> name.toLowerCase().endsWith(".bmp"));
        if (candidates == null || candidates.length == 0) {
            throw new IllegalArgumentException("No BMP files found in directory: " + dir.getPath());
        }
        Arrays.sort(candidates);

        List<File> filtered = new ArrayList<>();
        for (File f : candidates) {
            if (!f.getCanonicalFile().equals(secretFile)) {
                filtered.add(f);
            }
        }

        if (filtered.isEmpty()) {
            throw new IllegalArgumentException(
                    "No carrier BMP files found in directory: " + dir.getPath());
        }

        Integer configN = config.getN();
        int n = (configN != null) ? configN : filtered.size();

        if (n < k) {
            throw new IllegalArgumentException(
                    "Not enough BMP carrier files. Need at least k=" + k + " carriers, found: " + n + ".");
        }

        if (filtered.size() < n) {
            throw new IllegalArgumentException(
                    "Not enough BMP carrier files. Required: " + n + ", found: " + filtered.size() + ".");
        }

        if (k != 8 && secretImage.getWidth() % k != 0) {
            throw new IllegalArgumentException(
                    "For k=" + k + ", secret image width (" + secretImage.getWidth()
                    + ") must be divisible by k.");
        }

        List<BMPFile> shadowImages = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            File file = filtered.get(i);
            BMPFile shadow;
            try {
                shadow = new BMPFile(file.getPath());
            } catch (IOException e) {
                throw new IOException(
                        "Failed to read carrier image '" + file.getName() + "': " + e.getMessage(), e);
            }

            if (k == 8) {
                if (shadow.getWidth() != secretImage.getWidth()
                        || shadow.getHeight() != secretImage.getHeight()) {
                    throw new IllegalArgumentException(
                            "Carrier '" + file.getName() + "' is " + shadow.getWidth() + "x" + shadow.getHeight()
                            + " but secret is " + secretImage.getWidth() + "x" + secretImage.getHeight()
                            + ". For k=8 all carriers must match the secret image size.");
                }
            } else {
                int expectedWidth  = secretImage.getWidth() / k;
                int expectedHeight = secretImage.getHeight();
                if (shadow.getWidth() != expectedWidth || shadow.getHeight() != expectedHeight) {
                    throw new IllegalArgumentException(
                            "Carrier '" + file.getName() + "' is " + shadow.getWidth() + "x" + shadow.getHeight()
                            + " but for k=" + k + " carriers must be " + expectedWidth + "x" + expectedHeight + ".");
                }
            }

            shadowImages.add(shadow);
        }

        return shadowImages;
    }

    private static List<BMPFile> loadShadowImagesForRecovery(Config config) throws IOException {
        File dir = config.getDirectory();
        int k = config.getK();
        Integer n = config.getN();

        File[] candidates = dir.listFiles((parent, name) -> name.toLowerCase().endsWith(".bmp"));
        if (candidates == null || candidates.length == 0) {
            throw new IllegalArgumentException("No BMP files found in directory: " + dir.getPath());
        }
        Arrays.sort(candidates);

        int poolSize = (n != null) ? Math.min(n, candidates.length) : candidates.length;

        if (poolSize < k) {
            throw new IllegalArgumentException(
                    "Not enough BMP files for recovery. Need " + k + ", found: " + poolSize + ".");
        }

        List<BMPFile> result = new ArrayList<>(k);
        for (int i = 0; i < k; i++) {
            result.add(new BMPFile(candidates[i].getPath()));
        }

        if (k == 8) {
            int refWidth  = result.getFirst().getWidth();
            int refHeight = result.getFirst().getHeight();
            for (int i = 1; i < k; i++) {
                if (result.get(i).getWidth() != refWidth || result.get(i).getHeight() != refHeight) {
                    throw new IllegalArgumentException(
                            "For k=8, all carrier images must have the same dimensions.");
                }
            }
        } else {
            int refDataLen = result.getFirst().getData().length;
            int refWidth   = result.getFirst().getWidth();
            int refHeight  = result.getFirst().getHeight();
            for (int i = 1; i < k; i++) {
                BMPFile carrier = result.get(i);
                if (carrier.getData().length != refDataLen
                        || carrier.getWidth() != refWidth
                        || carrier.getHeight() != refHeight) {
                    throw new IllegalArgumentException(
                            "For k=" + k + ", all carrier images must have the same dimensions (" +
                            refWidth + "x" + refHeight + "). " +
                            "Carrier '" + candidates[i].getName() + "' is " +
                            carrier.getWidth() + "x" + carrier.getHeight() + ".");
                }
            }
        }

        return result;
    }
}
