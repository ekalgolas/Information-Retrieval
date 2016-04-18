import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Parser for query file
 *
 * @author Ekal.Golas
 */
public class QueryParser {
	private final Set<String>		stopwords;
	private final List<Dictionary>	dictionaries;

	/**
	 * Constructor
	 */
	public QueryParser(final Set<String> stopwords) {
		this.stopwords = stopwords;
		this.dictionaries = new ArrayList<>();
	}

	/**
	 * Parses queries file, tokenizes and builds a dictionary for each query
	 *
	 * @param file
	 *            File to parse
	 * @throws IOException
	 */
	public void readFile(final File file) throws IOException {
		// Validate input
		if (file == null || !file.exists() || file.isDirectory()) {
			return;
		}

		// Read all queries into a list of strings
		final String data = new String(Files.readAllBytes(file.toPath()));
		final String[] parts = Pattern.compile("[Q0-9:]+").split(data);
		final List<String> queries = new ArrayList<>();
		for (final String part : parts) {
			final String query = part.trim().replaceAll("\\r\\n", " ");
			if (query.length() > 0) {
				queries.add(query);
			}
		}

		// Get a dictionary for each query
		final Tokenizer tokenizer = new Tokenizer();
		for (final String query : queries) {
			final StorageManager storageManager = new StorageManager(this.stopwords);
			tokenizer.tokenize(file, query, storageManager);

			final Dictionary dictionary = new Dictionary();
			dictionary.append(storageManager, file);
			this.dictionaries.add(dictionary);
		}
	}

	/**
	 * @return the dictionary
	 */
	public final List<Dictionary> getDictionaries() {
		return this.dictionaries;
	}
}