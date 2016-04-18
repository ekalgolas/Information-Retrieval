import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class that provides methods to compute the text characteristics of the input data that is required to display results
 *
 * @author Ekal.Golas
 */
public class TextCharacteristics {
	/**
	 * Gets average document length given an index
	 *
	 * @param index
	 *            Hashmap index
	 * @return Average document length as double
	 */
	public double getAverageDocumentLength(final Map<String, Properties> index) {
		double sum = 0.0;
		for (final String term : index.keySet()) {
			final Properties properties = index.get(term);
			sum += properties.getDocFreq();
		}

		return sum / index.size();
	}

	/**
	 * Get the rank, score, external document identifier, and headline, for each of the top 5 documents for query
	 *
	 * @param queryMap
	 *            token map of the query
	 * @return Output formatted in a string
	 */
	public OutputFormatter getTopFive(final Map<String, Double> queryMap) {
		// Get a formatter for result
		final OutputFormatter formatter = new OutputFormatter();
		formatter.addRow("RANK", "SCORE", "EXTERNAL DOCUMENT IDENTIFIER", "HEADLINE");

		// Sort the map using sortable map wrapper class
		final SortableMap sortableMap = new SortableMap(queryMap);
		final TreeMap<String, Double> sortedMap = new TreeMap<>(sortableMap);
		sortedMap.putAll(queryMap);

		// Get at most 5 keys from the sorted map
		int count = 0;
		for (final String key : sortedMap.descendingKeySet()) {
			if (++count > 5) {
				break;
			}

			// Compute details
			final String headline = StorageManager.getDocProperties().get(key).getHeadline();
			final String score = String.valueOf(queryMap.get(key));

			// Put the details in result
			formatter.addRow(String.valueOf(count), score, "cranfield" + key, headline);
		}

		return formatter;
	}

	public String getTopFiveDocumentRepresentation(final Map<String, Double> queryMap, final Map<String, Map<String, Double>> docMap) {
		// Get a string builder for result
		final StringBuilder builder = new StringBuilder();

		// Sort the map using sortable map wrapper class
		final SortableMap sortableMap = new SortableMap(queryMap);
		final TreeMap<String, Double> sortedMap = new TreeMap<>(sortableMap);
		sortedMap.putAll(queryMap);

		// Get at most 5 keys from the sorted map
		int count = 0;
		for (final String key : sortedMap.descendingKeySet()) {
			if (++count > 5) {
				break;
			}

			// Compute details
			final Set<String> words = StorageManager.getDocProperties().get(key).getWords();
			final Map<String, Double> map = docMap.get(key);

			// Put the details in result
			builder.append("\nDoc: cranfield" + key + "\n");
			for (final String string : words) {
				if (map.containsKey(string)) {
					builder.append(string + " : " + map.get(string) + ", ");
				} else {
					builder.append(string + " : 0.0, ");
				}
			}

			builder.append("\n");
		}

		return builder.toString();
	}

	public String getQueryRepresentation(final Map<String, Double> dictionary) {
		final StringBuilder builder = new StringBuilder();
		for (final Entry<String, Double> term : dictionary.entrySet()) {
			builder.append(term.getKey() + ":" + term.getValue() + " ");
		}

		return builder.toString().trim();
	}
}