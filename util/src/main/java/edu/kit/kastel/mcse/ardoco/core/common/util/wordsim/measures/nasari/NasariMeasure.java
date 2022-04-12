package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.BabelNetDataSource;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.VectorSqliteDatabase;

public class NasariMeasure implements WordSimMeasure {

    private final BabelNetDataSource babelNetData;
    private final VectorSqliteDatabase vectorDatabase;

    @Override public boolean areWordsSimilar(ComparisonContext ctx) {
        // 1.) Get BabelNet Synsets for terms
        // 2.) Check
        return false;
    }

}
