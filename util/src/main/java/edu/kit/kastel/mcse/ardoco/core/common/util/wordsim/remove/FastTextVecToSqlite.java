/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.remove;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FastTextVecToSqlite {

    public static void main(String[] args) throws SQLException, IOException {
        Path vecFile = Path.of("C:\\dev\\uni\\fastText-0.9.2\\crawl-300d-2M-subword.vec");
        // Path vecFile = Path.of("C:\\dev\\uni\\fastText-0.9.2\\testvectors.vec");
        Path dbFile = vecFile.getParent().resolve("C:\\dev\\uni\\fastText-0.9.2\\vectors.sqlite");

        var cfg = new SQLiteConfig();
        cfg.setLockingMode(SQLiteConfig.LockingMode.EXCLUSIVE);
        cfg.setJournalMode(SQLiteConfig.JournalMode.OFF);
        cfg.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
        cfg.setOpenMode(SQLiteOpenMode.NOMUTEX);

        try (var connection = cfg.createConnection("jdbc:sqlite:" + dbFile)) {
            var sql = "INSERT INTO `words` (`word`, `vec`) VALUES (?, ?);";

            try (var statement = connection.prepareStatement(sql)) {
                try (var reader = new BufferedReader(new FileReader(vecFile.toFile()))) {
                    var line = reader.readLine(); // skip first line
                    var totalVectorCount = Integer.parseInt(line.split(" ")[0]);
                    var vectorDimension = Integer.parseInt(line.split(" ")[1]);
                    var lineCount = 0;
                    var skip = 1000001;
                    var maxRead = 3000000;
                    var linesInserted = 0;

                    var data = new String[0];
                    var word = "";
                    var buffer = ByteBuffer.allocate(4 * vectorDimension);

                    System.out.println("totalveccount " + totalVectorCount);
                    System.out.println("dim " + vectorDimension);

                    long start = System.currentTimeMillis();

                    while ((line = reader.readLine()) != null) {
                        lineCount++;

                        if (lineCount < skip) {
                            continue;
                        }

                        if (lineCount > maxRead) {
                            break;
                        }

                        data = line.split(" ");
                        word = data[0];

                        if (vectorDimension != data.length - 1) {
                            throw new IllegalStateException("invalid " + vectorDimension + " != " + line);
                        }

                        for (int i = 1; i < data.length; i++) {
                            float vectorElement = Float.parseFloat(data[i]);
                            buffer.putFloat(vectorElement);
                        }

                        statement.setString(1, word);
                        statement.setBytes(2, buffer.array());

                        statement.execute();
                        linesInserted++;

                        System.out.println(lineCount + ": " + line);

                        buffer.clear();
                    }

                    long end = System.currentTimeMillis();
                    System.out.println("Processed " + linesInserted + " lines in " + ((end - start) / 1000) + " seconds!");
                }
            }
        }

        System.out.println("Done!");
    }

    private static void setVector(PreparedStatement statement, int index, float[] vector) throws SQLException {
        ByteBuffer buffer = ByteBuffer.allocate(4 * vector.length);
        for (float v : vector) {
            buffer.putFloat(v);
        }

        var array = buffer.array();

        statement.setBytes(index, array);
    }

    private static float[] readVector(ResultSet resultSet, String column) throws SQLException {
        var vecBytes = resultSet.getBytes(column);

        ByteBuffer buffer = ByteBuffer.wrap(vecBytes);
        float[] vec = new float[buffer.capacity() / 4];

        for (int i = 0; i < vec.length; i++) {
            vec[i] = buffer.getFloat();
        }

        return vec;
    }

    private static void printHexString() {
        byte[] bytes = { (byte) 0x40, (byte) 0xb0, (byte) 0x00, (byte) 0x00, (byte) 0x40, (byte) 0xa3, (byte) 0x33, (byte) 0x33, (byte) 0x40, (byte) 0xa6,
                (byte) 0x66, (byte) 0x66, (byte) 0x40, (byte) 0xa9, (byte) 0x99, (byte) 0x9a, };

        var buffer = ByteBuffer.wrap(bytes);

        for (int i = 0; i < 4; i++) {
            System.out.println(buffer.getFloat());
        }

        throw new RuntimeException();
    }

}
