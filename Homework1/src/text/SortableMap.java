package text;

import java.util.Comparator;
import java.util.Map;

/**
 * Class that wraps a map and implements comparator for it to be sorted on keys
 *
 * @author Ekal.Golas
 */
public class SortableMap implements Comparator<String> {
	private final Map<String, Integer>	map;

	/**
	 * Default constructor
	 *
	 * @param map
	 *            Map to sort
	 */
	public SortableMap(final Map<String, Integer> map) {
		this.map = map;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(final String s1, final String s2) {
		return this.map.get(s1) >= this.map.get(s2) ? 1 : -1;
	}
}