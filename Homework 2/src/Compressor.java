import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

/**
 * Implements methods needed for index compression
 *
 * @author Ekal.Golas
 */
public class Compressor {
	/**
	 * In Index_Version1.compressed you shall use for dictionary compression a blocked compression with k=8 ( in this version of the index, the dictionary
	 * contains terms) and for the posting file you shall be using gamma encoding for the gaps between document- ids. Because the index also contains for each
	 * dictionary entry the df, and for each document the tf, the doclen and the max_tf, and all these four values are numbers, you should compress them using
	 * the gamma encoding as well.
	 *
	 * @param dictionary
	 *            Dictionary to compress
	 * @param file
	 *            Posting compression file
	 * @param file2
	 *            Dictionary compression file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void blockCompress(final Map<String, Properties> dictionary, final File file, final File file2) throws FileNotFoundException, IOException {
		// Get two files for dictionary and postings
		try (final RandomAccessFile accessFile = new RandomAccessFile(file, "rw"); PrintWriter writer = new PrintWriter(file2)) {
			List<String> words = new ArrayList<String>();
			int count = 0;
			for (final Entry<String, Properties> entry : dictionary.entrySet()) {
				// For each block k = 8, write
				if (count == 0) {
					final String compressed = StringUtils.join(words.toArray());
					words = new ArrayList<String>();

					writer.write(compressed + compressed.length());
				}
				if (count < 8) {
					// Encode tf, df, max_tf, doclen in gamma
					words.add(entry.getKey());
					final byte[] df = gamma(entry.getValue().getDocFreq());
					accessFile.write(df);

					int prev = 0;
					final Map<String, DocumentProperty> tempPostingFile = entry.getValue().getPostingFile();
					for (final Entry<String, DocumentProperty> list : tempPostingFile.entrySet()) {
						final byte[] gap = gamma(Integer.parseInt(list.getKey()) - prev);
						accessFile.write(gap);
						prev = Integer.parseInt(list.getKey());

						final byte[] tf = gamma(entry.getValue().getTermFreq().get(list.getKey()));
						accessFile.write(tf);

						final byte[] doclen = gamma(list.getValue().getDoclen());
						accessFile.write(doclen);

						final byte[] max = gamma(list.getValue().getMaxFreq());
						accessFile.write(max);
					}

					count++;
				}
				if (count == 8) {
					// If count is 8, reset block
					count = 0;
				}
			}
		}
	}

	/**
	 * In Index_Version2.compressed you shall use compression of the dictionary with front- coding and for the posting files you shall use delta codes to encode
	 * the gaps between document-ids. Because the index also contains for each dictionary entry the df, and for each document the tf, the doclen and the max_tf,
	 * and all these four values are numbers, you should compress them using the delta encoding as well.
	 *
	 * @param dictionary
	 *            Dictionary to compress
	 * @param file
	 *            Posting compression file
	 * @param file2
	 *            Dictionary compression file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void frontCodingCompress(final Map<String, Properties> dictionary, final File file, final File file2)
			throws FileNotFoundException,
			IOException {
		// Sort dictionary first
		final List<String> sortedList = new ArrayList<>();
		sortedList.addAll(dictionary.keySet());
		Collections.sort(sortedList);

		// Get the minimum length of prefix
		int minLen = Integer.MAX_VALUE;
		for (final String string : sortedList) {
			minLen = Math.min(minLen, string.length());
		}

		int prefixLen = 0;
		boolean breakFlag = false;
		final StringBuilder frontCodeString = new StringBuilder();

		// Get two files for dictionary and postings
		try (final RandomAccessFile accessFile = new RandomAccessFile(file, "rw"); PrintWriter writer = new PrintWriter(file2)) {
			// Get the prefix length
			while (prefixLen < minLen) {
				final char cur = sortedList.get(0).charAt(prefixLen);
				for (int i = 1; i < sortedList.size(); i++) {
					// if current char does not match, break here
					if (!(sortedList.get(i).charAt(prefixLen) == cur)) {
						breakFlag = true;
						break;
					}
				}

				if (breakFlag) {
					break;
				}

				prefixLen++;
			}

			// Get prefix and append to it if the prefix length is more than 0, i.e. a common prefix exists
			if (prefixLen >= 1) {
				frontCodeString.append(Integer.toString(sortedList.get(0).length())
						+ sortedList.get(0).substring(0, prefixLen) + "*"
						+ sortedList.get(0).substring(prefixLen));
				for (int i = 1; i < sortedList.size(); i++) {
					frontCodeString.append(Integer.toString(sortedList.get(i).length()
							- prefixLen)
							+ "|" + sortedList.get(i).substring(prefixLen));
				}
			} else {
				// Else, just append string and its length
				for (int i = 0; i < sortedList.size(); i++) {
					frontCodeString.append(Integer.toString(sortedList.get(i).length())
							+ sortedList.get(i));
				}
			}

			// Compress the postings file
			for (final String string : sortedList) {
				int prev = 0;
				final Map<String, DocumentProperty> tempPostingFile = dictionary.get(string).getPostingFile();
				for (final Entry<String, DocumentProperty> list : tempPostingFile.entrySet()) {
					final byte[] gap = delta(Integer.parseInt(list.getKey()) - prev);
					accessFile.write(gap);
					prev = Integer.parseInt(list.getKey());

					final byte[] tf = delta(dictionary.get(string).getTermFreq().get(list.getKey()));
					accessFile.write(tf);

					final byte[] doclen = delta(list.getValue().getDoclen());
					accessFile.write(doclen);

					final byte[] max = delta(list.getValue().getMaxFreq());
					accessFile.write(max);
				}
			}

			// Write front code to a different file
			writer.write(frontCodeString.toString());
		}
	}

	/**
	 * Convert a string to bytes
	 *
	 * @param string
	 *            String to convert
	 * @return Converted bytes
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] StringtoBytes(final String string) throws UnsupportedEncodingException {
		final BitSet bitSet = new BitSet(string.length());
		int index = 0;
		while (index < string.length()) {
			if (string.charAt(index) == '1') {
				bitSet.set(index);
			}
			index++;
		}

		final byte[] btob = new byte[(bitSet.length() + 7) / 8];
		int i = 0;
		while (i < bitSet.length()) {
			if (bitSet.get(i)) {
				btob[btob.length - i / 8 - 1] |= 1 << i % 8;
			}

			i++;
		}

		return btob;
	}

	/**
	 * Get gamma code
	 *
	 * @param number
	 *            Number
	 * @return Gamma code
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] gamma(final int number) throws UnsupportedEncodingException {
		final String unary = Integer.toBinaryString(number);
		String compressed = new String();
		int i = 1;
		while (i < unary.length()) {
			compressed = compressed + "1";
			i++;
		}

		compressed = compressed + "0" + unary.substring(1);
		final byte[] bytes = StringtoBytes(compressed);
		return bytes;
	}

	/**
	 * Get delta code
	 *
	 * @param number
	 *            Number
	 * @return Delta code as byte
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] delta(final int number) throws UnsupportedEncodingException {
		final String unary = Integer.toBinaryString(number);
		final int len = unary.length();
		final String lenUnary = Integer.toBinaryString(len);
		String compressed = new String();
		int i = 1;
		while (i < lenUnary.length()) {
			compressed = compressed + "1";
			i++;
		}

		compressed = compressed + "0" + lenUnary.substring(1);// Gamma Changing
		compressed = compressed + unary.substring(1);// Delta Change
		final byte[] bytes = StringtoBytes(compressed);
		return bytes;
	}

	/**
	 * Gets minimum of 4 numbers
	 *
	 * @param a
	 *            Number 1
	 * @param b
	 *            Number 2
	 * @param c
	 *            Number 3
	 * @param d
	 *            Number 4
	 * @return
	 */
	public static int min(final int a, final int b, final int c, final int d) {
		return Math.min(a, Math.min(b, Math.min(c, d)));
	}
}
