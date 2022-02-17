package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.sewordsim;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SEWordSimDataSource implements AutoCloseable {

    private static final String EXISTS_QUERY = "SELECT EXISTS(SELECT * FROM `Word_Similarity` WHERE `term_1` = ?) AS `ex`;";
    private static final String SELECT_QUERY = "SELECT `similarity` FROM `Word_Similarity` WHERE `term_1` = ? AND `term_2` = ?;";
    private static final String SELECT_ALL_QUERY = "SELECT DISTINCT `term_1` FROM `Word_Similarity`;";

    private final Connection connection;

    public SEWordSimDataSource(Path sqliteFile) throws SQLException {
        Objects.requireNonNull(sqliteFile);
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile);
    }

    public boolean containsWord(String word) throws SQLException {
        word = PorterStemmer.stem(word);

        try (var statement = this.connection.prepareStatement(EXISTS_QUERY)) {
            statement.setString(1, word);

            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("ex") > 0;
                }
            }
        }

        return false;
    }

    public Optional<Double> getSimilarity(String firstWord, String secondWord) throws SQLException {
        firstWord = PorterStemmer.stem(firstWord);
        secondWord = PorterStemmer.stem(secondWord);

        try (var statement = this.connection.prepareStatement(SELECT_QUERY)) {
            statement.setString(1, firstWord);
            statement.setString(2, secondWord);

            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    var similarity = result.getDouble("similarity");
                    return Optional.of(similarity);
                }
            }
        }

        return Optional.empty();
    }

    public List<String> getAllWords() throws SQLException {
        var words = new ArrayList<String>();

        try (var statement = this.connection.createStatement()) {
            try (var result = statement.executeQuery(SELECT_ALL_QUERY)) {
                while (result.next()) {
                    String word = result.getString("term_1");
                    words.add(word);
                }
            }
        }

        return words;
    }

    @Override
    public void close() throws SQLException {
        this.connection.close();
    }

}
