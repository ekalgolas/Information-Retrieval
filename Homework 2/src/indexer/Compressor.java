package indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import store.DocumentProperty;
import store.Properties;

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
		// Get two files for dictionary and postings
		try (final RandomAccessFile accessFile = new RandomAccessFile(file, "rw"); PrintWriter writer = new PrintWriter(file2)) {
			List<String> words = new ArrayList<String>();
			int count = 0;
			for (final Entry<String, Properties> entry : dictionary.entrySet()) {
				// For each block k = 8, write
				if (count == 0 && words.size() > 0) {
					final String compressed = dictionaryCompress(words);
					words = new ArrayList<String>();

					writer.write(compressed + compressed.length());
				}
				if (count < 8) {
					// Encode tf, df, max_tf, doclen in gamma
					words.add(entry.getKey());
					final byte[] df = delta(entry.getValue().getDocFreq());
					accessFile.write(df);

					int prev = 0;
					final Map<String, DocumentProperty> tempPostingFile = entry.getValue().getPostingFile();
					for (final Entry<String, DocumentProperty> list : tempPostingFile.entrySet()) {
						final byte[] gap = delta(Integer.parseInt(list.getKey()) - prev);
						accessFile.write(gap);
						prev = Integer.parseInt(list.getKey());

						final byte[] tf = delta(entry.getValue().getTermFreq().get(list.getKey()));
						accessFile.write(tf);

						final byte[] doclen = delta(list.getValue().getDoclen());
						accessFile.write(doclen);

						final byte[] max = delta(list.getValue().getMaxFreq());
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
	 * Returns front coded string split into 2 blocks of 4
	 *
	 * @param words
	 *            List of words to compress
	 * @return Compressed string
	 */
	private static String dictionaryCompress(final List<String> words) {
		String[] Array = new String[words.size()];
		Array = words.toArray(Array);
		final String first = frontcode(Array, 0);
		final String second = frontcode(Array, 4);
		return first + second;
	}

	private static String frontcode(final String strings[], final int a) {
		int i = 0, minlen;
		String compressed = new String();
		minlen = min(strings[a].length(), strings[a + 1].length(), strings[a + 2].length(), strings[a + 3].length());
		while (i < minlen) {
			if (strings[a].charAt(i) == strings[a + 1].charAt(i) & strings[a].charAt(i) == strings[a + 2].charAt(i) &
					strings[a].charAt(i) == strings[a + 3].charAt(i)) {
				i++;
			} else {
				break;
			}
		}
		if (i >= 2) {
			compressed = Integer.toString(strings[a].length()) + strings[a].substring(0, i) + "*" + strings[a].substring(i) + "1◊" +
					strings[a + 1].substring(i) + "2◊" +
					strings[a + 2].substring(i) + "3◊" + strings[a + 3].substring(i);
		} else {
			compressed = Integer.toString(strings[a].length()) + strings[a];
			compressed = compressed + Integer.toString(strings[a + 1].length()) + strings[a + 1];
			compressed = compressed + Integer.toString(strings[a + 2].length()) + strings[a + 2];
			compressed = compressed + Integer.toString(strings[a + 3].length()) + strings[a + 3];
		}

		return compressed;
	}

	public static byte[] StringtoBytes(final String fin) throws UnsupportedEncodingException {
		final BitSet sbit = new BitSet(fin.length());
		int i1 = 0;
		while (i1 < fin.length()) {
			if (fin.charAt(i1) == '1') {
				sbit.set(i1);
			}
			i1++;
		}
		final byte[] btob = new byte[(sbit.length() + 7) / 8];
		int i = 0;
		while (i < sbit.length()) {
			if (sbit.get(i)) {
				btob[btob.length - i / 8 - 1] |= 1 << i % 8;
			}
			i++;
		}
		return btob;
	}

	public static byte[] gamma(final int id) throws UnsupportedEncodingException {
		final String id_string = Integer.toBinaryString(id);
		String fin = new String();
		int i = 1;
		while (i < id_string.length()) {
			fin = fin + "1";
			i++;
		}
		fin = fin + "0" + id_string.substring(1);
		final byte[] bytes = StringtoBytes(fin);
		return bytes;
	}

	public static byte[] delta(final int id) throws UnsupportedEncodingException {
		final String string = Integer.toBinaryString(id);
		final int len = string.length();
		final String id_string = Integer.toBinaryString(len);
		String f = new String();
		int i = 1;
		while (i < id_string.length()) {
			f = f + "1";
			i++;
		}
		f = f + "0" + id_string.substring(1);// Gamma Changing
		f = f + string.substring(1);// Delta Change
		final byte[] bytes = StringtoBytes(f);
		return bytes;
	}

	public static int min(final int a, final int b, final int c, final int d) {
		return Math.min(a, Math.min(b, Math.min(c, d)));
	}
}
