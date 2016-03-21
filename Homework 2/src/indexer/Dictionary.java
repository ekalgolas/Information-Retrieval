package indexer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import store.DocumentProperty;
import store.Properties;
import store.StorageManager;

/**
 * Class to store stems and lemma dictionary
 *
 * @author Ekal.Golas
 */
public class Dictionary {
	private final Map<String, Properties>	stemsDictionary;
	private final Map<String, Properties>	lemmaDictionary;

	/**
	 * Default constructor
	 */
	public Dictionary() {
		this.stemsDictionary = new HashMap<>();
		this.lemmaDictionary = new HashMap<>();
	}

	/**
	 * Appends data from storage manager
	 *
	 * @param manager
	 *            Storage manager
	 * @param file
	 *            File for the map
	 */
	public void append(final StorageManager manager, final File file) {
		final String doc = file.getName().replaceAll("[^\\d]", "");
		this.appendToDictionary(this.stemsDictionary, manager.getStemsMap(), StorageManager.getDocProperties(), doc);
		this.appendToDictionary(this.lemmaDictionary, manager.getLemmaMap(), StorageManager.getDocProperties(), doc);
	}

	/**
	 * Appends data to dictionary from a map
	 *
	 * @param appendTo
	 *            Dictionary to append to
	 * @param appendFrom
	 *            Map to append from
	 * @param docProperties
	 *            Document properties
	 * @param file
	 *            File name for this map
	 */
	private void appendToDictionary(final Map<String, Properties> appendTo,
			final Map<String, Integer> appendFrom,
			final Map<String, DocumentProperty> docProperties,
			final String file) {
		for (final Entry<String, Integer> entry : appendFrom.entrySet()) {
			Properties temp;
			if (appendTo.containsKey(entry.getKey())) {
				// Increment document frequency
				temp = appendTo.get(entry.getKey());
				temp.setDocFreq(temp.getDocFreq() + 1);

				final Map<String, Integer> freq = temp.getTermFreq();
				freq.put(file, entry.getValue());
				temp.setTermFreq(freq);
			} else {
				// If does not exists, create postings file and set document frequency
				temp = new Properties();
				temp.setDocFreq(1);
				temp.getTermFreq().put(file, entry.getValue());
			}

			final DocumentProperty property = docProperties.get(file);
			temp.getPostingFile().put(file, property);
			appendTo.put(entry.getKey(), temp);
		}
	}

	/**
	 * @return the stemsDictionary
	 */
	public final Map<String, Properties> getStemsDictionary() {
		return this.stemsDictionary;
	}

	/**
	 * @return the lemmaDictionary
	 */
	public final Map<String, Properties> getLemmaDictionary() {
		return this.lemmaDictionary;
	}
}