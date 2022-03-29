package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.glove;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Provides GloVe trained word vector representations stored in a sqlite file.
 * Instances of this class keep an open connection to the sqlite file until {@link #close()} is called.
 */
public class GloveSqliteDataSource implements AutoCloseable {

    private static final int BYTES_PER_FLOAT = 4;
    private static final String SELECT_QUERY = "SELECT `vec` FROM `words` WHERE `word` = ?";

    private final Connection connection;
    private final PreparedStatement selectStatement;

    /**
     * Instantiates the {@link GloveSqliteDataSource}.
     * Once instantiated, a connection to the file will be kept open until {@link #close()} is called on this instance.
     *
     * @param sqliteFile the path to the sqlite file
     * @throws SQLException if connecting to the sqlite database fails
     */
    public GloveSqliteDataSource(Path sqliteFile) throws SQLException {
        if (!Files.exists(sqliteFile)) {
            throw new IllegalArgumentException("sqliteFile does not exist: " + sqliteFile);
        }

        var cfg = new SQLiteConfig();
        cfg.setReadOnly(true);
        cfg.setLockingMode(SQLiteConfig.LockingMode.EXCLUSIVE);
        cfg.setJournalMode(SQLiteConfig.JournalMode.OFF);
        cfg.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
        cfg.setOpenMode(SQLiteOpenMode.NOMUTEX);

        this.connection = cfg.createConnection("jdbc:sqlite:" + sqliteFile);
        this.selectStatement = this.connection.prepareStatement(SELECT_QUERY);
    }

    /**
     * Attempts to retrieve the vector representation of the given word.
     *
     * @param word the word
     * @return the vector representation, or {@link Optional#empty()} if no representation exists in the database.
     * @throws SQLException if a database access error occurs
     */
    public Optional<float[]> getWordVector(String word) throws SQLException {
        this.selectStatement.setString(1, word);

        try (var result = this.selectStatement.executeQuery()) {
            if (result.next()) {
                ByteBuffer bytes = ByteBuffer.wrap(result.getBytes("vec"));
                float[] vec = new float[bytes.capacity() / BYTES_PER_FLOAT];

                for (int i = 0; i < vec.length; i++) {
                    vec[i] = bytes.getFloat();
                }

                return Optional.of(vec);
            }
        }

        return Optional.empty();
    }

    @Override public void close() throws Exception {
        this.selectStatement.close();
        this.connection.close();
    }

}
