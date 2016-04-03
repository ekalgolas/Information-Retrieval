
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to store all the characteristics of the indexes
 *
 * @author Ekal.Golas
 */
public class StorageManager {
	private final Map<String, Integer>				lemmaMap;
	private static Map<String, DocumentProperty>	docProperties	= new HashMap<>();;
	private static Set<String>						stopwords;

	/**
	 * Default constructor
	 *
	 * @param stopwords
	 *            Set of stop words
	 */
	public StorageManager(final Set<String> stopwords) {
		this.lemmaMap = new HashMap<>();
		StorageManager.stopwords = stopwords;
	}

	/**
	 * Stores the index characteristics
	 *
	 * @param word
	 *            Word to be parsed
	 * @param lemma
	 *            Lemma of the word
	 * @param file
	 *            File of the document where the word came from
	 */
	public void store(final String word, final List<String> lemma, final File file) {
		// Create document properties
		final String doc = file.getName().replaceAll("[^\\d]", "");
		if (!StorageManager.docProperties.containsKey(doc)) {
			docProperties.put(doc, new DocumentProperty());
		}

		if (!StorageManager.stopwords.contains(word)) {
			int count = 0;

			// Increment occurrence of this lemma
			for (final String string : lemma) {
				count = this.lemmaMap.containsKey(string) ? this.lemmaMap.get(string) : 0;
				this.lemmaMap.put(string, count + 1);
			}

			// Update the term with maximum frequency for this document
			if (StorageManager.docProperties.get(doc).getMaxFreq() < count + 1) {
				StorageManager.docProperties.get(doc).setMaxFreq(count + 1);
			}
		}

		// Increment number of words in the document
		final int len = StorageManager.docProperties.get(doc).getDoclen();
		StorageManager.docProperties.get(doc).setDoclen(len + 1);
	}

	/**
	 * @return the lemmaMap
	 */
	public final Map<String, Integer> getLemmaMap() {
		return this.lemmaMap;
	}

	/**
	 * @return the docProperties
	 */
	public static final Map<String, DocumentProperty> getDocProperties() {
		return StorageManager.docProperties;
	}
}
