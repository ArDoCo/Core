package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.agents.AgentDatastructure;
import edu.kit.ipd.consistency_analyzer.agents.DependencyType;
import edu.kit.ipd.consistency_analyzer.agents.Loader;
import edu.kit.ipd.consistency_analyzer.agents.TextAgent;
import edu.kit.ipd.consistency_analyzer.common.Tuple;
import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;
import edu.kit.ipd.consistency_analyzer.extractors.IExtractor;
import edu.kit.ipd.consistency_analyzer.extractors.TextExtractor;

@MetaInfServices(TextAgent.class)
public class InitialTextAgent extends TextAgent {

    private List<IExtractor> extractors = new ArrayList<>();

    private Logger logger = Logger.getLogger(getName());

    public InitialTextAgent(IText text, ITextState textState) {
        super(DependencyType.TEXT, text, textState);
        initializeAgents();
    }

    @Override
    public TextAgent create(IText text, ITextState textExtractionState) {
        return new InitialTextAgent(text, textExtractionState);
    }

    public InitialTextAgent(AgentDatastructure data) {
        this(data.getText(), data.getTextState());
    }

    public InitialTextAgent(AgentDatastructure data, Tuple<List<IExtractor>, Map<IExtractor, List<Double>>> extractorsTuple) {
        this(data);

        extractors = new ArrayList<>(extractorsTuple.getFirst());
        Map<IExtractor, List<Double>> map = extractorsTuple.getSecond();
        for (IExtractor extractor : extractors) {
            if (map.containsKey(extractor)) {
                extractor.setProbability(map.get(extractor));
            } else {
                logger.warning("The extractor " + extractor.getName() + " has no specified probability!");
            }
        }
    }

    public InitialTextAgent() {
        super(DependencyType.TEXT);
    }

    @Override
    public void exec() {
        for (IWord word : text.getWords()) {
            for (IExtractor extractor : extractors) {
                extractor.exec(word);
            }
        }
    }

    /**
     * Initializes graph dependent analyzers and solvers
     */

    private void initializeAgents() {

        Map<String, TextExtractor> loadedExtractors = Loader.loadLoadable(TextExtractor.class);

        for (String textExtractor : GenericTextConfig.TEXT_EXTRACTORS) {
            if (!loadedExtractors.containsKey(textExtractor)) {
                throw new IllegalArgumentException("TextAgent " + textExtractor + " not found");
            }
            extractors.add(loadedExtractors.get(textExtractor).create(textState));
        }
    }

}
