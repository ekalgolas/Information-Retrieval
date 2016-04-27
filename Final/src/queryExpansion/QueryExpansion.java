package queryExpansion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class to gather information about tokens in the Cranfield database
 *
 * @author Ekal.Golas
 */
public class QueryExpansion
{
	/**
	 * Main function
	 *
	 * @param args
	 *            Command line arguments
	 * @throws IOException
	 * @throws JSONException
	 */
	public static void main(final String args[]) throws IOException, JSONException
	{
		// Validate command line arguments
		final CommandLine cmd = validateArguments(args);
		final File stopwords = new File(cmd.getOptionValue("stop"));

		final String query = "kung fu";
		final long start = System.currentTimeMillis();
		final Element[][] elements = getExpandedQuery(stopwords, query);
		System.out.println("Time taken: " + (System.currentTimeMillis() - start) + " ms\n");
		for (final Element[] elements2 : elements) {
			for (final Element element : elements2) {
				System.out.print(element + " ");
			}

			System.out.println();
		}
	}

	/**
	 * @param stopwords
	 * @param query
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws JSONException
	 * @throws FileNotFoundException
	 */
	public static Element[][] getExpandedQuery(final File stopwords, final String query)
			throws IOException,
			MalformedURLException,
			JSONException,
			FileNotFoundException {
		final InputStream inputStream = new URL("http://ec2-54-191-183-57.us-west-2.compute.amazonaws.com:8983/solr/collection1/select?q=title%3A" +
				String.join("+", query.split(" ")) + "~10&wt=json&indent=true").openStream();

		String parsed = "";
		try {
			parsed = IOUtils.toString(inputStream);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}

		final JSONObject json = new JSONObject(parsed);
		final JSONArray arr = json.getJSONObject("response").getJSONArray("docs");
		final String[] documents = new String[10];
		for (int i = 0; i < 10; i++)
		{
			documents[i] = arr.getJSONObject(i).getString("content");
		}

		// Call document parser
		final Parser parser = new Parser(stopwords);
		parser.parse(documents);
		// Display results
		final HashMap<String, Map<Integer, Integer>> tokenMap = parser.getTokenMap();

		// Call stemming
		final Stemming stemming = new Stemming();
		stemming.stem(tokenMap);
		final Map<String, Set<String>> stemsMap = stemming.getStemsMap();

		return metricClusters(tokenMap, stemsMap, query);
	}

	/**
	 * @param tokenMap
	 * @param stemsMap
	 * @return
	 */
	public static Element[][] metricClusters(final HashMap<String, Map<Integer, Integer>> tokenMap, final Map<String, Set<String>> stemsMap, final String query) {
		final Element[][] matrix = new Element[stemsMap.size()][stemsMap.size()];
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

		return printTopN(norm, stems, query);
	}

	/**
	 * @param metric
	 * @param stems
	 * @return
	 */
	static Element[][] printTopN(final Element[][] metric, final String[] stems, final String query) {
		final Set<String> strings = new HashSet<>();
		strings.addAll(Arrays.asList(query.split(" ")));

		final Element[][] elements = new Element[strings.size()][3];
		int index = 0;
		for (final String string : strings) {
			final PriorityQueue<Element> queue = new PriorityQueue<>(3, new Comparator<Element>() {

				@Override
				public int compare(final Element o1, final Element o2) {
					return o1.value >= o2.value ? 1 : -1;
				}
			});

			final int i = find(stems, string);
			for (int j = 0; j < metric[i].length; j++) {
				if (metric[i][j] == null || strings.contains(metric[i][j].u) && !metric[i][j].u.equals(string) || strings.contains(metric[i][j].v) &&
						!metric[i][j].v.equals(string)) {
					continue;
				}

				queue.add(metric[i][j]);
				if (queue.size() > 3) {
					queue.poll();
				}
			}

			elements[index++] = queue.toArray(new Element[3]);
		}

		return elements;
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

		return cmd;
	}
}