package solution;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import tokenizer.Parser;
import util.StorageManager;

/**
 * Class to gather information about documents and display results
 *
 * @author Ekal.Golas
 */
public class Question1 {
	/**
	 * Main function
	 *
	 * @param args
	 *            Command line arguments
	 * @throws IOException
	 */
	public static void main(final String args[]) throws IOException {
		// Validate command line arguments
		final CommandLine cmd = validateArguments(args);

		// Call parser
		final File folder = new File(cmd.getOptionValue("path"));
		final File stopwords = new File(cmd.getOptionValue("stop"));
		final Parser parser = new Parser(stopwords);
		parser.parse(folder);

		// Display results
		final StorageManager manager = parser.getStorageManager();
		DisplayResults.displayWeights(manager);
		DisplayResults.displayFirstFiveTerms(manager);
		displayQueryResults(manager);
	}

	/**
	 * Validates and gets the command line arguments provided
	 *
	 * @param args
	 *            Command-line arguments
	 * @return Validates arguments
	 */
	private static CommandLine validateArguments(final String[] args) {
		// Get options
		final Options options = new Options();
		options.addOption("path", "dataPath", true, "Absolute or relative path to the Cranfield database");
		options.addOption("stop", "stopWords", true, "Absolute or relative path to the Stop Words file");

		// Parse arguments
		final CommandLineParser commandLineParser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = commandLineParser.parse(options, args, false);
		} catch (final ParseException e1) {
			System.out.println("Invalid arguments provided");
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Tokenization", options);
			System.exit(1);
		}

		// Validate
		if (!cmd.hasOption("path") || !cmd.hasOption("stop")) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Tokenization", options);
			System.exit(2);
		}

		return cmd;
	}

	/**
	 * Displays query results for hard-coded queries
	 *
	 * @param manager
	 */
	private static void displayQueryResults(final StorageManager manager) {
		DisplayResults.displayQueryResult(manager, "Clinton AND Trump");
		DisplayResults.displayQueryResult(manager, "(Clinton AND Democratic) OR (Trump AND Republican)");
		DisplayResults.displayQueryResult(manager, "(Clinton AND Democratic AND Texas) OR source");
		DisplayResults.displayQueryResult(manager, "(Clinton OR Trump) AND (Cruz OR Rubio)");
	}
}