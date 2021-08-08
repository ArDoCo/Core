package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents_extractors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.RecommendedRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Loader;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.*;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.DependencyExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DependencyExtractionAgent extends DependencyAgent {
    private List<DependencyExtractor> extractors = new ArrayList<>();

    public DependencyExtractionAgent() {
        super(GenericRecommendationConfig.class);
    }

    public DependencyExtractionAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
                                     GenericRecommendationConfig config) {
        super(DependencyType.TEXT_MODEL_RECOMMENDATION, GenericRecommendationConfig.class, text, textState, modelState, recommendationState);
        initializeExtractors(config.dependencyExtractors, config);
    }

    private void initializeExtractors(List<String> extractorList, GenericRecommendationConfig config) {
        Map<String, DependencyExtractor> loadedExtractors = Loader.loadLoadable(DependencyExtractor.class);

        for (String dependencyExtractor : extractorList) {
            if (!loadedExtractors.containsKey(dependencyExtractor)) {
                throw new IllegalArgumentException("DependencyExtractor " + dependencyExtractor + " not found");
            }
            extractors.add(loadedExtractors.get(dependencyExtractor).create(textState, modelState, recommendationState, config));
        }
    }

    @Override
    public DependencyExtractionAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState, Configuration config) {
        return new DependencyExtractionAgent(text, textState, modelState, recommendationState, (GenericRecommendationConfig) config);
    }

    @Override
    public void exec() {
        getRelations();
    }

    private List<RecommendedRelation> getRelations() {
        List<RecommendedRelation> relations = new ArrayList<>();

        for (IRecommendedInstance instance : recommendationState.getRecommendedInstances()) {
            for (INounMapping mapping : instance.getNameMappings()) {
                for (IWord word : mapping.getWords()) {
                    List<IWord> verbsOn = this.getVerbsOn(word);
                    for (IWord verbOn : verbsOn) {
                        System.out.println("Got verb ON " + verbOn.getText() + " from " + word.getText() + " at " + word.getPosition());
                        for (IWord secondDep : this.getVerbDepOn(verbOn)) {
                            System.out.println(secondDep.getText());
                        }
                    }
                    List<IWord> verbsOf = this.getVerbsOf(word);
                    for (IWord verbOf : verbsOf) {
                        System.out.println("Got verb OF " + verbOf.getText() + " from " + word.getText() + " at " + word.getPosition());
                    }
                }
            }
        }

        return relations;
    }

    private List<IWord> getVerbDepOn(IWord word) {
        List<IWord> dependencies = word.getWordsThatAreDependencyOfThis(DependencyTag.AGENT);
        return dependencies;
    }

    private List<IWord> getVerbsOn(IWord word) {
        return this.getNounDepOn(word).stream().filter(IWord::isVerb).collect(Collectors.toList());
    }

    private List<IWord> getNounDepOn(IWord word) {
        List<IWord> dependencies = word.getWordsThatAreDependentOnThis(DependencyTag.OBJ);
        dependencies.addAll(word.getWordsThatAreDependentOnThis(DependencyTag.NSUBJ));
        return dependencies;
    }

    private List<IWord> getVerbsOf(IWord word) {

        return this.getNounDepOf(word).stream().filter(IWord::isVerb).collect(Collectors.toList());
    }

    private List<IWord> getNounDepOf(IWord word) {
        List<IWord> dependencies = word.getWordsThatAreDependencyOfThis(DependencyTag.OBJ);
        dependencies.addAll(word.getWordsThatAreDependencyOfThis(DependencyTag.NSUBJ));
        return dependencies;
    }

    private void doStuff() {

//        DependencyType dt = super.getDependencyType();
//        logger.info("HELLO Dependency " + dt.name() + " - DependencyExtractionAgent");
//        for (IWord word : text.getWords()) {
//            System.out.print(word.getText() + " ");
//        }
//        System.out.println();
//
//        for (DependencyExtractor extractor : extractors) {
//            for (IRecommendedInstance rec : recommendationState.getRecommendedInstances()) {
//                extractor.exec(rec);
//            }
//        }
        List<IWord> subs;
        List<IWord> obs;
        List<IWord> verbs;
        List<IWord> deps;
        for (IWord word : this.text.getWords()) {
            String words = "";
            for (DependencyTag tag : DependencyTag.values()) {
                deps = word.getWordsThatAreDependencyOfThis(tag);
                if (deps.size() > 0) {
                    words += tag.name() + " OF ";
                    for (IWord depOfWord : deps) {
                        words += depOfWord.getText() + " ";
                    }
                }
                deps = word.getWordsThatAreDependentOnThis(tag);
                if (deps.size() > 0) {
                    words += tag.name() + " ON ";
                    for (IWord depOnWord : deps) {
                        words += depOnWord.getText() + " ";
                    }
                }
            }
            System.out.println("Deps on word " + word.getText() + ":\n" + words);
//            if (word.isNoun()) {
//
//            }
//            if (word.isVerb()) {
//                verbs = getVerb(word);
//                //subs = getSubject(word);
//                //obs = getObject(word);
//                if (verbs.size() > 0) {
//                    System.out.println("Got relation");
//                    String out = word.getText() + " " + listToString(verbs);
//                    System.out.println(out);
//                }
//            }
        }
    }

    private List<IWord> getSubject(IWord verb) {
        List<IWord> subjects = verb.getWordsThatAreDependentOnThis(DependencyTag.NSUBJ);
        subjects.addAll(verb.getWordsThatAreDependencyOfThis(DependencyTag.NSUBJ));

        return subjects;
    }

    private List<IWord> getObject(IWord verb) {
        List<IWord> objects = verb.getWordsThatAreDependentOnThis(DependencyTag.OBJ);
        objects.addAll(verb.getWordsThatAreDependencyOfThis(DependencyTag.OBJ));

        return objects;
    }

    private List<IWord> getVerb(IWord noun) {
        return noun.getWordsThatAreDependentOnThis(DependencyTag.AGENT);
    }

    private String listToString(List<IWord> text) {
        StringBuilder str = new StringBuilder();
        for (IWord word : text) {
            str.append(word.getText()).append(" ");
        }
        return str.toString();
    }
}
