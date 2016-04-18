import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Class to perform ranked retrieval
 *
 * @author Ekal.Golas
 */
public class RankedRetrieval {
	private static final TextCharacteristics characteristics = new TextCharacteristics();

	/**
	 * Main function
	 *
	 * @param args
	 *            Command line arguments
	 * @throws IOException
	 */
	public static void main(final String args[]) throws IOException {
		// Validate command line arguments
		final long start = System.currentTimeMillis();
		final CommandLine cmd = validateArguments(args);

		// Call document parser
		final File folder = new File(cmd.getOptionValue("path"));
		final File stopwords = new File(cmd.getOptionValue("stop"));
		final DocumentParser documentParser = new DocumentParser(stopwords);
		documentParser.parse(folder);

		// Get average document length
		final Map<String, Properties> lemmaDictionary = documentParser.getDictionary().getLemmaDictionary();
		final double avgdoclen = characteristics.getAverageDocumentLength(lemmaDictionary);

		// Call query parser
		final File query = new File(cmd.getOptionValue("query"));
		final QueryParser queryParser = new QueryParser(documentParser.getStopwords());
		queryParser.readFile(query);

		// Process the queries and display results
		displayResults(lemmaDictionary, avgdoclen, queryParser);
		System.out.println("\nTotal running time: " + (System.currentTimeMillis() - start) + " milliseconds");
	}

	/**
	 * Takes in a query parser and computes ranks of each document with the lemma dictionary. Prints the query representation and top 5 ranked documents
	 *
	 * @param lemmaDictionary
	 *            Lemma dictionary of the index
	 * @param avgdoclen
	 *            Average document length in the index
	 * @param queryParser
	 *            Query parser containing all the queries
	 */
	private static void displayResults(final Map<String, Properties> lemmaDictionary, final double avgdoclen, final QueryParser queryParser) {
		// Get query processor for each query and process it
		int number = 1;
		for (final Dictionary dictionary : queryParser.getDictionaries()) {
			final QueryProcessor processor = new QueryProcessor(lemmaDictionary, avgdoclen);
			processor.process(dictionary);

			System.out.println("#################################################################################");
			System.out.println("Results for Query " + number + "\n");
			final double avglen = characteristics.getAverageDocumentLength(dictionary.getLemmaDictionary());
			final QueryProcessor queryProcessor = new QueryProcessor(dictionary.getLemmaDictionary(), avglen);
			queryProcessor.process();

			// Print W1
			System.out.println("Vector representation for W1:");
			System.out.println(characteristics.getQueryRepresentation(queryProcessor.getW1()));
			System.out.println("\nTable for W1:\n");
			System.out.println(characteristics.getTopFive(processor.getW1()));
			System.out.println("\nVector representation of top 5 ranked documents");
			System.out.println(characteristics.getTopFiveDocumentRepresentation(processor.getW1(), processor.getW1Doc()));

			// Print W2
			System.out.println("Vector representation for W2:");
			System.out.println(characteristics.getQueryRepresentation(queryProcessor.getW2()));
			System.out.println("\nTable for W2:\n");
			System.out.println(characteristics.getTopFive(processor.getW2()));
			System.out.println("\nVector representation of top 5 ranked documents");
			System.out.println(characteristics.getTopFiveDocumentRepresentation(processor.getW2(), processor.getW2Doc()));
			number++;
		}

		System.out.println("#################################################################################");
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
		options.addOption("query", "queriesFile", true, "Absolute or relative path to the Queries file");

		// Parse arguments
		final CommandLineParser commandLineParser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = commandLineParser.parse(options, args, false);
		} catch (final ParseException e1) {
			System.out.println("Invalid arguments provided");
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("RankedRetrieval", options);
			System.exit(1);
		}

		// Validate
		if (!cmd.hasOption("path") || !cmd.hasOption("stop") || !cmd.hasOption("query")) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("RankedRetrieval", options);
			System.exit(2);
		}

		return cmd;
	}
}