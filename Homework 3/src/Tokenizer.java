import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Class that implements generating tokens and calling storage manager on them
 *
 * @author Ekal.Golas
 */
public class Tokenizer {
	public static StanfordLemmatizer lemmatizer = new StanfordLemmatizer();

	/**
	 * Tokenizes and stores the index characteristics from the words
	 *
	 * @param file
	 *            File containing the line
	 * @param line
	 *            Line which needs to be tokenized
	 * @param storageManager
	 *            Storage manager to be updated
	 * @throws IOException
	 */
	public void tokenize(final File file, String line, final StorageManager storageManager) throws IOException {
		// Transform the line in order to tokenize it
		line = this.transformText(line);

		// Get and read each token
		final String[] words = line.split(" ");
		for (final String word : words) {
			// Skip if word is empty
			if (word == null || word.length() < 1) {
				continue;
			}

			// Lemmatize
			final List<String> lemma = lemmatizer.lemmatize(word);
			storageManager.store(word, lemma, file);
		}
	}

	/**
	 * <pre>
	 *  Handles:-
	 *
	 *  A. Upper and lower case words (e.g. "People", "people", "Apple", "apple")
	 *  B. Words with dashes (e.g. "1996-97", "middle-class", "30-year", "tean-ager")
	 *  C. Possessives (e.g. "sheriff's", "university's")
	 *  D. Acronyms (e.g., "U.S.", "U.N.")
	 *  E. SGML tags are not considered words, so they should not be included in any of the information your program gathers. The SGML tags in this data follow the conventional style:
	 * 		<[/]?tag> | >[/]?tag (attr[=value])+>
	 * </pre>
	 *
	 * @param text
	 *            Text to transform
	 * @return Transformed text
	 */
	private String transformText(String text) {
		// Replacing the SGML tags with space.
		text = text.replaceAll("\\<.*?>", " ");

		// Remove digits
		text = text.replaceAll("[\\d+]", "");

		// Remove the special characters
		text = text.replaceAll("[+^:,?;=%#&~`$!@*_)/(}{\\.]", "");

		// Remove possessives
		text = text.replaceAll("\\'s", "");

		// Replace "'" with a space
		text = text.replaceAll("\\'", " ");

		// Replace - with space to count two words
		text = text.replaceAll("-", " ");

		// Remove multiple white spaces
		text = text.replaceAll("\\s+", " ");

		// Trim and set text to lower case
		text = text.trim().toLowerCase();
		return text;
	}
}