package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector;

import com.google.common.collect.ImmutableList;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.sewordsim.PorterStemmer;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reads a file containing word vector embeddings and inserts them into a sqlite database.
 * The database must contain a table called {@code words} with two columns: {@code word} and {@code vec}.
 * The {@code word} column must be a {@code TEXT} column while the {@code vec} column must be a {@code BLOB}.
 * Vector representations will be inserted as a consecutive sequence of floats.
 * The amount of floats in a sequence depends on the dimension of the vectors.
 */
public class WordVectorSqliteImporter {

    // TODO: Make this class more customizable (through extending)
    // TODO: Use logger
    // and move this class to tests?

	/**
	 * Launches an import process.
	 * The first string in the args array must be the path to the file containing the vector representations.
	 * The second string in the args array must be the path to the sqlite database file.
	 * The third string in the args array must be the dimension of the vectors.
	 *
	 * @param args the args array
	 * @throws IOException  if an io error occurs
	 * @throws SQLException if a database related error occurs
	 */
	public static void main(String[] args) throws SQLException, IOException {
		ImportResult result = new WordVectorSqliteImporter(
			Path.of(args[0]),
			Path.of(args[1]),
			Integer.parseInt(args[2])
		).beginImport();

		System.out.printf("Inserted: %s%n", result.inserted);
		System.out.println("Skipped:");

		result.skippedWords.forEach(word -> System.out.printf("%s%n", word));
	}

	private static final int MAX_WORD_LENGTH = 300;

	record ImportResult(long inserted, ImmutableList<String> skippedWords) { }

	private final Path vectorFile;
	private final Path dbFile;
	private final int dimension;

	private final long startLine;
	private final long endLine;
	private final boolean stem;
	private final boolean lowercase;
	private final boolean dryRun;

	/**
	 * Constructs a new {@link WordVectorSqliteImporter} instance.
	 *
	 * @param vectorFile the path to the file that contains the vector representations for each word
	 * @param dbFile     the path to the sqlite database into which the vector representations will be inserted
	 * @param dimension  the dimension of the vectors
	 */
	public WordVectorSqliteImporter(Path vectorFile, Path dbFile, int dimension) {
		this(vectorFile, dbFile, dimension, false, false, 0L, -1L, false);
	}

	/**
	 * Constructs a new {@link WordVectorSqliteImporter} instance.
	 *
	 * @param vectorFile the path ot the file that contains the vector representations for each word
	 * @param dbFile     the path to the sqlite database into which the vector representations will be inserted
	 * @param dimension  the dimension of the vectors
	 * @param stem       whether to stem a word before inserting it into the database
	 * @param lowercase  whether to lowercase a word before inserting it into the database
	 */
	public WordVectorSqliteImporter(Path vectorFile, Path dbFile, int dimension, boolean stem, boolean lowercase) {
		this(vectorFile, dbFile, dimension, stem, lowercase, 0L, -1L, false);
	}

	/**
	 * Constructs a new {@link WordVectorSqliteImporter} instance.
	 * To start the import process, call {@link #beginImport()}.
	 *
	 * @param vectorFile the path ot the file that contains the vector representations for each word
	 * @param dbFile     the path to the sqlite database into which the vector representations will be inserted
	 * @param dimension  the dimension of the vectors
	 * @param stem       whether to stem a word before inserting it into the database
	 * @param lowercase  whether to lowercase a word before inserting it into the database
	 * @param startLine  at which line of the {@code vectorFile} this importer will start inserting
	 * @param endLine    at which line of the {@code vectorFile} this importer will stop inserting
	 * @param dryRun     whether this importer should actually insert. Use {@code false} to run this importer without
	 *                   actually inserting anything
	 */
	public WordVectorSqliteImporter(Path vectorFile, Path dbFile, int dimension, boolean stem, boolean lowercase,
	                                long startLine, long endLine, boolean dryRun) {
		this.vectorFile = vectorFile;
		this.dbFile = dbFile;
		this.dimension = dimension;
		this.stem = stem;
		this.lowercase = lowercase;
		this.startLine = startLine;
		this.endLine = endLine;
		this.dryRun = dryRun;

		if (!Files.exists(vectorFile)) {
			throw new IllegalStateException("vectorFile does not exist: " + vectorFile);
		}

		if (!Files.exists(dbFile)) {
			throw new IllegalStateException("dbFile does not exist: " + dbFile);
		}
	}

	/**
	 * Starts the import process.
	 * @return the result of the import process
	 * @throws SQLException if an error occurs while interacting with the database
	 * @throws IOException if an error occurs while interacting with the vector file
	 */
	public ImportResult beginImport() throws SQLException, IOException {
		final List<String> skippedWords = new ArrayList<>();
		long linesRead = 0;
		long inserted = 0;

		try (Connection connection = connect()) {
			try (PreparedStatement statement = prepareSelect(connection)) {
				try (var in = Files.newInputStream(vectorFile, StandardOpenOption.READ)) {
					try (var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
						try (var bufferedReader = new BufferedReader(reader)) {
							ByteBuffer buffer = ByteBuffer.allocate(dimension * 4);

							while (bufferedReader.ready()) {
								var line = bufferedReader.readLine();
								var parts = line.split(" ");

								linesRead++;

								if (linesRead < startLine) {
									continue;
								}

								if (linesRead > endLine) {
									break;
								}

                                if (parts.length - 1 != this.dimension) {
                                    throw new IllegalStateException("importer has read line with invalid vector dimension: \"" + line + "\"");
                                }

                                String word = parts[0];

								if (word.length() > MAX_WORD_LENGTH) {
									// Filter out weird words from dataset
									skippedWords.add(word);
									continue;
								}

								if (lowercase) {
									word = word.toLowerCase(Locale.ROOT);
								}

                                // Insert into database
								buffer.clear();

								for (int i = 0; i < parts.length - 1; i++) {
									float value = Float.parseFloat(parts[i + 1]);
									buffer.putFloat(value);
								}

								if (!dryRun) {
									statement.setString(1, word);
									statement.setBytes(2, buffer.array());
									statement.execute();
								}
                                else {
                                    System.out.println("Would have inserted: " + word);
                                }

								inserted++;
							}
						}
					}
				}
			}
		}

		return new ImportResult(inserted, ImmutableList.copyOf(skippedWords));
	}

	private Connection connect() throws SQLException {
		var cfg = new SQLiteConfig();
		cfg.setLockingMode(SQLiteConfig.LockingMode.EXCLUSIVE);
		cfg.setJournalMode(SQLiteConfig.JournalMode.OFF);
		cfg.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
		cfg.setOpenMode(SQLiteOpenMode.NOMUTEX);

		return cfg.createConnection("jdbc:sqlite:" + this.dbFile.toAbsolutePath());
	}

	private PreparedStatement prepareSelect(Connection conn) throws SQLException {
		return conn.prepareStatement("INSERT INTO `words` (`word`, `vec`) VALUES (?, ?);");
	}

    protected String processWord(String word) {
        if (this.lowercase) {
            word = word.toLowerCase(Locale.ROOT);
        }

        if (this.stem) {
            word = PorterStemmer.stem(word);
        }

        return word;
    }

}
