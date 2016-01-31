package tokenization;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Class to parse input data
 *
 * @author Ekal.Golas
 */
public class Parser {
	private HashMap<String, Integer>	tokenMap;
	private int							totalWords;
	private int							totalDocuments;

	/**
	 * Default constructor
	 */
	public Parser() {
		this.setTokenMap(new HashMap<String, Integer>());
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

			// Increment total number of documents after parsing
			this.setTotalDocuments(this.getTotalDocuments() + 1);
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
			for (String line; (line = reader.readLine()) != null;) {
				// Transform the line in order to tokenize it
				line = this.transformText(line);

				// Get and read each token
				final String[] words = line.split(" ");
				for (final String word : words) {
					// Skip if word is empty
					if (word == null || word.length() < 1) {
						continue;
					}

					// Increment occurrence of this word in token map
					final int count = this.getTokenMap().getOrDefault(word, 0);
					this.getTokenMap().put(word, count + 1);

					// Increment the count of total words
					this.setTotalWords(this.getTotalWords() + 1);
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

	/**
	 * @return the token map
	 */
	public HashMap<String, Integer> getTokenMap() {
		return this.tokenMap;
	}

	/**
	 * @param token
	 *            the token map to set
	 */
	public void setTokenMap(final HashMap<String, Integer> token) {
		this.tokenMap = token;
	}

	/**
	 * @return the totalWords
	 */
	public int getTotalWords() {
		return this.totalWords;
	}

	/**
	 * @param totalWords
	 *            the totalWords to set
	 */
	public void setTotalWords(final int totalWords) {
		this.totalWords = totalWords;
	}

	/**
	 * @return the totalDocuments
	 */
	public int getTotalDocuments() {
		return this.totalDocuments;
	}

	/**
	 * @param totalDocuments
	 *            the totalDocuments to set
	 */
	public void setTotalDocuments(final int totalDocuments) {
		this.totalDocuments = totalDocuments;
	}
}