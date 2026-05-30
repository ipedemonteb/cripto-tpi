package ar.edu.itba.cripto;

import ar.edu.itba.cripto.bmp.BMPFile;
import ar.edu.itba.cripto.steganography.SteganographyEngine;
import ar.edu.itba.cripto.utils.Config;
import ar.edu.itba.cripto.utils.ParserCLI;
import org.apache.commons.cli.CommandLine;

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

            BMPFile secretImage = new BMPFile(config.getSecretImagePath());

            if (config.isDistribute()) {
                List<BMPFile> shadowImages = loadShadowImages(config, secretImage);
                SteganographyEngine engine = new SteganographyEngine(config, secretImage, shadowImages);
                engine.distribute();
            }

        } catch (IllegalArgumentException e) {
            System.err.println("Configuration Error: " + e.getMessage());
            parserCli.printHelp();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("I/O Error: " + e.getMessage());
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

        File[] candidates = dir.listFiles((parent, name) ->
                name.toLowerCase().endsWith(".bmp"));

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
                    "No carrier BMP files found in directory (only the secret image was present): "
                            + dir.getPath());
        }

        Integer configN = config.getN();
        int n;
        if (configN != null) {
            if (filtered.size() < configN) {
                throw new IllegalArgumentException(
                        "Not enough BMP carrier files in directory. Required: " + configN
                                + ", found: " + filtered.size() + ".");
            }
            n = configN;
        } else {
            n = filtered.size();
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

            if (config.getK() == 8) {
                if (shadow.getWidth() != secretImage.getWidth()
                        || shadow.getHeight() != secretImage.getHeight()) {
                    throw new IllegalArgumentException(
                            "Carrier image '" + file.getName() + "' has dimensions "
                                    + shadow.getWidth() + "x" + shadow.getHeight()
                                    + " but the secret image is "
                                    + secretImage.getWidth() + "x" + secretImage.getHeight()
                                    + ". For k=8, all carriers must match the secret image size.");
                }
            }

            shadowImages.add(shadow);
        }

        return shadowImages;
    }
}