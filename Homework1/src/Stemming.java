import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Class that wraps a publicly available implementation of Porter stemmer
 *
 * @author Ekal.Golas
 */
public class Stemming {
	private HashMap<String, Integer>	stemsMap;
	private int							totalStems;

	/**
	 * Default constructor
	 */
	public Stemming() {
		this.setStemsMap(new HashMap<String, Integer>());
	}

	/**
	 * Takes in a token map and stems all the tokens in it
	 *
	 * @param tokenMap
	 *            token map to stem
	 */
	public void stem(final HashMap<String, Integer> tokenMap) {
		// Go through each token
		final Stemmer stemmer = new Stemmer();
		for (final Entry<String, Integer> entry : tokenMap.entrySet()) {
			// Add all the characters of the token to the stemmer
			final String token = entry.getKey();
			final char[] charArray = token.toCharArray();
			for (final char element : charArray) {
				stemmer.add(element);
			}

			// Run stemming
			stemmer.stem();

			// Get the stemmed word and map its occurence
			final String stemWord = stemmer.toString();
			final int count = this.getStemsMap().getOrDefault(stemWord, 0);
			this.getStemsMap().put(stemWord, count + 1);

			// Count total stemmed words
			this.setTotalStems(this.getTotalStems() + 1);
		}
	}

	/**
	 * @return the stemsMap
	 */
	public HashMap<String, Integer> getStemsMap() {
		return this.stemsMap;
	}

	/**
	 * @param stemsMap
	 *            the stemsMap to set
	 */
	public void setStemsMap(final HashMap<String, Integer> stemsMap) {
		this.stemsMap = stemsMap;
	}

	/**
	 * @return the totalStems
	 */
	public int getTotalStems() {
		return this.totalStems;
	}

	/**
	 * @param totalStems
	 *            the totalStems to set
	 */
	public void setTotalStems(final int totalStems) {
		this.totalStems = totalStems;
	}
}