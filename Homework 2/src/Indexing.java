import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

/**
 * Class to gather information about tokens in the Cranfield database
 *
 * @author Ekal.Golas
 */
public class Indexing {
	private static final TextCharacteristics	characteristics	= new TextCharacteristics();

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

		// Call parser
		final File folder = new File(cmd.getOptionValue("path"));
		final File stopwords = new File(cmd.getOptionValue("stop"));
		final Parser parser = new Parser(stopwords);
		parser.parse(folder);

		// Display Index 1 uncompressed results
		String fileName = "Index_Version1.uncompressed";
		Map<String, Properties> dictionary = parser.getDictionary().getLemmaDictionary();
		System.out.println("#################################################################################");
		displayIndexResults(fileName, dictionary);

		// Display Index 1 compressed results
		File file = new File("Index_Version1.compressed");
		File file2 = new File("Index_Version1.compressedDictionary");
		long startCompressTime = System.currentTimeMillis();
		Compressor.blockCompress(dictionary, file, file2);
		long endcompressedTime = System.currentTimeMillis();
		long elapsedTime = endcompressedTime - startCompressTime;
		displayCompressionResults(elapsedTime, file, file2);

		// Display Index 2 uncompressed results
		fileName = "Index_Version2.uncompressed";
		dictionary = parser.getDictionary().getStemsDictionary();
		displayIndexResults(fileName, dictionary);

		// Display Index 2 compressed results
		file = new File("Index_Version2.compressed");
		file2 = new File("Index_Version2.compressedDictionary");
		startCompressTime = System.currentTimeMillis();
		Compressor.frontCodingCompress(dictionary, file, file2);
		endcompressedTime = System.currentTimeMillis();
		elapsedTime = endcompressedTime - startCompressTime;
		displayCompressionResults(elapsedTime, file, file2);
		System.out.println("#################################################################################");

		// Display tf, df and size of inverted list for terms
		displayTermCharacteristics(parser);

		// Display results for NASA
		displayResultforNasa(parser);

		// Display largest and smallest
		dictionary = parser.getDictionary().getLemmaDictionary();
		System.out.println("#################################################################################");
		System.out.println("INDEX 1");
		displayPeakTerms(dictionary);

		dictionary = parser.getDictionary().getStemsDictionary();
		System.out.println("INDEX 2");
		displayPeakTerms(dictionary);

		// Get documents with largest max_tf and doclen
		System.out.println("\nDocuments with largest max_tf: " + StringUtils.join(characteristics.getDocsWithLargestMaxTF(), " "));
		System.out.println("Documents with largest doclen: " + StringUtils.join(characteristics.getDocsWithLargestDoclen(), " "));
		System.out.println("#################################################################################");
		System.out.println("\nTotal running time: " + (System.currentTimeMillis() - start) + " milliseconds");
	}

	/**
	 * Display the terms with largest and smallest DF
	 *
	 * @param dictionary
	 *            Dictionary to parse
	 */
	static void displayPeakTerms(final Map<String, Properties> dictionary) {
		// Get largest terms
		List<String> terms = characteristics.getTermsWithLargestDf(dictionary);

		OutputFormatter formatter = new OutputFormatter();
		formatter.addRow("Term with Largest DF", "DF");
		for (final String string : terms) {
			formatter.addRow(string, String.valueOf(dictionary.get(string).getDocFreq()));
		}

		System.out.println(formatter);

		// Get smallest terms
		terms = characteristics.getTermsWithSmallestDf(dictionary);
		formatter = new OutputFormatter();
		formatter.addRow("Term with Smallest DF", "DF");
		for (final String string : terms) {
			formatter.addRow(string, String.valueOf(dictionary.get(string).getDocFreq()));
		}

		System.out.println(formatter);
	}

	/**
	 * Displays the df, for “NASA�? as well as the tf, the doclen and the max_tf, for the first 3 entries in its posting list.
	 *
	 * @param parser
	 *            Parser containing all the data
	 */
	private static void displayResultforNasa(final Parser parser) {
		// Get properties for NASA
		final Properties properties = parser.getDictionary().getLemmaDictionary().get("nasa");
		final int df = properties.getDocFreq();

		// Get results for first three
		final OutputFormatter formatter = characteristics.getFirstThree(properties);

		// Display the results
		System.out.println("NASA: DF = " + df + "\n");
		System.out.println(formatter);
	}

	/**
	 * Displays results of compressing the indexes
	 *
	 * @param time
	 *            Time taken
	 * @param file
	 *            File to be created to store compressed format
	 * @param file2
	 *            File for dictionary
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void displayCompressionResults(final long time, final File file, final File file2)
			throws FileNotFoundException,
			IOException {
		// Print the results
		final OutputFormatter formatter = new OutputFormatter();
		formatter.addRow(file.getName(), String.valueOf(file.length() + file2.length()));
		formatter.addRow("Creation time for " + file.getName(), time + " ms");
		System.out.println(formatter);
	}

	/**
	 * Displays the df, tf, and inverted list length (in bytes) for the terms: "Reynolds", "NASA", "Prandtl", "flow", "pressure", "boundary", "shock" (or stems
	 * that correspond to them)
	 *
	 * @param parser
	 * @throws NumberFormatException
	 * @throws UnsupportedEncodingException
	 */
	static void displayTermCharacteristics(final Parser parser) throws NumberFormatException, UnsupportedEncodingException {
		// Create terms on a set and then lemmatize and stem them
		final Set<String> terms = new HashSet<>();
		terms.add("reynolds");
		terms.add("nasa");
		terms.add("prandtl");
		terms.add("flow");
		terms.add("pressure");
		terms.add("boundary");
		terms.add("shock");

		final StanfordLemmatizer lemmatizer = Tokenizer.lemmatizer;
		final Set<String> lemmaSet = new HashSet<>();
		for (final String string : terms) {
			lemmaSet.addAll(lemmatizer.lemmatize(string));
		}

		final Set<String> stemSet = new HashSet<>();
		for (final String string : terms) {
			stemSet.add(Stemming.stem(string));
		}

		// Display results for Index 1
		System.out.println("#################################################################################");
		System.out.println("LEMMATIZATION TOKENS\n");
		OutputFormatter formatter = characteristics.getTermCharacteristics(lemmaSet, parser.getDictionary().getLemmaDictionary());
		System.out.println(formatter);

		// Display results for Index 2
		System.out.println("STEMMING TOKENS\n");
		formatter = characteristics.getTermCharacteristics(stemSet, parser.getDictionary().getStemsDictionary());
		System.out.println(formatter);
		System.out.println("#################################################################################");
	}

	/**
	 * Displays the size, time and length of inverted lists for a dictionary
	 *
	 * @param fileName
	 *            File name of the file to be created
	 * @param dictionary
	 *            Dictionary to display results for
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	private static void displayIndexResults(final String fileName, final Map<String, Properties> dictionary)
			throws FileNotFoundException,
			UnsupportedEncodingException {
		// Write dictionaries formed to a file
		final FileWriter writer = new FileWriter();

		// Start the timer
		final long startTime = System.currentTimeMillis();
		final File index1Uncompressed = writer.write(dictionary, fileName);
		final long endTime = System.currentTimeMillis();

		// Display size and time taken
		final OutputFormatter formatter = new OutputFormatter();
		formatter.addRow(fileName, index1Uncompressed.length() + " bytes");
		formatter.addRow("Creation time for " + fileName, endTime - startTime + " ms");

		// Get number of inverted lists
		formatter.addRow("Number of inverted lists in " + fileName, String.valueOf(dictionary.size()));
		System.out.println(formatter);
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
}