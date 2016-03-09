package util;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class to store all the characteristics of the indexes
 *
 * @author Ekal.Golas
 */
public class StorageManager {
	private final Map<String, Integer>				tf;
	private final Map<String, Integer>				df;
	private final Map<String, Map<String, Integer>>	docList;
	private final Map<String, Integer>				doclen;
	private final Set<String>						stopwords;
	private final Map<String, Set<String>>			docs;

	/**
	 * Default constructor
	 *
	 * @param stopwords
	 *            Set of stop words
	 */
	public StorageManager(final Set<String> stopwords) {
		this.tf = new HashMap<>();
		this.df = new HashMap<>();
		this.docList = new HashMap<>();
		this.doclen = new HashMap<>();
		this.stopwords = stopwords;
		this.docs = new HashMap<>();
	}

	/**
	 * Stores the index characteristics after stemming
	 *
	 * @param word
	 *            Word to be parsed
	 * @param file
	 *            File of the document where the word came from
	 */
	public void store(final String word, final File file) {
		final String doc = file.getName();

		if (!this.stopwords.contains(word)) {
			// word = Stemming.stem(word);

			// Increment occurrence of this word in term frequency
			this.tf.put(word, this.tf.getOrDefault(word, 0) + 1);

			// If document list does not contain the word or the word map does not contain the doc
			if (!this.docList.containsKey(word) || !this.docList.get(word).containsKey(doc)) {
				// Increment occurrence of this word in document frequency
				this.df.put(word, this.df.getOrDefault(word, 0) + 1);
			}

			// Increment occurrence in document list
			this.docList.putIfAbsent(word, new HashMap<>());
			this.docList.get(word).put(doc, this.docList.get(word).getOrDefault(doc, 0) + 1);

			// Increment number of words in the document
			if (this.docList.get(word).getOrDefault(doc, 0) == 1) {
				this.doclen.put(doc, this.doclen.getOrDefault(doc, 0) + 1);
			}

			// Add word to docs
			this.docs.putIfAbsent(doc, new HashSet<>());
			this.docs.get(doc).add(word);
		}
	}

	/**
	 * @return the termFreq
	 */
	public final Map<String, Integer> getTermFreq() {
		return this.tf;
	}

	/**
	 * @return the docFreq
	 */
	public final Map<String, Integer> getDocFreq() {
		return this.df;
	}

	/**
	 * @return the docList
	 */
	public final Map<String, Map<String, Integer>> getDocList() {
		return this.docList;
	}

	/**
	 * @return the doclen
	 */
	public final Map<String, Integer> getDoclen() {
		return this.doclen;
	}

	/**
	 * @return the stopwords
	 */
	public final Set<String> getStopwords() {
		return this.stopwords;
	}

	/**
	 * @return the docs
	 */
	public final Map<String, Set<String>> getDocs() {
		return this.docs;
	}
}
