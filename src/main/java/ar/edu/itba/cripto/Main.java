package ar.edu.itba.cripto;

import ar.edu.itba.cripto.utils.ParserCLI;
import org.apache.commons.cli.CommandLine;

public class Main {
    public static void main(String[] args) {
        ParserCLI parserCli = new ParserCLI();
        try {
            CommandLine parsed = parserCli.parse(args);

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
            if (k < 2 || k > 10) {
                throw new IllegalArgumentException("The value of k must be between 2 and 10.");
            }

            Integer n = null;
            if (parsed.hasOption("n")) {
                if (isRecover) {
                    throw new IllegalArgumentException("The option -n cannot be used in recovery mode (-r).");
                }
                try {
                    n = Integer.parseInt(parsed.getOptionValue("n"));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("The value for -n must be an integer.");
                }
                if (n < 2) {
                    throw new IllegalArgumentException("The value of n must be at least 2.");
                }
                if (n < k) {
                    throw new IllegalArgumentException("The value of n cannot be less than k.");
                }
            }

            String directory = parsed.hasOption("dir") ? parsed.getOptionValue("dir") : ".";

            // Run program
            System.out.println("CLI Configuration successfully loaded!");

        } catch (IllegalArgumentException e) {
            System.err.println("Configuration Error: " + e.getMessage());
            parserCli.printHelp();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            parserCli.printHelp();
            System.exit(1);
        }
    }
}