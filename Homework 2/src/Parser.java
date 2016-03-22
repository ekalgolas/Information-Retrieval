package tokenization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import indexer.Dictionary;
import store.StorageManager;

/**
 * Class to parse and tokenize input data
 *
 * @author Ekal.Golas
 */
public class Parser {
	private final Set<String>	stopwords;
	private final Dictionary	dictionary;

	/**
	 * Default Constructor
	 *
	 * @param file
	 *            Stop words file
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public Parser(final File file) throws FileNotFoundException, IOException {
		this.stopwords = new HashSet<>();
		this.dictionary = new Dictionary();
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
		final Tokenizer tokenizer = new Tokenizer();
		final StorageManager storageManager = new StorageManager(this.stopwords);
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			for (String line; (line = reader.readLine()) != null;) {
				tokenizer.tokenize(file, line, storageManager);
			}
		}

		// Append the parsed tokens to stems and lemma dictionary
		this.dictionary.append(storageManager, file);
	}

	/**
	 * @return the dictionary
	 */
	public final Dictionary getDictionary() {
		return this.dictionary;
	}
}