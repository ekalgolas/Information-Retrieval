package solution;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import tokenizer.Tokenizer;
import util.OutputFormatter;
import util.StorageManager;

/**
 * Class to display various results
 *
 * @author Ekal.Golas
 */
public class DisplayResults {
	/**
	 * Takes a query and prints the results from the dictionary
	 *
	 * @param manager
	 * @param line
	 */
	public static void displayQueryResult(final StorageManager manager, final String line) {
		System.out.println(line);
		final Tokenizer tokenizer = new Tokenizer();
		final Map<String, Integer> queryMap = new HashMap<>();

		// Tokenize the query
		for (String word : line.split(" ")) {
			word = tokenizer.transformText(word);
			// final String stem = Stemming.stem(word);

			// Process each word in the query
			if (!manager.getStopwords().contains(word)) {
				for (final String key : manager.getTermFreq().keySet()) {
					// Put matched word in query map and print matching documents
					if (key.startsWith(word)) {
						queryMap.put(key, queryMap.getOrDefault(key, 0) + 1);
						System.out.println(word + ": " + manager.getDocList().get(key));
					}
				}
			}
		}

		// Use query map to get document similarity
		displayCosineSimilarity(queryMap, manager);
		displayJaccardSimilarity(queryMap.keySet(), manager.getDocs());
		System.out.println();
	}

	/**
	 * Calculates cosine similarity of the query map with each document
	 *
	 * @param queryMap
	 *            Query map to process
	 * @param manager
	 *            Storage manager containing dictionary
	 */
	public static void displayCosineSimilarity(final Map<String, Integer> queryMap, final StorageManager manager) {
		System.out.println();

		// Get all documents
		for (final String index : manager.getDoclen().keySet()) {
			// Get formatter
			final OutputFormatter table = new OutputFormatter();
			table.addRow("Word", "TF", "tf-wt", "DF", "iDF", "tf-idf", "Normalized", "TF", "tf-wt", "Normalized", "Product");

			// Get a union of document words and query words
			final Set<String> docWords = manager.getDocs().get(index);
			docWords.addAll(queryMap.keySet());

			// For each word in union, get tf-idf
			double querySum = 0, docSum = 0, sum = 0;
			final DecimalFormat format = new DecimalFormat("0.####");
			for (final String entry : docWords) {
				// tf-idf in query
				final double query = (queryMap.containsKey(entry) ? 1.0 + Math.log10(queryMap.get(entry)) : 0) *
						Math.log10(manager.getDoclen().size() / (double) manager.getDocFreq().get(entry));
				querySum += query * query;

				// tf-idf in document
				final double doc = manager.getDocList().get(entry).containsKey(index)
						? 1.0 + Math.log10(manager.getDocList().get(entry).get(index)) : 0;
				docSum += doc * doc;
			}

			// Add the rows to table
			for (final String entry : docWords) {
				// tf-idf in query
				final double df = manager.getDocFreq().get(entry);
				final double tf_wt = queryMap.containsKey(entry) ? 1.0 + Math.log10(queryMap.get(entry)) : 0;
				final double idf = Math.log10(manager.getDoclen().size() / df);
				final double query = tf_wt * idf;

				// tf-idf in document
				final double doc = manager.getDocList().get(entry).containsKey(index)
						? 1 + Math.log10(manager.getDocList().get(entry).get(index)) : 0;

				sum += query * doc / (Math.sqrt(querySum) * Math.sqrt(docSum));
				table.addRow(entry,
						String.valueOf(queryMap.getOrDefault(entry, 0)),
						String.valueOf(format.format(tf_wt)),
						String.valueOf(df),
						String.valueOf(format.format(idf)),
						String.valueOf(format.format(query)),
						String.valueOf(format.format(query / Math.sqrt(querySum))),
						String.valueOf(format.format(manager.getDocList().get(entry).getOrDefault(index, 0))),
						String.valueOf(format.format(doc)),
						String.valueOf(format.format(doc / Math.sqrt(docSum))),
						String.valueOf(format.format(query * doc / (Math.sqrt(querySum) * Math.sqrt(docSum)))));
			}

			// Print similiarity
			System.out.println("Cosine similarity with doc" + index + ": " + format.format(sum));
			System.out.println(table);
		}
	}

	/**
	 * @param querySet
	 *            Set of words in query
	 * @param docs
	 *            document map
	 */
	private static void displayJaccardSimilarity(final Set<String> querySet, final Map<String, Set<String>> docs) {
		for (final String doc : docs.keySet()) {
			final Set<String> intersection = new HashSet<>();
			intersection.addAll(docs.get(doc));
			intersection.retainAll(querySet);
			System.out.println("Intersection with doc" + doc + ": " + intersection.size());

			final Set<String> union = new HashSet<>();
			union.addAll(querySet);
			union.addAll(docs.get(doc));
			System.out.println("Union with doc" + doc + ": " + union.size());

			final DecimalFormat format = new DecimalFormat("0.####");
			final double similarity = intersection.size() / (double) union.size();
			System.out.println("Jaccard similiarity: " + format.format(similarity));
		}
	}

	/**
	 * @param manager
	 *            Storage manager containing dictionary
	 */
	public static void displayFirstFiveTerms(final StorageManager manager) {
		// Get formatter
		final OutputFormatter terms = new OutputFormatter();
		terms.addRow("Word", "TF", "DF", "Doc", "Freq");

		// Sort the words
		final List<String> words = new ArrayList<>(manager.getTermFreq().keySet());
		Collections.sort(words);

		// Get postings for first five
		for (int i = 0; i < 5; i++) {
			for (final Entry<String, Integer> entry : manager.getDocList().get(words.get(i)).entrySet()) {
				terms.addRow(words.get(i),
						String.valueOf(manager.getTermFreq().get(words.get(i))),
						String.valueOf(manager.getDocFreq().get(words.get(i))),
						entry.getKey(),
						String.valueOf(entry.getValue()));
			}
		}

		System.out.println(terms);
	}

	/**
	 * @param manager
	 *            Storage manager containing dictionary
	 */
	public static void displayWeights(final StorageManager manager) {
		// Get formatters
		final OutputFormatter binary = new OutputFormatter();
		final OutputFormatter raw = new OutputFormatter();
		final OutputFormatter tf_idf = new OutputFormatter();

		// Create headers by sorting the order of documents
		final List<String> docs = new ArrayList<>(manager.getDoclen().keySet());
		Collections.sort(docs);
		docs.add(0, "Word");
		final String[] array = docs.toArray(new String[docs.size()]);
		binary.addRow(array);
		raw.addRow(array);
		tf_idf.addRow(array);

		// Get sorted words
		final List<String> words = new ArrayList<>(manager.getTermFreq().keySet());
		Collections.sort(words);

		// For all words, do
		for (final String string : words) {
			final String[] b = new String[docs.size()];
			final String[] r = new String[docs.size()];
			final String[] tfidf = new String[docs.size()];
			b[0] = string;
			r[0] = string;
			tfidf[0] = string;
			for (int i = 1; i < b.length; i++) {
				// Get binary weight
				b[i] = String.valueOf(manager.getDocList().get(string).containsKey(String.valueOf(i)) ? 1 : 0);

				// Get raw weight
				r[i] = String.valueOf(manager.getDocList().get(string).getOrDefault(String.valueOf(i), 0));

				// Get tf-idf
				final double value = (manager.getDocList().get(string).containsKey(String.valueOf(i))
						? 1 + Math.log10(manager.getDocList().get(string).get(String.valueOf(i))) : 0) *
						Math.log10((docs.size() - 1.0) / manager.getDocFreq().get(string));
				final DecimalFormat format = new DecimalFormat("0.####");
				tfidf[i] = String.valueOf(format.format(value));
			}

			binary.addRow(b);
			raw.addRow(r);
			tf_idf.addRow(tfidf);
		}

		System.out.println(binary);
		System.out.println(raw);
		System.out.println(tf_idf);
	}
}