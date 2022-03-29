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

public class GloveSqliteImporter {

    public static void main(String[] args) throws IOException, SQLException {
        // 2 196 017 lines
        Path p =      Path.of("C:\\dev\\uni\\bachelor\\Notizen\\wordsim\\data\\glove\\twitter\\glove.twitter.27B.25d.txt");
        Path dbFile = Path.of("C:\\dev\\uni\\bachelor\\Notizen\\wordsim\\data\\glove\\twitter\\glove_twitter_25d.sqlite");

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

        // INSERT
        try (var connection = cfg.createConnection("jdbc:sqlite:" + dbFile.toAbsolutePath())) {
            try (var statement = connection.prepareStatement("INSERT INTO `words` (`word`, `vec`) VALUES (?, ?);")) {
                try (var in = Files.newInputStream(p)) {
                    try (var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                        try (var bufferedReader = new BufferedReader(reader)) {
                            int dimension = 300;
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

                                //System.out.println(word.length() + ": " + word);
                                statement.setString(1, word);
                                statement.setBytes(2, buffer.array());
                                statement.execute();

                                inserted++;
                            }
                        }
                    }
                }
            }

            System.out.println("=======================================================================================");
            System.out.println("Inserted: " + inserted);
            System.out.println("Skipped:");

            for (String s : skipped) {
                System.out.println(s.length() + ": " + s);
            }
        }


    }

}
