package indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import store.DocumentProperty;
import store.Properties;

/**
 * Write the contents of a dictionary to a file
 *
 * @author Ekal.Golas
 */
public class FileWriter {
	/**
	 * Writes dictionary to a file
	 *
	 * @param dictionary
	 *            Dictionary to be written
	 * @param fileName
	 *            File name
	 * @return
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public File write(final Map<String, Properties> dictionary, final String fileName) throws FileNotFoundException, UnsupportedEncodingException {
		// Print the dictionary into a file
		final File file = new File(fileName);
		try (final PrintWriter writer = new PrintWriter(file, "UTF-8")) {
			for (final Entry<String, Properties> entry : dictionary.entrySet()) {
				writer.print(entry.getKey() + ":" + entry.getValue().getDocFreq() + " [");

				// Add posting file entries
				final List<String> pairs = new ArrayList<>();
				for (final Entry<String, DocumentProperty> list : entry.getValue().getPostingFile().entrySet()) {
					pairs.add(list.getKey() + ":" + entry.getValue().getTermFreq().get(list.getKey()) + " " + list.getValue().getMaxFreq() + " " +
							list.getValue().getDoclen());
				}

				writer.print(StringUtils.join(pairs, " "));
				writer.println("]");
			}
		}

		return file;
	}
}