package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText;

import edu.kit.kastel.mcse.ardoco.core.common.util.VectorUtils;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class SqliteFastTextDataSource implements FastTextDataSource {

    //public record VectorPair(float[] firstVec, float[] secondVec) { }

    private final Connection connection;
    private final PreparedStatement statement;

    public SqliteFastTextDataSource(Path sqliteFile) throws SQLException {
        Objects.requireNonNull(sqliteFile);

        var cfg = new SQLiteConfig();
        cfg.setReadOnly(true);
        cfg.setLockingMode(SQLiteConfig.LockingMode.EXCLUSIVE);
        cfg.setJournalMode(SQLiteConfig.JournalMode.OFF);
        cfg.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
        cfg.setOpenMode(SQLiteOpenMode.NOMUTEX);

        this.connection = cfg.createConnection("jdbc:sqlite:" + sqliteFile);
        this.statement = this.connection.prepareStatement("SELECT `vec` FROM `words` WHERE `word` = ? LIMIT 1;");
    }

    public Optional<float[]> getVector(String word) throws RetrieveVectorException {
        try {
            this.statement.setString(1, word);

            try (var resultSet = this.statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty(); // word is not in the database
                }

                byte[] bytes = resultSet.getBytes(1);
                float[] vec = byteToFloat(bytes);

                return Optional.of(vec);
            }
        }
        catch (SQLException e) {
            throw new RetrieveVectorException(word, e);
        }
    }

//    public Optional<VectorPair> getVectors(String firstWord, String secondWord) throws RetrieveVectorException {
//        try {
//            this.statement.setString(1, firstWord);
//            this.statement.setString(2, secondWord);
//
//            try (var resultSet = this.statement.executeQuery()) {
//                if (!resultSet.next()) {
//                    return Optional.empty(); // first word is missing in database
//                }
//
//                byte[] firstBytes = resultSet.getBytes(1);
//
//                if (!resultSet.next()) {
//                    return Optional.empty(); // second word is mising in database
//                }
//
//                byte[] secondBytes = resultSet.getBytes(1);
//
//                float[] firstVec = byteToFloat(firstBytes);
//                float[] secondVec = byteToFloat(secondBytes);
//
//                return Optional.of(new VectorPair(firstVec, secondVec));
//            }
//        }
//        catch (SQLException e) {
//            throw new RetrieveVectorException(e);
//        }
//    }

    @Override public Optional<Double> getSimilarity(String firstWord, String secondWord) throws RetrieveVectorException {
        var firstVec = getVector(firstWord).orElse(null);
        if (firstVec == null) { return Optional.empty(); }

        var secondVec = getVector(secondWord).orElse(null);
        if (secondVec == null) { return Optional.empty(); }

        return Optional.of(VectorUtils.cosineSimilarity(firstVec, secondVec));
    }

    @Override public void close() {
        try { this.statement.close(); } catch (SQLException ignore) { }
        try { this.connection.close(); } catch (SQLException ignore) { }
    }

    private float[] byteToFloat(byte[] bytes) {
        var buffer = ByteBuffer.wrap(bytes);
        var floatArray = new float[bytes.length / 4];

        for (int i = 0; i < floatArray.length; i++) {
            floatArray[i] = buffer.getFloat();
        }

        return floatArray;
    }

}
