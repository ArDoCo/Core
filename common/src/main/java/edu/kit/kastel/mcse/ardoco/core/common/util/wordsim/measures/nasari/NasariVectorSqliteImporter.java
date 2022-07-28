/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari;

import java.nio.file.Path;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.WordVectorSqliteImporter;

/**
 * Reads Nasari vectors and imports them into a sqlite database. The sqlite database must conform to the requirements
 * explained in {@link WordVectorSqliteImporter}.
 */
public class NasariVectorSqliteImporter extends WordVectorSqliteImporter {

    private static final int NASARI_VECTORS_DIMENSION = 300;

    public NasariVectorSqliteImporter(Path vectorFile, Path dbFile, int dimension, long startLine, long endLine, boolean dryRun) {
        super(vectorFile, dbFile, dimension, NASARI_VECTORS_DIMENSION, startLine, endLine, dryRun);
    }

    @Override
    protected String processWord(String word) {
        word = super.processWord(word);

        // Nasari words look like this: bn:00000003n__.22_Long_Rifle
        // We only need the id (the part before __)

        word = word.split("__")[0];

        return word;
    }

}
