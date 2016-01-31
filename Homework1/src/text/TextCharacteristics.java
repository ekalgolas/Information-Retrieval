package text;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Class that provides methods to compute the text characteristics of the input data that is required to display results
 *
 * @author Ekal.Golas
 */
public class TextCharacteristics {
	/**
	 * Gets count of tokens which occur only once in data
	 *
	 * @param tokenMap
	 *            token map of data
	 * @return Count as integer
	 */
	public static int getCountForFrequencyOne(final HashMap<String, Integer> tokenMap) {
		int count = 0;
		for (final Entry<String, Integer> entry : tokenMap.entrySet()) {
			// If occurrence is 1, this token is unique
			if (entry.getValue() == 1) {
				count++;
			}
		}

		return count;
	}

	/**
	 * Get 30 most frequent tokens in the data
	 * 
	 * @param tokenMap
	 *            token map of the data
	 * @return Map of 30 most frequent tokens in descending order of frequency
	 */
	public static Map<String, Integer> getTop30MostFrequent(final HashMap<String, Integer> tokenMap) {
		// Get a map for result
		final Map<String, Integer> top30Map = new HashMap<>();

		// Sort the map using sortable map wrapper class
		final SortableMap sortableMap = new SortableMap(tokenMap);
		final TreeMap<String, Integer> sortedMap = new TreeMap<>(sortableMap);
		sortedMap.putAll(tokenMap);

		// Get at most 30 keys from the sorted map
		int count = 0;
		for (final String key : sortedMap.descendingKeySet()) {
			if (++count > 30) {
				break;
			}

			// Put the key and frequency in result
			final int frequency = tokenMap.get(key);
			top30Map.put(key, frequency);
		}

		return top30Map;
	}
}