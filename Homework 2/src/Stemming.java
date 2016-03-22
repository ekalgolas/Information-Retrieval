

/**
 * Class that wraps a publicly available implementation of Porter stemmer
 *
 * @author Ekal.Golas
 */
public class Stemming {
	/**
	 * Takes in a token map and stems all the tokens in it
	 *
	 * @param token
	 *            token to stem
	 * @return Stemmed token
	 */
	public static String stem(final String token) {
		final Stemmer stemmer = new Stemmer();

		final char[] charArray = token.toCharArray();
		for (final char element : charArray) {
			stemmer.add(element);
		}

		// Run stemming
		stemmer.stem();

		// Get the stemmed word and map its occurence
		return stemmer.toString();
	}
}