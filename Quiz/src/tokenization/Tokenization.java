package tokenization;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import stemming.Stemming;

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
		// Validate command line arguments
		final CommandLine cmd = validateArguments(args);

		// Call document parser
		final File folder = new File(cmd.getOptionValue("path"));
		final File stopwords = new File(cmd.getOptionValue("stop"));
		final Parser parser = new Parser(stopwords);
		parser.parse(folder);

		// Display results
		final HashMap<String, Map<Integer, Integer>> tokenMap = parser.getTokenMap();
		displayResults(tokenMap, "Vi : Relevant words (Ignoring the stop words and numbers but count them in the distance):");

		// Call stemming
		final Stemming stemming = new Stemming();
		stemming.stem(tokenMap);

		// Display results
		final Map<String, Set<String>> stemsMap = stemming.getStemsMap();
		displayResults(stemsMap, "Si : After stemming the vocabulary");

		final Element[][] matrix = new Element[tokenMap.size()][tokenMap.size()];
		final String[] stems = stemsMap.keySet().toArray(new String[stemsMap.size()]);
		for (int i = 0; i < stems.length; i++) {
			for (int j = 0; j < stems.length; j++) {
				if (i == j) {
					continue;
				}

				double cuv = 0.0;
				final Set<String> iStrings = stemsMap.get(stems[i]);
				final Set<String> jStrings = stemsMap.get(stems[j]);
				for (final String string1 : iStrings) {
					for (final String string2 : jStrings) {
						final Map<Integer, Integer> iMap = tokenMap.get(string1);
						final Map<Integer, Integer> jMap = tokenMap.get(string2);
						for (final Integer integer : iMap.keySet()) {
							if (jMap.containsKey(integer)) {
								cuv += 1.0 / Math.abs(iMap.get(integer) - jMap.get(integer));
							}
						}
					}
				}

				matrix[i][j] = new Element(stems[i], stems[j], cuv);
			}
		}

		System.out.println("\nMetric Clusters:");
		printMatrix(matrix);
		System.out.println("\nTop 3 terms for query expansion:");
		printTopN(matrix, stems);

		final Element[][] norm = new Element[stemsMap.size()][stemsMap.size()];
		for (int i = 0; i < stems.length; i++) {
			for (int j = 0; j < stems.length; j++) {
				if (i == j) {
					continue;
				}

				double cuv = 0.0;
				if (matrix[i][j] != null) {
					cuv = matrix[i][j].value / (stemsMap.get(stems[i]).size() * stemsMap.get(stems[j]).size());
				}

				norm[i][j] = new Element(stems[i], stems[j], cuv);
			}
		}

		System.out.println("\nMetric Clusters after normalization:");
		printMatrix(norm);
		System.out.println("\nTop 3 terms for query expansion after normalization:");
		printTopN(norm, stems);
	}

	/**
	 * Print the matrix
	 *
	 * @param matrix
	 *            Matrix to print
	 */
	static void printMatrix(final Element[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < i; j++) {
				if (matrix[i][j] != null && matrix[i][j].value != 0.0) {
					System.out.print(matrix[i][j] + ", ");
				}
			}
		}

		System.out.println();
	}

	/**
	 * @param metric
	 * @param stems
	 */
	static void printTopN(final Element[][] metric, final String[] stems) {
		PriorityQueue<Element> queue = new PriorityQueue<>(3, new Comparator<Element>() {

			@Override
			public int compare(final Element o1, final Element o2) {
				return o1.value >= o2.value ? 1 : -1;
			}
		});

		int i = find(stems, "earthquake");
		for (int j = 0; j < metric[i].length; j++) {
			if (metric[i][j] == null || metric[i][j].u.equals("ecuador") || metric[i][j].v.equals("ecuador")) {
				continue;
			}

			queue.add(metric[i][j]);
			if (queue.size() > 3) {
				queue.poll();
			}
		}

		System.out.println(StringUtils.join(queue.toArray(new Element[3]), " "));
		queue = new PriorityQueue<>(3, new Comparator<Element>() {

			@Override
			public int compare(final Element o1, final Element o2) {
				return o1.value >= o2.value ? 1 : -1;
			}
		});

		i = find(stems, "ecuador");
		for (int j = 0; j < metric[i].length; j++) {
			if (metric[i][j] == null || metric[i][j].u.equals("earthquake") || metric[i][j].v.equals("earthquake")) {
				continue;
			}

			queue.add(metric[i][j]);
			if (queue.size() > 3) {
				queue.poll();
			}
		}

		System.out.println(StringUtils.join(queue.toArray(new Element[3]), " "));
	}

	public static int find(final String[] arr, final String string) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equalsIgnoreCase(string)) {
				return i;
			}
		}

		return -1;
	}

	static class Element {
		String	u;
		String	v;
		double	value;

		public Element() {
		}

		public Element(final String u, final String v, final double value) {
			this.u = u;
			this.v = v;
			this.value = value;
		}

		@Override
		public String toString() {
			return this.u + " " + this.v + " : " + this.value;
		}
	}

	/**
	 * Validates and gets the command line arguments provided
	 *
	 * @param args
	 *            Command-line arguments
	 * @return Data path
	 */
	static CommandLine validateArguments(final String[] args) {
		// Get options
		final Options options = new Options();
		options.addOption("path", "dataPath", true, "Absolute or relative path to the database");
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

		// Get data path
		if (!cmd.hasOption("path") || !cmd.hasOption("stop")) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Tokenization", options);
			System.exit(2);
		}

		return cmd;
	}

	/**
	 * Display results
	 *
	 * @param <K>
	 *            Key type
	 * @param <V>
	 *            Value type
	 * @param tokenMap
	 *            token map
	 * @param header
	 *            Name of the problem for which result is displayed
	 */
	static <K, V> void displayResults(final Map<K, V> tokenMap, final String header) {
		// Print header
		System.out.println(header);

		// Print output
		System.out.println("Size: " + tokenMap.size());
		System.out.println(StringUtils.join(tokenMap.entrySet(), " "));
		System.out.println();
	}
}