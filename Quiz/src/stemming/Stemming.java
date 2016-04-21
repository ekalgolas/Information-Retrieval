package stemming;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;

/**
 * @author Ekal.Golas
 */
public class Stemming {
	private final Map<String, Set<String>>	stemsMap;

	public Stemming() {
		this.stemsMap = new HashMap<>();
	}

	public void stem(final HashMap<String, Map<Integer, Integer>> tokenMap) throws IOException {
		final Dictionary dict = new Dictionary(new File("F:\\home\\ekal\\Softwares\\NLP\\WordNet-3.0\\dict"));
		dict.open();
		final WordnetStemmer stemmer = new WordnetStemmer(dict);

		final Set<String> set = new HashSet<String>();
		set.addAll(tokenMap.keySet());
		for (final String string1 : tokenMap.keySet()) {
			final List<String> stems1 = new ArrayList<>();
			for (final POS pos : POS.values()) {
				stems1.addAll(stemmer.findStems(string1, pos));
			}

			for (final String string2 : tokenMap.keySet()) {
				if (string1.equals(string2)) {
					continue;
				}

				final List<String> stems2 = new ArrayList<>();
				for (final POS pos : POS.values()) {
					stems2.addAll(stemmer.findStems(string2, pos));
				}

				stems2.retainAll(stems1);
				if (stems2.size() > 0) {
					final String string = stems2.get(0);
					if (!this.stemsMap.containsKey(string)) {
						this.stemsMap.put(string, new HashSet<String>());
					}

					this.stemsMap.get(string).add(string1);
					set.remove(string1);
					this.stemsMap.get(string).add(string2);
					set.remove(string2);
				}
			}
		}

		for (final String string : set) {
			if (!this.stemsMap.containsKey(string)) {
				this.stemsMap.put(string, new HashSet<String>());
			}

			this.stemsMap.get(string).add(string);
		}
	}

	public Map<String, Set<String>> getStemsMap() {
		return this.stemsMap;
	}
}