package ar.edu.itba.cripto;

import ar.edu.itba.cripto.bmp.BMPFile;
import ar.edu.itba.cripto.steganography.PermutationTable;
import ar.edu.itba.cripto.utils.ParserCLI;
import ar.edu.itba.cripto.utils.ProgramConfig;
import org.apache.commons.cli.CommandLine;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        ParserCLI parserCli = new ParserCLI();
        try {
            CommandLine parsed = parserCli.parse(args);
            ProgramConfig config = ProgramConfig.from(parsed);

            BMPFile file = new BMPFile(config.getSecretImagePath());
            PermutationTable table = new PermutationTable(file.getSeed(), file.getData().length);

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