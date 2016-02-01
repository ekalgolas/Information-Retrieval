package tokenization;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import stemming.Stemming;
import text.OutputFormatter;
import text.TextCharacteristics;

/**
 * Class to gather information about tokens in the Cranfield database
 *
 * @author Ekal.Golas
 */
public class Tokenization
{
	/**
	 * Main function
	 *
	 * @param args
	 *            Command line arguments
	 * @throws IOException
	 */
	public static void main(final String args[]) throws IOException
	{
		// Record the start time of the program
		final long startTime = System.currentTimeMillis();

		// Validate command line arguments
		final String dataPath = validateArguments(args);

		// Call tokenization
		final File folder = new File(dataPath);
		final Parser parser = new Parser();
		parser.parse(folder);

		// Display results
		final int totalDocuments = parser.getTotalDocuments();
		final int totalWords = parser.getTotalWords();
		final HashMap<String, Integer> tokenMap = parser.getTokenMap();
		displayResults(totalDocuments, totalWords, tokenMap, "TOKENIZATION");

		// Display time taken
		final long endTime = System.currentTimeMillis();
		final long runningTime = endTime - startTime;
		System.out.println("Time taken to acquire the text characteristics is: " + runningTime + " milliseconds\n");

		// Call stemming
		final Stemming stemming = new Stemming();
		stemming.stem(tokenMap);

		// Display results
		final int totalStems = stemming.getTotalStems();
		final HashMap<String, Integer> stemsMap = stemming.getStemsMap();
		displayResults(totalDocuments, totalStems, stemsMap, "STEMMING");
	}

	/**
	 * Validates and gets the command line arguments provided
	 *
	 * @param args
	 *            Command-line arguments
	 * @return Data path
	 */
	static String validateArguments(final String[] args) {
		// Get options
		final Options options = new Options();
		options.addOption("path", "dataPath", true, "Absolute or relative path to the Cranfield database");

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

		// Get data path
		String dataPath = "";
		if (cmd.hasOption("path")) {
			dataPath = cmd.getOptionValue("path");
		} else {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Tokenization", options);
			System.exit(2);
		}

		return dataPath;
	}

	/**
	 * Display results
	 *
	 * @param totalDocuments
	 *            total number of documents
	 * @param totalWords
	 *            total number of tokens
	 * @param tokenMap
	 *            token map
	 * @param header
	 *            Name of the problem for which result is displayed
	 */
	static void displayResults(final int totalDocuments, final int totalWords, final HashMap<String, Integer> tokenMap, final String header) {
		// Print header
		System.out.println("###############################################\n");
		System.out.println(header);
		System.out.println("Total Number of the Documents: " + String.valueOf(totalDocuments));
		System.out.println("\n###############################################\n");

		// Add all characteristics to the output formatter
		final OutputFormatter outputFormatter = new OutputFormatter();
		outputFormatter.addRow("1: Total number of tokens:", String.valueOf(totalWords));
		outputFormatter.addRow("2: Number of unique words:", String.valueOf(tokenMap.size()));
		outputFormatter.addRow("3: Number of words that occur only once:", String.valueOf(TextCharacteristics.getCountForFrequencyOne(tokenMap)));
		outputFormatter.addRow("4: 30 most frequent words:", " ");

		// Add top 30 frequent words
		outputFormatter.addRow(" ", " ");
		outputFormatter.addRow("WORD", "FREQUENCY");
		final List<String> top30Map = TextCharacteristics.getTop30MostFrequent(tokenMap);
		for (final String key : top30Map) {
			outputFormatter.addRow(key, tokenMap.get(key).toString());
		}

		outputFormatter.addRow(" ", " ");
		outputFormatter.addRow("5: Average number of tokens per document:", String.valueOf(totalWords / (double) totalDocuments));

		// Print output
		System.out.println(outputFormatter);
		System.out.println("###############################################\n");
	}
}