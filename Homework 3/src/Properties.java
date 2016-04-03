

import java.util.HashMap;
import java.util.Map;

public class Properties {
	private int								docFreq;
	private Map<String, DocumentProperty>	postingFile	= new HashMap<>();
	private Map<String, Integer>			termFreq	= new HashMap<>();

	/**
	 * @return the termFreq
	 */
	public final Map<String, Integer> getTermFreq() {
		return this.termFreq;
	}

	/**
	 * @param termFreq
	 *            the termFreq to set
	 */
	public final void setTermFreq(final Map<String, Integer> termFreq) {
		this.termFreq = termFreq;
	}

	/**
	 * @return the docFreq
	 */
	public final int getDocFreq() {
		return this.docFreq;
	}

	/**
	 * @param docFreq
	 *            the docFreq to set
	 */
	public final void setDocFreq(final int docFreq) {
		this.docFreq = docFreq;
	}

	/**
	 * @return the postingFile
	 */
	public final Map<String, DocumentProperty> getPostingFile() {
		return this.postingFile;
	}

	/**
	 * @param postingFile
	 *            the postingFile to set
	 */
	public final void setPostingFile(final Map<String, DocumentProperty> postingFile) {
		this.postingFile = postingFile;
	}

}