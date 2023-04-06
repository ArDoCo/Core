package edu.kit.kastel.mcse.ardoco.core.inconsistency.util.designdecisions;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;

public interface DesignDecisionKindClassifier {
    boolean sentenceHasKind(Sentence sentence, ArchitecturalDesignDecision kind);

    ArchitecturalDesignDecision classifySentence(Sentence sentence);

    boolean sentenceHasDesignDecision(Sentence sentence);
}
