package edu.kit.kastel.mcse.ardoco.core.pipeline.helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.kit.kastel.mcse.ardoco.core.datastructures.NounMappingForEagle;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstanceLink;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRelationLink;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.MappingKind;

public class FilePrinter {

    private static final String GENERIC_ERROR = "An error occurred.";

    private static final String PROCESSING_TIME = " Processing Time in minutes: ";

    private static final String SUCCESS_WRITE = "Successfully wrote to the file.";

    private static final Logger logger = LogManager.getLogger(FilePrinter.class);

    private static final String SPACE = " ";
    private static final String SINGLE_SEPARATOR = "|";
    private static final String SINGLE_SEPARATOR_WITH_SPACES = " | ";
    private static final String FIVE_SEPARATORS = "| | | | |";
    private static final String THREE_SEPARATORS = "| | |";
    private static final String EIGHT_SEPARATORS = "| | | | | | | |";
    private static final String HORIZONTAL_RULE = "---------------------------------------------------------------------------------------------------------------------------------------------";
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private FilePrinter() {
        throw new IllegalAccessError();
    }

    /**
     * Writes the sentences as they are stored in the PARSE graph into a file.
     *
     * @param graph the graph to read from.
     */
    public static void writeSentencesInFile(File debugGraphSentences, IText graph) {

        boolean fileCreated = createFileIfNonExistent(debugGraphSentences);
        if (!fileCreated) {
            return;
        }

        try (FileWriter myWriter = new FileWriter(debugGraphSentences)) {

            int minSentenceNumber = 0;
            for (IWord node : graph.getWords()) {
                int sentenceNumber = Integer.parseInt(String.valueOf(node.getSentenceNo()));
                if (sentenceNumber + 1 > minSentenceNumber) {
                    myWriter.append(LINE_SEPARATOR + sentenceNumber + ": ");
                    minSentenceNumber++;
                }
                myWriter.append(SPACE + node.getText());
            }

            logger.info(SUCCESS_WRITE);
        } catch (IOException | NumberFormatException e) {
            logger.error("An error occurred while writing sentences to file.");
            logger.debug(e.getMessage(), e.getCause());
        }

    }

    private static boolean createFileIfNonExistent(File file) {
        try {
            if (file.createNewFile()) {
                logger.info("File created: " + file.getAbsolutePath());

            } else {
                logger.info("File already exists.");
            }
        } catch (IOException e) {
            logger.error("An error occured creating a file.");
            logger.debug(e.getMessage(), e.getCause());
            return false;
        }
        return true;
    }

    public static void writeEval1ToFile(File debugGraphSentences, IText graph, ITextState textExtractionState) {

        boolean fileCreated = createFileIfNonExistent(debugGraphSentences);
        if (!fileCreated) {
            return;
        }

        try (FileWriter myWriter = new FileWriter(debugGraphSentences)) {
            int minSentenceNumber = 0;
            StringBuilder valueBuilder = new StringBuilder(SINGLE_SEPARATOR_WITH_SPACES);

            for (IWord node : graph.getWords()) {
                int sentenceNo = Integer.parseInt(String.valueOf(node.getSentenceNo()));

                if (sentenceNo + 1 > minSentenceNumber) {
                    myWriter.append(LINE_SEPARATOR + valueBuilder.toString() + LINE_SEPARATOR);
                    myWriter.append(LINE_SEPARATOR + (sentenceNo + 1) + SINGLE_SEPARATOR_WITH_SPACES);
                    valueBuilder = new StringBuilder(SINGLE_SEPARATOR_WITH_SPACES);
                    minSentenceNumber++;
                }

                String nodeValue = node.getText();
                if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
                    continue;
                }
                myWriter.append(nodeValue + SINGLE_SEPARATOR_WITH_SPACES);
                if (textExtractionState.getNounMappingsByNode(node).isEmpty()) {
                    valueBuilder.append("0");
                    valueBuilder.append(SINGLE_SEPARATOR_WITH_SPACES);
                } else {
                    valueBuilder.append("1");
                    valueBuilder.append(SINGLE_SEPARATOR_WITH_SPACES);
                }
            }
            myWriter.append(LINE_SEPARATOR + valueBuilder.toString() + LINE_SEPARATOR);

            logger.info(SUCCESS_WRITE);
        } catch (IOException | NumberFormatException e) {
            logger.error(GENERIC_ERROR);
            logger.debug(e.getMessage(), e.getCause());
        }

    }

    public static void writeTraceLinksWithTextInFile(File debugGraphSentences, IText graph, IConnectionState connectionState) {
        boolean fileCreated = createFileIfNonExistent(debugGraphSentences);
        if (!fileCreated) {
            return;
        }

        try (FileWriter myWriter = new FileWriter(debugGraphSentences)) {
            int minSentenceNumber = 0;
            StringBuilder valueBuilder = new StringBuilder(SINGLE_SEPARATOR_WITH_SPACES);

            for (IWord node : graph.getWords()) {
                int sentenceNo = Integer.parseInt(String.valueOf(node.getSentenceNo()));

                if (sentenceNo + 1 > minSentenceNumber) {
                    myWriter.append(LINE_SEPARATOR + valueBuilder.toString() + LINE_SEPARATOR);
                    myWriter.append(LINE_SEPARATOR + (sentenceNo + 1) + SINGLE_SEPARATOR_WITH_SPACES);
                    valueBuilder = new StringBuilder(SINGLE_SEPARATOR_WITH_SPACES);
                    minSentenceNumber++;
                }

                String nodeValue = node.getText();
                if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
                    continue;
                }
                myWriter.append(nodeValue + SINGLE_SEPARATOR_WITH_SPACES);

                if (!connectionState.getInstanceLinks()
                        .stream()
                        .anyMatch(
                                link -> link.getTextualInstance().getNameMappings().stream().anyMatch(mapping -> mapping.getNodes().contains(node) == true))) {
                    valueBuilder.append("0");
                    valueBuilder.append(SINGLE_SEPARATOR_WITH_SPACES);
                } else {
                    valueBuilder.append("1");
                    valueBuilder.append(SINGLE_SEPARATOR_WITH_SPACES);
                }
            }
            myWriter.append(LINE_SEPARATOR + valueBuilder.toString() + LINE_SEPARATOR);

            logger.info(SUCCESS_WRITE);
        } catch (IOException | NumberFormatException e) {
            logger.error(GENERIC_ERROR);
            logger.debug(e.getMessage(), e.getCause());
        }
    }

    public static void writeRecommendedRelationToFile(IRecommendationState recommendationState) {
        File debugGraphSentences = new File("evaluations/EvalRecommendationRelations.txt");

        boolean fileCreated = createFileIfNonExistent(debugGraphSentences);
        if (!fileCreated) {
            return;
        }

        try (FileWriter myWriter = new FileWriter(debugGraphSentences)) {
            List<IRecommendedRelation> recommendedRelations = recommendationState.getRecommendedRelations();

            Comparator<IRecommendedRelation> comRecommendedRelationByName = //
                    getRecommendedRelationComparator();

            Collections.sort(recommendedRelations, comRecommendedRelationByName);

            myWriter.append("Recommended Instances | Sentences | Probability ");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);
            for (IRecommendedRelation ri : recommendedRelations) {

                StringBuilder recommendationStringBuilder = new StringBuilder();

                List<IRecommendedInstance> recommendedInstances = ri.getRelationInstances();

                for (IRecommendedInstance rins : recommendedInstances) {
                    recommendationStringBuilder.append(rins.getName());
                    recommendationStringBuilder.append(" : ");
                    recommendationStringBuilder.append(rins.getType());
                    recommendationStringBuilder.append(" , ");
                }
                recommendationStringBuilder.append(SINGLE_SEPARATOR_WITH_SPACES);

                List<IWord> positions = ri.getNodes();
                for (IWord n : positions) {
                    recommendationStringBuilder.append(String.valueOf(n.getSentenceNo()));
                    recommendationStringBuilder.append(1);
                    recommendationStringBuilder.append(", ");
                }

                recommendationStringBuilder.append(SINGLE_SEPARATOR_WITH_SPACES);
                double probability = ri.getProbability();
                recommendationStringBuilder.append(probability);
                recommendationStringBuilder.append(LINE_SEPARATOR);

                myWriter.append(recommendationStringBuilder.toString());

            }

            logger.info(SUCCESS_WRITE);
        } catch (IOException e) {
            logger.error(GENERIC_ERROR);
            logger.debug(e.getMessage(), e.getCause());
        }
    }

    public static void writeConnectionRelationsToFile(IConnectionState connectionState) {
        File debugGraphSentences = new File("evaluations/EvalRelationLinks.txt");

        boolean fileCreated = createFileIfNonExistent(debugGraphSentences);
        if (!fileCreated) {
            return;
        }

        try (FileWriter myWriter = new FileWriter(debugGraphSentences)) {
            List<IRelationLink> relationLinks = connectionState.getRelationLinks();

            myWriter.append("UID (Ansatz) | Model Instances | Textual Instances| Probability");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

            for (IRelationLink rl : relationLinks) {
                IRelation modelRelation = rl.getModelRelation();
                List<? extends IInstance> modelInstances = modelRelation.getInstances();
                StringBuilder modelInstanceNamesBuilder = new StringBuilder();
                for (IInstance i : modelInstances) {
                    modelInstanceNamesBuilder.append(i.getLongestName());
                    modelInstanceNamesBuilder.append(":");
                    modelInstanceNamesBuilder.append(i.getLongestType());
                    modelInstanceNamesBuilder.append(", ");
                }

                StringBuilder textualInstanceNamesBuilder = new StringBuilder();
                for (IRecommendedInstance ri : rl.getTextualRelation().getRelationInstances()) {
                    textualInstanceNamesBuilder.append(ri.getName());
                    textualInstanceNamesBuilder.append(":");
                    textualInstanceNamesBuilder.append(ri.getType());
                    textualInstanceNamesBuilder.append(", ");
                }

                String relationLinkString = //
                        modelRelation.getUid() + SINGLE_SEPARATOR_WITH_SPACES + //
                                modelInstanceNamesBuilder.toString() + SINGLE_SEPARATOR_WITH_SPACES + //
                                textualInstanceNamesBuilder.toString() + SINGLE_SEPARATOR_WITH_SPACES + //
                                rl.getProbability() + LINE_SEPARATOR;
                myWriter.append(relationLinkString);
            }

            logger.info(SUCCESS_WRITE);
        } catch (IOException e) {
            logger.error(GENERIC_ERROR);
            logger.debug(e.getMessage(), e.getCause());
        }
    }

    public static void writeConnectionsToFile(IConnectionState connectionState, double min) {
        File debugGraphSentences = new File("evaluations/EvalInstanceLinks.txt");

        boolean fileCreated = createFileIfNonExistent(debugGraphSentences);
        if (!fileCreated) {
            return;
        }

        try (FileWriter myWriter = new FileWriter(debugGraphSentences)) {

            List<IInstanceLink> instanceLinks = connectionState.getInstanceLinks();

            myWriter.append("UID (Ansatz) | Model Name | ModelType | Textual Name| Names|Textual Type| Types |Probability ");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

            for (IInstanceLink il : instanceLinks) {

                Set<String> names = new HashSet<>();
                List<Integer> namePositions = new ArrayList<>();
                Set<String> types = new HashSet<>();
                List<Integer> typePositions = new ArrayList<>();
                IRecommendedInstance textualInstance = il.getTextualInstance();
                IInstance modelInstance = il.getModelInstance();

                for (INounMapping nameMapping : textualInstance.getNameMappings()) {
                    names.addAll(nameMapping.getOccurrences());
                    namePositions.addAll(nameMapping.getMappingSentenceNo());
                }
                for (INounMapping typeMapping : textualInstance.getTypeMappings()) {
                    types.addAll(typeMapping.getOccurrences());
                    typePositions.addAll(typeMapping.getMappingSentenceNo());
                }

                String instanceLinkString = modelInstance.getUid() + SINGLE_SEPARATOR_WITH_SPACES + modelInstance.getLongestName() + //
                        SINGLE_SEPARATOR_WITH_SPACES + String.join(", ", modelInstance.getLongestType()) + SINGLE_SEPARATOR_WITH_SPACES
                        + textualInstance.getName() + //
                        SINGLE_SEPARATOR_WITH_SPACES + names.toString() + SINGLE_SEPARATOR_WITH_SPACES + textualInstance.getType()
                        + SINGLE_SEPARATOR_WITH_SPACES + types.toString() + SINGLE_SEPARATOR_WITH_SPACES + //
                        il.getProbability() + LINE_SEPARATOR;

                myWriter.append(instanceLinkString);
            }

            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);
            myWriter.append(PROCESSING_TIME + min);

            logger.info(SUCCESS_WRITE);
        } catch (IOException e) {
            logger.error(GENERIC_ERROR);
            logger.debug(e.getMessage(), e.getCause());
        }
    }

    public static void writeRecommendationsToFile(IRecommendationState recommendationState, double min) {

        File debugGraphSentences = new File("evaluations/EvalRecommendations.txt");

        boolean fileCreated = createFileIfNonExistent(debugGraphSentences);
        if (!fileCreated) {
            return;
        }

        try (FileWriter myWriter = new FileWriter(debugGraphSentences)) {
            List<IRecommendedInstance> recommendedInstances = recommendationState.getRecommendedInstances();

            Comparator<IRecommendedInstance> comRecommendedInstanceByName = //
                    getRecommendedInstancesComparator();
            Collections.sort(recommendedInstances, comRecommendedInstanceByName);

            myWriter.append("Name | Type | Probability | nameMappings| | | | | typeMappings| | | |");
            myWriter.append(LINE_SEPARATOR);
            myWriter.append("| | | reference | kind| occurrences | position| probability| reference | kind | occurrences | position| probability");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

            for (IRecommendedInstance ri : recommendedInstances) {

                String name = ri.getName();
                String type = ri.getType();
                double probability = ri.getProbability();

                List<INounMapping> nameMappings = ri.getNameMappings();
                List<INounMapping> typeMappings = ri.getTypeMappings();

                List<String> nameMappingStrings = createNameMappingStrings(nameMappings);

                List<String> typeMappingStrings = createTypeMappingStrings(typeMappings);

                String nameAndTypeMappingInfoString = createNameAndTypeMappingInfoString(name, type, probability, nameMappingStrings, typeMappingStrings);

                myWriter.append(nameAndTypeMappingInfoString);

            }

            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);
            myWriter.append(PROCESSING_TIME + min);

            logger.info(SUCCESS_WRITE);
        } catch (IOException e) {
            logger.error(GENERIC_ERROR);
            logger.debug(e.getMessage(), e.getCause());
        }

    }

    private static StringBuilder createRecommendationStringBuilder(String name, String type, double probability) {
        StringBuilder recommendationStringBuilder = new StringBuilder();
        recommendationStringBuilder.append(name);
        recommendationStringBuilder.append(SINGLE_SEPARATOR_WITH_SPACES);
        recommendationStringBuilder.append(type);
        recommendationStringBuilder.append(SINGLE_SEPARATOR_WITH_SPACES);
        recommendationStringBuilder.append(probability);
        recommendationStringBuilder.append(SPACE);
        recommendationStringBuilder.append(SINGLE_SEPARATOR);
        return recommendationStringBuilder;
    }

    private static String createNameAndTypeMappingInfoString(String name, String type, double probability, List<String> nameMappingStrings,
            List<String> typeMappingStrings) {

        StringBuilder recommendationStringBuilder = createRecommendationStringBuilder(name, type, probability);
        if (!nameMappingStrings.isEmpty()) {
            recommendationStringBuilder.append(nameMappingStrings.get(0));
            nameMappingStrings.remove(0);
        } else {
            recommendationStringBuilder.append(FIVE_SEPARATORS);
        }

        if (!typeMappingStrings.isEmpty()) {
            recommendationStringBuilder.append(typeMappingStrings.get(0));
            typeMappingStrings.remove(0);
        } else {
            recommendationStringBuilder.append(LINE_SEPARATOR);
        }

        for (int i = 0; i < nameMappingStrings.size() && i < typeMappingStrings.size(); i++) {

            recommendationStringBuilder.append(THREE_SEPARATORS);
            recommendationStringBuilder.append(nameMappingStrings.get(i));
            recommendationStringBuilder.append(typeMappingStrings.get(i));
        }

        int nameAmount = nameMappingStrings.size();
        int typeAmount = typeMappingStrings.size();

        if (nameAmount > 0 && typeAmount > nameAmount) {
            for (int i = nameAmount - 1; i < typeAmount; i++) {
                recommendationStringBuilder.append(EIGHT_SEPARATORS);
                recommendationStringBuilder.append(typeMappingStrings.get(i));
            }
        } else if (typeAmount > 0 && nameAmount > typeAmount) {
            for (int i = typeAmount - 1; i < nameAmount; i++) {
                recommendationStringBuilder.append(THREE_SEPARATORS);
                recommendationStringBuilder.append(SPACE);
                recommendationStringBuilder.append(nameMappingStrings.get(i));
                recommendationStringBuilder.append(LINE_SEPARATOR);
            }
        }
        return recommendationStringBuilder.toString();
    }

    private static List<String> createTypeMappingStrings(List<INounMapping> typeMappings) {
        List<String> typeMappingStrings = new ArrayList<>();
        for (INounMapping typeMapping : typeMappings) {

            String typeString = typeMapping.getReference() + SINGLE_SEPARATOR_WITH_SPACES + //
                    typeMapping.getKind() + SINGLE_SEPARATOR_WITH_SPACES + //
                    String.join(", ", typeMapping.getOccurrences()) + SINGLE_SEPARATOR_WITH_SPACES + //
                    typeMapping.getMappingSentenceNo() + //
                    SINGLE_SEPARATOR_WITH_SPACES + typeMapping.getProbability() + LINE_SEPARATOR;
            typeMappingStrings.add(typeString);
        }
        return typeMappingStrings;
    }

    private static List<String> createNameMappingStrings(List<INounMapping> nameMappings) {
        List<String> nameMappingStrings = new ArrayList<>();
        for (INounMapping nameMapping : nameMappings) {

            String nameString = nameMapping.getReference() + SINGLE_SEPARATOR_WITH_SPACES + //
                    nameMapping.getKind() + SINGLE_SEPARATOR_WITH_SPACES + //
                    String.join(", ", nameMapping.getOccurrences()) + SINGLE_SEPARATOR_WITH_SPACES + //
                    nameMapping.getMappingSentenceNo() + //
                    SINGLE_SEPARATOR_WITH_SPACES + nameMapping.getProbability() + SINGLE_SEPARATOR;
            nameMappingStrings.add(nameString);
        }
        return nameMappingStrings;
    }

    /***
     * Writes the states into a string
     *
     * @param extractionState     the extraction state, containing the extracted elements of the model
     * @param ntrState            the name type relation state, containing the mappings found in the text, sorted in
     *                            name, type or name_or_type
     * @param recommendationState the supposing state, containing the supposing mappings for instances, as well as
     *                            relations
     * @param connectionState     containing all instances and relations, matched by supposed mappings
     * @param duration            past time the approach needed to calculate the results
     */
    public static String getPrettyInformation(IModelState extractionState, ITextState ntrState, //
            IRecommendationState recommendationState, IConnectionState connectionState, Duration duration) {

        try (StringWriter sw = new StringWriter()) {
            writeStates(sw, extractionState, ntrState, recommendationState, connectionState, duration);
            return sw.toString();

        } catch (IOException e) {
            logger.error(GENERIC_ERROR);
            logger.debug(e.getMessage(), e.getCause());
        }

        return null;
    }

    private static void writeStates(Writer myWriter, IModelState extractionState, ITextState ntrState, //
            IRecommendationState recommendationState, IConnectionState connectionState, Duration duration) throws IOException {
        myWriter.write("Results of ModelConnector: ");
        myWriter.append(LINE_SEPARATOR);
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("ExtractorState: ");
        myWriter.append(LINE_SEPARATOR);
        myWriter.write(extractionState.toString());
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("FoundNames as Set: ");
        myWriter.append(LINE_SEPARATOR);
        List<String> nameList = ntrState.getNameList();
        Collections.sort(nameList);
        myWriter.write(nameList.toString() + LINE_SEPARATOR);
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("FoundNameTerms as Set: ");
        myWriter.append(LINE_SEPARATOR);
        List<String> nameTermList = ntrState.getNameTermList();
        Collections.sort(nameTermList);
        myWriter.write(nameTermList.toString() + LINE_SEPARATOR);
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("FoundNORTs as Set: ");
        myWriter.append(LINE_SEPARATOR);
        List<String> nortList = ntrState.getNortList();
        Collections.sort(nortList);
        myWriter.write(nortList.toString() + LINE_SEPARATOR);
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("FoundTypes as Set: ");
        myWriter.append(LINE_SEPARATOR);
        List<String> typeList = ntrState.getTypeList();
        Collections.sort(typeList);
        myWriter.write(typeList.toString() + LINE_SEPARATOR);
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("FoundTypeTerms as Set: ");
        myWriter.append(LINE_SEPARATOR);
        List<String> typeTermList = ntrState.getTypeTermList();
        Collections.sort(typeTermList);
        myWriter.write(typeTermList.toString() + LINE_SEPARATOR);
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("Instances of the Recommendation State: ");
        myWriter.append(LINE_SEPARATOR);

        List<IRecommendedInstance> recommendedInstances = recommendationState.getRecommendedInstances();
        Comparator<IRecommendedInstance> comRecommendedInstanceByName = getRecommendedInstancesComparator();
        Collections.sort(recommendedInstances, comRecommendedInstanceByName);

        for (IRecommendedInstance ri : recommendedInstances) {
            myWriter.write(ri.toString() + LINE_SEPARATOR);
        }
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("Instances of the Connection State: ");
        myWriter.append(LINE_SEPARATOR);

        Comparator<IInstanceLink> compInstByUID = getInstanceLinkComparator();
        List<IInstanceLink> instanceMappings = new ArrayList<>(connectionState.getInstanceLinks());
        Collections.sort(instanceMappings, compInstByUID);

        for (IInstanceLink imap : instanceMappings) {

            myWriter.write(imap.toString() + LINE_SEPARATOR);
        }

        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("Relations of the Recommendation State: ");
        myWriter.append(LINE_SEPARATOR);

        List<IRecommendedRelation> rels = recommendationState.getRecommendedRelations();
        Comparator<IRecommendedRelation> compRRelationsByFirstInstanceName = getRecommendedRelationComparator();
        Collections.sort(rels, compRRelationsByFirstInstanceName);

        for (IRecommendedRelation si : rels) {
            myWriter.write(si.toString() + LINE_SEPARATOR);
        }

        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("Relations of the Connection State: ");
        myWriter.append(LINE_SEPARATOR);

        Comparator<IRelationLink> compRelByUID = getRelationLinkComparator();
        List<IRelationLink> relationLinks = new ArrayList<>(connectionState.getRelationLinks());
        Collections.sort(relationLinks, compRelByUID);

        for (IRelationLink rlink : relationLinks) {
            myWriter.write(rlink.toString() + LINE_SEPARATOR);
        }

        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);
        myWriter.write("ExecutionTime: " + duration);
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        logger.info(SUCCESS_WRITE);
    }

    /***
     * Writes the states into a file
     *
     * @param resultFile          the result file
     * @param extractionState     the extraction state, containing the extracted elements of the model
     * @param ntrState            the name type relation state, containing the mappings found in the text, sorted in
     *                            name, type or name_or_type
     * @param recommendationState the supposing state, containing the supposing mappings for instances, as well as
     *                            relations
     * @param connectionState     containing all instances and relations, matched by supposed mappings
     * @param duration            past time the approach needed to calculate the results
     */
    public static void writeStatesToFile(File resultFile, IModelState extractionState, ITextState ntrState, //
            IRecommendationState recommendationState, IConnectionState connectionState, Duration duration) {
        boolean fileCreated = createFileIfNonExistent(resultFile);
        if (!fileCreated) {
            return;
        }

        try (FileWriter myWriter = new FileWriter(resultFile)) {
            writeStates(myWriter, extractionState, ntrState, recommendationState, connectionState, duration);

        } catch (IOException e) {
            logger.error(GENERIC_ERROR);
            logger.debug(e.getMessage(), e.getCause());
        }

    }

    public static void writeModelInstancesInCsvFile(File destination, IModelState modelState, String name) {
        List<String[]> dataLines = getInstancesFromModelState(modelState, name);
        writeDataLinesInFile(destination, dataLines);
    }

    private static List<String[]> getInstancesFromModelState(IModelState modelState, String name) {
        List<String[]> dataLines = new ArrayList<>();

        dataLines.add(new String[] { "Found Model Elements in " + name + ":", "", "" });
        dataLines.add(new String[] { "" });
        dataLines.add(new String[] { "UID", "Name", "Type" });

        for (IInstance instance : modelState.getInstances()) {

            dataLines.add(new String[] { instance.getUid(), instance.getLongestName(), instance.getLongestType() });

        }

        return dataLines;
    }

    /**
     * https://www.baeldung.com/java-csv
     *
     * @param connectionState
     * @param duration
     * @throws FileNotFoundException
     */
    public static void writeTraceLinksInCsvFile(File resultFile, IConnectionState connectionState) {
        List<String[]> dataLines = getLinksAsDataLinesOfConnectionState(connectionState);
        writeDataLinesInFile(resultFile, dataLines);
    }

    public static void writeNounMappingsInCsvFile(File resultFile, ITextState textState) {
        List<String[]> dataLines = getMappingsAsDataLinesOfTextState(textState);
        writeDataLinesInFile(resultFile, dataLines);
    }

    private static List<String[]> getMappingsAsDataLinesOfTextState(ITextState textState) {
        List<String[]> dataLines = new ArrayList<>();

        dataLines.add(new String[] { "Found NounMappings: ", "", "", "" });
        dataLines.add(new String[] { "" });
        dataLines.add(new String[] { "Reference", "Name", "Type", "NameOrType" });

        if (textState.getAllMappings().isEmpty() || !(textState.getAllMappings().get(0) instanceof NounMappingForEagle)) {
            for (INounMapping mapping : textState.getAllMappings()) {

                MappingKind kind = mapping.getKind();

                String nameProb = Double.toString(kind == MappingKind.NAME ? mapping.getProbability() : 0);
                String typeProb = Double.toString(kind == MappingKind.TYPE ? mapping.getProbability() : 0);
                String nortProb = Double.toString(kind == MappingKind.NAME_OR_TYPE ? mapping.getProbability() : 0);

                dataLines.add(new String[] { mapping.getReference(), nameProb, typeProb, nortProb });

            }
            return dataLines;
        }

        for (INounMapping mapping : textState.getAllMappings()) {

            NounMappingForEagle eagleMapping = (NounMappingForEagle) mapping;
            Map<MappingKind, Double> distribution = eagleMapping.getDistribution();
            String nameProb = Double.toString(distribution.get(MappingKind.NAME));
            String typeProb = Double.toString(distribution.get(MappingKind.TYPE));
            String nortProb = Double.toString(distribution.get(MappingKind.NAME_OR_TYPE));

            dataLines.add(new String[] { eagleMapping.getReference(), nameProb, typeProb, nortProb });

        }
        return dataLines;
    }

    private static List<String[]> getLinksAsDataLinesOfConnectionState(IConnectionState connectionState) {
        List<String[]> dataLines = new ArrayList<>();

        dataLines.add(new String[] { "#Found TraceLinks: ", "", "" });
        dataLines.add(new String[] { "" });
        dataLines.add(new String[] { "modelElementID", "sentence", "confidence" });

        Set<String> modelElementUids = connectionState.getInstanceLinks()
                .stream()
                .map(instanceLink -> instanceLink.getModelInstance().getUid())
                .collect(Collectors.toSet());
        for (String modelElementUid : modelElementUids) {
            List<IInstanceLink> instanceLinksForUid = connectionState.getInstanceLinks()
                    .stream()
                    .filter(instanceLink -> instanceLink.getModelInstance().getUid().equals(modelElementUid))
                    .collect(Collectors.toList());

            IInstanceLink instanceLinkWithBestConfidence = instanceLinksForUid.get(0);
            for (IInstanceLink instanceLink : instanceLinksForUid) {
                if (instanceLink.getProbability() > instanceLinkWithBestConfidence.getProbability()) {
                    instanceLinkWithBestConfidence = instanceLink;
                }
            }

            String probability = Double.toString(instanceLinkWithBestConfidence.getProbability());

            for (INounMapping nameMapping : instanceLinkWithBestConfidence.getTextualInstance().getNameMappings()) {
                for (IWord word : nameMapping.getNodes()) {
                    dataLines.add(new String[] { modelElementUid, Integer.toString(word.getSentenceNo() + 1), probability });
                }
            }
        }
        return dataLines;
    }

    public static void writeDataLinesInFile(File file, List<String[]> dataLines) {

        try (FileWriter pw = new FileWriter(file)) {
            dataLines.stream().map(FilePrinter::convertToCSV).forEach(s -> {
                try {
                    pw.append(s).append("\n");
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            });
        } catch (IOException e) {
            logger.error(GENERIC_ERROR);
            logger.debug(e.getMessage(), e);
        }

    }

    /**
     * https://www.baeldung.com/java-csv
     *
     * @param data
     * @return
     */
    private static String convertToCSV(String[] data) {
        return Stream.of(data).map(FilePrinter::escapeSpecialCharacters).collect(Collectors.joining(","));
    }

    /**
     * https://www.baeldung.com/java-csv
     *
     * @param data
     * @return
     */
    private static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    private static Comparator<IRecommendedInstance> getRecommendedInstancesComparator() {
        return (ri1, ri2) -> ri1.getName().compareTo(ri2.getName());
    }

    private static Comparator<IInstanceLink> getInstanceLinkComparator() {
        return (i1, i2) -> i1.getModelInstance().getUid().compareTo(i2.getModelInstance().getUid());
    }

    private static Comparator<IRecommendedRelation> getRecommendedRelationComparator() {
        return (rl1, rl2) -> rl1.getRelationInstances().get(0).getName().compareTo(rl2.getRelationInstances().get(0).getName());
    }

    private static Comparator<IRelationLink> getRelationLinkComparator() {
        return (i1, i2) -> i1.getModelRelation().getUid().compareTo(i2.getModelRelation().getUid());
    }

}
