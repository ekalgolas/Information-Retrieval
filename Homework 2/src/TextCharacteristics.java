package text;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import store.DocumentProperty;
import store.Properties;
import store.StorageManager;

/**
 * Class that provides methods to compute the text characteristics of the input data that is required to display results
 *
 * @author Ekal.Golas
 */
public class TextCharacteristics {
	/**
	 * Get characteristics like term frequency, document frequency, size of inverted list for the terms
	 *
	 * @param terms
	 *            Terms to get characteristics for
	 * @param dictionary
	 *            Dictionary to get characteristics from
	 * @return formatter containing the results
	 * @throws NumberFormatException
	 * @throws UnsupportedEncodingException
	 */
	public OutputFormatter getTermCharacteristics(final Set<String> terms, final Map<String, Properties> dictionary)
			throws NumberFormatException,
			UnsupportedEncodingException {
		// Create a formatter
		final OutputFormatter formatter = new OutputFormatter();
		formatter.addRow("TERM", "TF", "DF", "Size of inverted list");

		// Process each term and calculate characteristics for it
		for (final String term : terms) {
			if (dictionary.containsKey(term)) {
				final Map<String, DocumentProperty> postingFile = dictionary.get(term).getPostingFile();
				int tf = 0, lists = 0;
				for (final Entry<String, DocumentProperty> entry : postingFile.entrySet()) {
					// Increment term frequency
					tf += dictionary.get(term).getTermFreq().get(entry.getKey());

					// Increment string size for key and integer size for each, tf, df, max_tf and doclen
					lists += Character.SIZE * entry.getKey().length();
					lists += Integer.SIZE * 4;
				}

				formatter.addRow(term, String.valueOf(tf), String.valueOf(dictionary.get(term).getDocFreq()), lists + " bytes");
			}
		}

		return formatter;
	}

	/**
	 * Gets results of first three entries
	 *
	 * @param properties
	 *            Properties for a word
	 * @return formatter with the results
	 */
	public OutputFormatter getFirstThree(final Properties properties) {
		// Get first 3 documents in postings file
		List<String> docs = new ArrayList<>();
		docs.addAll(properties.getTermFreq().keySet());
		Collections.sort(docs);
		docs = docs.subList(0, 3);

		// Get tf, max_tf and doclen for first three
		final OutputFormatter formatter = new OutputFormatter();
		formatter.addRow("DOC-ID", "TF", "MAX_TF", "DOCLEN");
		for (final String string : docs) {
			final int tf = properties.getTermFreq().get(string);
			final int max_tf = properties.getPostingFile().get(string).getMaxFreq();
			final int doclen = properties.getPostingFile().get(string).getDoclen();
			formatter.addRow(string, String.valueOf(tf), String.valueOf(max_tf), String.valueOf(doclen));
		}
		return formatter;
	}

	/**
	 * Gets terms with largest document frequency in a dictionary
	 *
	 * @param dictionary
	 *            Dictionary to parse
	 * @return List of terms
	 */
	public List<String> getTermsWithLargestDf(final Map<String, Properties> dictionary) {
		final List<String> terms = new ArrayList<>();

		// Go through all terms
		int max = 0;
		for (final Entry<String, Properties> entry : dictionary.entrySet()) {
			// If max is less than df, set it as new max
			if (max < entry.getValue().getDocFreq()) {
				terms.clear();
				max = entry.getValue().getDocFreq();
				terms.add(entry.getKey());
			} else if (max == entry.getValue().getDocFreq()) {
				// If max is equal to df, add this term to the list
				terms.add(entry.getKey());
			}
		}

		return terms;
	}

	/**
	 * Gets documents with largest max_tf in a dictionary
	 *
	 * @param dictionary
	 *            Dictionary to parse
	 * @return List of terms
	 */
	public List<String> getDocsWithLargestMaxTF() {
		final List<String> docs = new ArrayList<>();

		// Go through all terms
		int max = 0;
		for (final Entry<String, DocumentProperty> entry : StorageManager.getDocProperties().entrySet()) {
			// If max is less than df, set it as new max
			if (max < entry.getValue().getMaxFreq()) {
				docs.clear();
				max = entry.getValue().getMaxFreq();
				docs.add(entry.getKey());
			} else if (max == entry.getValue().getMaxFreq()) {
				// If max is equal to df, add this term to the list
				docs.add(entry.getKey());
			}
		}

		return docs;
	}

	/**
	 * Gets documents with largest doclen in a dictionary
	 *
	 * @param dictionary
	 *            Dictionary to parse
	 * @return List of terms
	 */
	public List<String> getDocsWithLargestDoclen() {
		final List<String> docs = new ArrayList<>();

		// Go through all terms
		int max = 0;
		for (final Entry<String, DocumentProperty> entry : StorageManager.getDocProperties().entrySet()) {
			// If max is less than df, set it as new max
			if (max < entry.getValue().getDoclen()) {
				docs.clear();
				max = entry.getValue().getDoclen();
				docs.add(entry.getKey());
			} else if (max == entry.getValue().getDoclen()) {
				// If max is equal to df, add this term to the list
				docs.add(entry.getKey());
			}
		}

		return docs;
	}

	/**
	 * Gets terms with smallest document frequency in a dictionary
	 *
	 * @param dictionary
	 *            Dictionary to parse
	 * @return List of terms
	 */
	public List<String> getTermsWithSmallestDf(final Map<String, Properties> dictionary) {
		final List<String> terms = new ArrayList<>();

		// Go through all terms
		int min = Integer.MAX_VALUE;
		for (final Entry<String, Properties> entry : dictionary.entrySet()) {
			// If min is greater than df, set it as new max
			if (min > entry.getValue().getDocFreq()) {
				terms.clear();
				min = entry.getValue().getDocFreq();
				terms.add(entry.getKey());
			} else if (min == entry.getValue().getDocFreq()) {
				// If min is equal to df, add this term to the list
				terms.add(entry.getKey());
			}
		}

		return terms;
	}

}