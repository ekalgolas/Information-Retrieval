import java.util.HashSet;
import java.util.Set;

/**
 * Stores the length and maximum frequency properties for a document
 *
 * @author Ekal.Golas
 */
public class DocumentProperty {
	private int					doclen;
	private int					maxFreq;
	private String				headline;
	private final Set<String>	words;

	/**
	 * Default constructor
	 */
	public DocumentProperty() {
		this.words = new HashSet<>();
	}

	/**
	 * @return the doclen
	 */
	public final int getDoclen() {
		return this.doclen;
	}

	/**
	 * @param doclen
	 *            the doclen to set
	 */
	public final void setDoclen(final int doclen) {
		this.doclen = doclen;
	}

	/**
	 * @return the maxFreq
	 */
	public final int getMaxFreq() {
		return this.maxFreq;
	}

	/**
	 * @param maxFreq
	 *            the maxFreq to set
	 */
	public final void setMaxFreq(final int maxFreq) {
		this.maxFreq = maxFreq;
	}

	/**
	 * @return the headline
	 */
	public String getHeadline() {
		return this.headline;
	}

	/**
	 * @param headline
	 *            the headline to set
	 */
	public void setHeadline(final String headline) {
		this.headline = headline;
	}

	/**
	 * @return the words
	 */
	public final Set<String> getWords() {
		return this.words;
	}
}