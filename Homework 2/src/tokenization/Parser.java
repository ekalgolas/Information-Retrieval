package tokenization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import indexer.StorageManager;

/**
 * Class to parse and tokenize input data
 *
 * @author Ekal.Golas
 */
public class Parser {
	/**
	 * Parses the data of all the files in the path and its subdirectories
	 *
	 * @param rootFile
	 *            Path to be parsed
	 * @param stem
	 *            Whether to stem the tokens or not
	 * @return Storage Manager for the index generated
	 * @throws IOException
	 */
	public StorageManager parse(final File rootFile, final boolean stem) throws IOException {
		final StorageManager storageManager = new StorageManager();

		// Go through every entry in the root path
		for (final File file : rootFile.listFiles()) {
			// If entry is a directory, recursively parse it
			if (file.isDirectory()) {
				this.parse(file, stem);
			} else {
				// Else, read this file
				this.readFile(file, storageManager, stem);
			}
		}

		return storageManager;
	}

	/**
	 * Parses a file and tokenizes it
	 *
	 * @param file
	 *            File to parse
	 * @param storageManager
	 *            Storage Manager to store the index
	 * @param stem
	 *            Whether to stem the tokens or not
	 * @throws IOException
	 */
	private void readFile(final File file, final StorageManager storageManager, final boolean stem) throws IOException {
		// Validate input
		if (file == null || !file.exists() || file.isDirectory()) {
			return;
		}

		// Read all lines in this file
		final Tokenizer tokenizer = new Tokenizer();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			for (String line; (line = reader.readLine()) != null;) {
				tokenizer.tokenize(file, line, storageManager, stem);
			}
		}
	}
}