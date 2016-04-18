import java.util.HashMap;
import java.util.Map;

/**
 * Class to process a query and build W1 and W2 tables for it
 *
 * @author Ekal.Golas
 */
public class QueryProcessor {
	private final Map<String, Properties>			index;
	private final Map<String, Double>				W1;
	private final Map<String, Double>				W2;
	private final Map<String, Map<String, Double>>	W1Doc;
	private final Map<String, Map<String, Double>>	W2Doc;
	private final double							avgdoclen;

	/**
	 * Constructor
	 *
	 * @param index
	 *            Index built for the data
	 */
	public QueryProcessor(final Map<String, Properties> index, final double avgdoclen) {
		this.index = index;
		this.W1 = new HashMap<>();
		this.W2 = new HashMap<>();
		this.W1Doc = new HashMap<>();
		this.W2Doc = new HashMap<>();
		this.avgdoclen = avgdoclen;
	}

	/**
	 * Process the query and build W1 and W2 tables for itself
	 */
	public void process() {
		final int collectionSize = this.index.size();
		for (final String term : this.index.keySet()) {
			// Get df and postings file
			final Properties properties = this.index.get(term);
			final int df = properties.getDocFreq();
			final Map<String, DocumentProperty> postingFile = properties.getPostingFile();

			// For each doc in postings file
			for (final String docID : postingFile.keySet()) {
				// Get maxtf, doc and tf
				final int maxtf = postingFile.get(docID).getMaxFreq();
				final int doclen = postingFile.get(docID).getDoclen();
				final int tf = properties.getTermFreq().get(docID);

				// Add w1 to the table
				final double w1 = this.W1(tf, maxtf, df, collectionSize);
				double weight = this.W1.containsKey(term) ? this.W1.get(term) : 0.0;
				this.W1.put(term, weight + w1);

				// Add w2 to the table
				final double w2 = this.W2(tf, doclen, this.avgdoclen, df, collectionSize);
				weight = this.W2.containsKey(term) ? this.W2.get(term) : 0.0;
				this.W2.put(term, weight + w2);
			}
		}
	}

	/**
	 * Process the query and build W1 and W2 tables
	 *
	 * @param query
	 *            Dictionary for the query
	 */
	public void process(final Dictionary query) {
		final int collectionSize = this.index.size();
		for (final String term : query.getLemmaDictionary().keySet()) {
			// Skip if query term does not exist in the index built
			final Properties properties = this.index.get(term);
			if (properties == null) {
				continue;
			}

			// Get df and postings file
			final int df = properties.getDocFreq();
			final Map<String, DocumentProperty> postingFile = properties.getPostingFile();

			// For each doc in postings file
			for (final String docID : postingFile.keySet()) {
				// Get maxtf, doc and tf
				final int maxtf = postingFile.get(docID).getMaxFreq();
				final int doclen = postingFile.get(docID).getDoclen();
				final int tf = properties.getTermFreq().get(docID);

				// Update w1 and w2 weights
				this.updateWeights(collectionSize, term, df, docID, maxtf, doclen, tf);
			}
		}
	}

	/**
	 * Updates w1 and w2 weights in the maps
	 * 
	 * @param the
	 *            number of documents in the collection
	 * @param term
	 *            Lemma term
	 * @param df
	 *            Document frequency
	 * @param docID
	 *            Document ID
	 * @param maxtf
	 *            Maximum term frequency
	 * @param doclen
	 *            Document length
	 * @param tf
	 *            term frequency
	 */
	private void updateWeights(final int collectionSize, final String term, final int df, final String docID, final int maxtf, final int doclen, final int tf) {
		// Add w1 to the table
		final double w1 = this.W1(tf, maxtf, df, collectionSize);
		double weight = this.W1.containsKey(docID) ? this.W1.get(docID) : 0.0;
		this.W1.put(docID, weight + w1);

		// Add w1 to doc representation
		if (this.W1Doc.get(docID) == null) {
			this.W1Doc.put(docID, new HashMap<String, Double>());
		}

		weight = this.W1Doc.get(docID).containsKey(term) ? this.W1Doc.get(docID).get(term) : 0.0;
		this.W1Doc.get(docID).put(term, weight + w1);

		// Add w2 to the table
		final double w2 = this.W2(tf, doclen, this.avgdoclen, df, collectionSize);
		weight = this.W2.containsKey(docID) ? this.W2.get(docID) : 0.0;
		this.W2.put(docID, weight + w2);

		// Add w2 to doc representation
		if (this.W2Doc.get(docID) == null) {
			this.W2Doc.put(docID, new HashMap<String, Double>());
		}

		weight = this.W2Doc.get(docID).containsKey(term) ? this.W2Doc.get(docID).get(term) : 0.0;
		this.W2Doc.get(docID).put(term, weight + w2);
	}

	/**
	 * Computes the weighting function 1
	 *
	 * @param tf
	 *            Term frequency
	 * @param maxtf
	 *            Maximum term frequency
	 * @param df
	 *            Document frequency
	 * @param collectionSize
	 *            Collection size excluding stopwords
	 * @return W1 as double
	 */
	public double W1(final int tf, final int maxtf, final int df, final int collectionSize) {
		double temp = 0;
		try {
			temp = (0.4 + 0.6 * Math.log(tf + 0.5) / Math.log(maxtf + 1.0)) * (Math.log(collectionSize / (double) df) / Math.log(collectionSize));
		} catch (final Exception e) {
			// If a divide by zero or any other error occurred, return 0
			temp = 0;
		}

		return temp;
	}

	/**
	 * Computes the weighting function 2
	 *
	 * @param tf
	 *            Term frequency
	 * @param doclen
	 *            Document length excluding stopwords
	 * @param avgdoclen
	 *            Average document length
	 * @param df
	 *            Document frequency
	 * @param collectionSize
	 *            Collection size excluding stopwords
	 * @return W2 as double
	 */
	public double W2(final int tf, final int doclen, final double avgdoclen, final int df, final int collectionSize) {
		double temp = 0;
		try {
			temp = 0.4 + 0.6 * (tf / (tf + 0.5 + 1.5 * (doclen / avgdoclen))) * Math.log(collectionSize / (double) df) / Math.log(collectionSize);
		} catch (final Exception e) {
			// If a divide by zero or any other error occurred, return 0
			temp = 0;
		}

		return temp;
	}

	/**
	 * @return W1 table
	 */
	public Map<String, Double> getW1() {
		return this.W1;
	}

	/**
	 * @return W2 table
	 */
	public Map<String, Double> getW2() {
		return this.W2;
	}

	/**
	 * @return the w1Doc
	 */
	public final Map<String, Map<String, Double>> getW1Doc() {
		return this.W1Doc;
	}

	/**
	 * @return the w2Doc
	 */
	public final Map<String, Map<String, Double>> getW2Doc() {
		return this.W2Doc;
	}
}