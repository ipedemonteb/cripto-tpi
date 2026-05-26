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
                "Indicates the image name to be hidden"
        ).getOption("secret").setArgName("image");
        options.addOption(
                "k",
                true,
                "Sets the minimum shades for secret recovery"
        ).getOption("k").setArgName("number");
        options.addOption(
                "n",
                true,
                "Sets the number of shades to be generated"
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
