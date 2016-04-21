package tokenization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class to parse input data
 *
 * @author Ekal.Golas
 */
public class Parser {
	private final HashMap<String, Map<Integer, Integer>>	tokenMap;
	private final Set<String>								stopwords;
	private static int										index;

	/**
	 * Default constructor
	 *
	 * @param file
	 *            Stop words file
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public Parser(final File file) throws FileNotFoundException, IOException {
		this.tokenMap = new HashMap<>();
		this.stopwords = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			for (String line; (line = reader.readLine()) != null;) {
				this.stopwords.add(line.trim());
			}
		}
	}

	/**
	 * Parses the data of all the files in the path and its subdirectories
	 *
	 * @param rootFile
	 *            Path to be parsed
	 * @throws IOException
	 */
	public void parse(final File rootFile) throws IOException {
		// Go through every entry in the root path
		for (final File file : rootFile.listFiles()) {
			// If entry is a directory, recursively parse it
			if (file.isDirectory()) {
				this.parse(file);
			} else {
				// Else, read this file
				this.readFile(file);
			}
		}
	}

	/**
	 * Parses a file and tokenizes it
	 *
	 * @param file
	 *            File to parse
	 * @throws IOException
	 */
	private void readFile(final File file) throws IOException {
		// Validate input
		if (file == null || !file.exists() || file.isDirectory()) {
			return;
		}

		// Read all lines in this file
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			index++;
			int num = 0;
			for (String line; (line = reader.readLine()) != null;) {
				// Get and read each token
				final String[] words = line.split(" ");
				for (String word : words) {
					num++;

					// Transform the word in order to tokenize it
					word = this.transformText(word);

					// Skip if word is empty
					if (word == null || word.length() < 1 || this.stopwords.contains(word)) {
						continue;
					}

					// Increment occurrence of this word in token map
					if (!this.tokenMap.containsKey(word)) {
						this.tokenMap.put(word, new HashMap<Integer, Integer>());
					}

					this.tokenMap.get(word).put(index, num);
				}
			}
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
		text = text.replaceAll("\\'", "");

		// Replace - with space to count two words
		text = text.replaceAll("-", " ");

		// Remove multiple white spaces
		text = text.replaceAll("\\s+", " ");

		// Trim and set text to lower case
		text = text.trim().toLowerCase();
		return text;
	}

	/**
	 * @return the token map
	 */
	public HashMap<String, Map<Integer, Integer>> getTokenMap() {
		return this.tokenMap;
	}
}