package indexer;

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
	private Map<String, Integer>		tf;
	private Map<String, Integer>		df;
	private Map<String, Set<String>>	docList;
	private Map<String, String>			max_tf;
	private Map<String, Integer>		doclen;

	/**
	 * Default constructor
	 */
	public StorageManager() {
		this.tf = new HashMap<>();
		this.df = new HashMap<>();
		this.docList = new HashMap<>();
		this.max_tf = new HashMap<>();
		this.doclen = new HashMap<>();
	}

	/**
	 * Stores the index characteristics
	 * 
	 * @param word
	 *            Word to be parsed
	 * @param file
	 *            File of the document where the word came from
	 */
	public void store(final String word, final File file) {
		// Increment occurrence of this word in term frequency
		final int count = this.tf.containsKey(word) ? this.tf.get(word) : 0;
		this.tf.put(word, count + 1);

		// Update the term with maximum frequency for this document
		final String doc = file.getName();
		if (!this.max_tf.containsKey(doc) || this.tf.get(this.max_tf.get(doc)) < count + 1) {
			this.max_tf.put(doc, word);
		}

		// If document list contains the word
		if (this.docList.containsKey(word)) {
			if (!this.docList.get(word).contains(doc)) {
				// Add this document to list of documents where this term occurs
				this.docList.get(word).add(doc);

				// Increment occurrence of this word in document frequency
				final int countDf = this.df.containsKey(word) ? this.df.get(word) : 0;
				this.df.put(word, countDf + 1);
			}
		} else {
			// Else, create a set for this word and add this document to it
			this.docList.put(word, new HashSet<String>());
			this.docList.get(word).add(doc);

			// Increment occurrence of this word in document frequency
			final int countDf = this.df.containsKey(word) ? this.df.get(word) : 0;
			this.df.put(word, countDf + 1);
		}

		// Increment number of words in the document
		final int len = this.doclen.containsKey(doc) ? this.doclen.get(doc) : 0;
		this.doclen.put(doc, len + 1);
	}

	/**
	 * @return the termFreq
	 */
	public final Map<String, Integer> getTermFreq() {
		return this.tf;
	}

	/**
	 * @param termFreq
	 *            the termFreq to set
	 */
	public final void setTermFreq(final Map<String, Integer> termFreq) {
		this.tf = termFreq;
	}

	/**
	 * @return the docFreq
	 */
	public final Map<String, Integer> getDocFreq() {
		return this.df;
	}

	/**
	 * @param docFreq
	 *            the docFreq to set
	 */
	public final void setDocFreq(final Map<String, Integer> docFreq) {
		this.df = docFreq;
	}

	/**
	 * @return the docList
	 */
	public final Map<String, Set<String>> getDocList() {
		return this.docList;
	}

	/**
	 * @param docList
	 *            the docList to set
	 */
	public final void setDocList(final Map<String, Set<String>> docList) {
		this.docList = docList;
	}

	/**
	 * @return the max_tf
	 */
	public final Map<String, String> getMax_tf() {
		return this.max_tf;
	}

	/**
	 * @param max_tf
	 *            the max_tf to set
	 */
	public final void setMax_tf(final Map<String, String> max_tf) {
		this.max_tf = max_tf;
	}

	/**
	 * @return the doclen
	 */
	public final Map<String, Integer> getDoclen() {
		return this.doclen;
	}

	/**
	 * @param doclen
	 *            the doclen to set
	 */
	public final void setDoclen(final Map<String, Integer> doclen) {
		this.doclen = doclen;
	}
}
