package ar.edu.itba.cripto;

import ar.edu.itba.cripto.bmp.BMPFile;
import ar.edu.itba.cripto.steganography.SteganographyEngine;
import ar.edu.itba.cripto.utils.Config;
import ar.edu.itba.cripto.utils.ParserCLI;
import org.apache.commons.cli.CommandLine;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        ParserCLI parserCli = new ParserCLI();
        try {
            CommandLine parsed = parserCli.parse(args);
            Config config = Config.from(parsed);

            BMPFile secretImage = new BMPFile(config.getSecretImagePath());

            if (config.isDistribute()) {
                SteganographyEngine engine = new SteganographyEngine(config, secretImage, null);
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
}