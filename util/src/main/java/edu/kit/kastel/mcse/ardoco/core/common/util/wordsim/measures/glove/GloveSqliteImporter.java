package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.glove;

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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Reads a file containing pre-trained glove vector representations and inserts them into a sqlite database.
 * The database must contain a table called {@code words} with two columns: {@code word} and {@code vec}.
 * The {@code word} column must be a {@code TEXT} column while the {@code vec} column must be a {@code BLOB}.
 * Vector representations will be inserted as a consecutive sequence of floats.
 * The amount of floats in a sequence depends on the dimension of the vectors.
 */
public class GloveSqliteImporter {

    /**
     * Executes the import process.
     * The first string in the args array must be the path to the file containing the vector representations.
     * The second string in the args array must be the path to the sqlite database file.
     * The third string in the args array must be the dimension of the vectors.
     * @param args the args
     * @throws IOException if an io error occurs
     * @throws SQLException if a database related error occurs
     */
    public static void main(String[] args) throws IOException, SQLException {
        Path vectorFile = Path.of(args[0]);
        Path dbFile = Path.of(args[1]);
        int dimension = Integer.parseInt(args[2]);

        long lineStart = 0;
        long lineEnd = 3000000;
        long linesRead = 0;
        long inserted = 0;
        boolean stem = false;
        boolean lowercase = false;

        var cfg = new SQLiteConfig();
        cfg.setLockingMode(SQLiteConfig.LockingMode.EXCLUSIVE);
        cfg.setJournalMode(SQLiteConfig.JournalMode.OFF);
        cfg.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
        cfg.setOpenMode(SQLiteOpenMode.NOMUTEX);

        var skipped = new ArrayList<String>();

        try (var connection = cfg.createConnection("jdbc:sqlite:" + dbFile.toAbsolutePath())) {
            try (var statement = connection.prepareStatement("INSERT INTO `words` (`word`, `vec`) VALUES (?, ?);")) {
                try (var in = Files.newInputStream(vectorFile)) {
                    try (var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                        try (var bufferedReader = new BufferedReader(reader)) {
                            ByteBuffer buffer = ByteBuffer.allocate(dimension * 4);

                            while (bufferedReader.ready()) {
                                var line = bufferedReader.readLine();
                                var parts = line.split(" ");

                                linesRead++;

                                if (linesRead < lineStart) {
                                    continue;
                                }

                                if (linesRead > lineEnd) {
                                    break;
                                }

                                String word = parts[0];

                                if (word.length() > 300) {
                                    // Filter out weird words from dataset
                                    skipped.add(word);
                                    continue;
                                }

                                if (lowercase) {
                                    word = word.toLowerCase(Locale.ROOT);
                                }

                                if (stem) {
                                    word = PorterStemmer.stem(word);
                                }

                                buffer.clear();

                                for (int i = 0; i < parts.length - 1; i++) {
                                    float value = Float.parseFloat(parts[i + 1]);
                                    buffer.putFloat(value);
                                }

                                statement.setString(1, word);
                                statement.setBytes(2, buffer.array());
                                statement.execute();

                                inserted++;
                            }
                        }
                    }
                }
            }

            System.out.println("==============================================");
            System.out.printf("Inserted: %s%n", inserted);
            System.out.println("Skipped:");

            for (String skippedWord : skipped) {
                System.out.printf("%s: %s%n", skippedWord.length(), skippedWord);
            }
        }
    }

}