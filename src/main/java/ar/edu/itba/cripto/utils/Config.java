package ar.edu.itba.cripto.utils;

import org.apache.commons.cli.CommandLine;

import java.io.File;

public class Config {

    private static final int MIN_K = 2;
    private static final int MAX_K = 10;
    private static final int MIN_N = 2;

    private final boolean distribute;
    private final String secretImagePath;
    private final int k;
    private final Integer n;
    private final File directory;

    private Config(boolean distribute, String secretImagePath, int k, Integer n, File directory) {
        this.distribute = distribute;
        this.secretImagePath = secretImagePath;
        this.k = k;
        this.n = n;
        this.directory = directory;
    }

    public static Config from(CommandLine parsed) {
        boolean isDistribute = parsed.hasOption("d");
        boolean isRecover = parsed.hasOption("r");

        if (!isDistribute && !isRecover) {
            throw new IllegalArgumentException("You must specify either -d (distribute) or -r (recover).");
        }
        if (isDistribute && isRecover) {
            throw new IllegalArgumentException("Cannot specify both -d and -r at the same time.");
        }

        if (!parsed.hasOption("secret")) {
            throw new IllegalArgumentException("The option -secret is mandatory.");
        }
        String secretImagePath = parsed.getOptionValue("secret");

        if (!parsed.hasOption("k")) {
            throw new IllegalArgumentException("The option -k is mandatory.");
        }
        int k;
        try {
            k = Integer.parseInt(parsed.getOptionValue("k"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The value for -k must be an integer.");
        }
        if (k < MIN_K || k > MAX_K) {
            throw new IllegalArgumentException("The value of k must be between " + MIN_K + " and " + MAX_K + ".");
        }

        Integer n = null;
        if (parsed.hasOption("n")) {
            try {
                n = Integer.parseInt(parsed.getOptionValue("n"));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("The value for -n must be an integer.");
            }
            if (n < MIN_N) {
                throw new IllegalArgumentException("The value of n must be at least " + MIN_N + ".");
            }
            if (n < k) {
                throw new IllegalArgumentException("The value of n cannot be less than k.");
            }
        }


        String directoryPath = parsed.hasOption("dir") ? parsed.getOptionValue("dir") : ".";
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("The specified directory does not exist or is not a directory: " + directoryPath);
        }

        return new Config(isDistribute, secretImagePath, k, n, directory);
    }

    public boolean isDistribute() { return distribute; }

    public String getSecretImagePath() { return secretImagePath; }

    public int getK() { return k; }

    public Integer getN() { return n; }

    public File getDirectory() { return directory; }
}
