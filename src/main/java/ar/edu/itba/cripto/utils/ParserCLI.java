package ar.edu.itba.cripto.utils;

import org.apache.commons.cli.*;

public class ParserCLI {
    private final Options options = new Options();
    private final CommandLineParser parser = new DefaultParser();
    private final HelpFormatter formatter = new HelpFormatter();

    public ParserCLI() {
        options.addOption(
                "d",
                false,
                "Sets mode to image distribution"
        );
        options.addOption(
                "r",
                false,
                "Sets mode to image recovery"
        );
        options.addOption(
                "secret",
                true,
                "(-d) secret image to hide / (-r) output file for the recovered image"
        ).getOption("secret").setArgName("image");
        options.addOption(
                "k",
                true,
                "Minimum number of shadows required to recover the secret"
        ).getOption("k").setArgName("number");
        options.addOption(
                "n",
                true,
                "Total number of shadows in the scheme"
        ).getOption("n").setArgName("number");
        options.addOption(
                "dir",
                true,
                "Indicates the directory with images"
        ).getOption("dir").setArgName("directory");

    }

    public CommandLine parse(String[] args) throws ParseException {
        return parser.parse(options, args);
    }

    public void printHelp() {
        formatter.printHelp("visualSSS", options);
    }
}
