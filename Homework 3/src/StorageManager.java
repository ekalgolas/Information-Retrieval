import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
	 * @throws IOException
	 */
	public void store(final String word, final List<String> lemma, final File file) throws IOException {
		// Create document properties
		final String doc = file.getName().replaceAll("[^\\d]", "");
		if (!docProperties.containsKey(doc)) {
			docProperties.put(doc, new DocumentProperty());
		}

		// Set document headline
		final String headline = getHeadLine(file);
		docProperties.get(doc).setHeadline(headline);

		if (!StorageManager.stopwords.contains(word)) {
			int count = 0;

			// Increment occurrence of this lemma
			for (final String string : lemma) {
				count = this.lemmaMap.containsKey(string) ? this.lemmaMap.get(string) : 0;
				this.lemmaMap.put(string, count + 1);

				// Add this word to set of words
				docProperties.get(doc).getWords().add(string);
			}

			// Update the term with maximum frequency for this document
			if (docProperties.get(doc).getMaxFreq() < count + 1) {
				docProperties.get(doc).setMaxFreq(count + 1);
			}

			// Increment number of words in the document
			final int len = docProperties.get(doc).getDoclen();
			docProperties.get(doc).setDoclen(len + 1);
		}
	}

	/**
	 * Get document headline
	 *
	 * @param file
	 *            File for the document
	 * @return Headline as a string
	 * @throws IOException
	 */
	private static String getHeadLine(final File file) throws IOException {
		// Read file
		final String data = new String(Files.readAllBytes(file.toPath()));

		// Match the title part and return the value
		final Pattern pattern = Pattern.compile("<.?title>", Pattern.CASE_INSENSITIVE);
		final String[] parts = pattern.split(data);
		if (parts.length > 1) {
			return parts[1].replace("\n", "");
		} else {
			return "";
		}
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