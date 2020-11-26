package modelconnector.helpers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.connectionGenerator.state.ConnectionState;
import modelconnector.connectionGenerator.state.InstanceLink;
import modelconnector.connectionGenerator.state.RelationLink;
import modelconnector.modelExtractor.state.Instance;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.modelExtractor.state.Relation;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.recommendationGenerator.state.RecommendedInstance;
import modelconnector.recommendationGenerator.state.RecommendedRelation;
import modelconnector.textExtractor.state.NounMapping;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * Class encapsulating all writing/ logging methods, like writing the results, or the read in sentences in a file
 *
 * @author Sophie
 *
 */
public final class FilesWriter {

    private static final String SPACE_STRING = " ";
    private static final String EMPTY_STRING = "";
    private static final String SINGLE_SEPARATOR = "|";
    private static final String SINGLE_SEPARATOR_WITH_SPACES = " | ";
    private static final String FIVE_SEPARATORS = "| | | | |";
    private static final String THREE_SEPARATORS = "| | |";
    private static final String EIGHT_SEPARATORS = "| | | | | | | |";
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private FilesWriter() {
        throw new IllegalAccessError();
    }

    /**
     * Writes the sentences as they are stored in the PARSE graph into a file.
     *
     * @param graph
     *            the graph to read from.
     */
    public static void writeSentencesInFile(IGraph graph) {

        File debugGraphSentences = new File(ModelConnectorConfiguration.fileForInput_Path);

        boolean fileCreated = createFileIfNonExistent(debugGraphSentences);
        if (!fileCreated) {
            return;
        }

        try (FileWriter myWriter = new FileWriter(debugGraphSentences)) {

            int minSentenceNumber = 0;
            for (INode node : graph.getNodes()) {
                int sentenceNumber = Integer.valueOf(node.getAttributeValue("sentenceNumber")
                                                         .toString());
                if (sentenceNumber + 1 > minSentenceNumber) {
                    myWriter.append(LINE_SEPARATOR + sentenceNumber + ": ");
                    minSentenceNumber++;
                }
                myWriter.append(SPACE_STRING + GraphUtils.getNodeValue(node));
            }

            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    private static boolean createFileIfNonExistent(File file) {
        try {
            if (file.createNewFile()) {
                System.out.println("File created: " + file.getName());

            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void writeEval1ToFile(IGraph graph, TextExtractionState textExtractionState, double min) {

        File debugGraphSentences = new File("evaluations/EvalA1.txt");

        boolean fileCreated = createFileIfNonExistent(debugGraphSentences);
        if (!fileCreated) {
            return;
        }

        try (FileWriter myWriter = new FileWriter(debugGraphSentences)) {
            int minSentenceNumber = 0;
            String value = SINGLE_SEPARATOR_WITH_SPACES;

            for (INode node : graph.getNodes()) {
                int sentenceNo = Integer.valueOf(node.getAttributeValue("sentenceNumber")
                                                     .toString());

                if (sentenceNo + 1 > minSentenceNumber) {
                    myWriter.append(LINE_SEPARATOR + value + LINE_SEPARATOR);
                    myWriter.append(LINE_SEPARATOR + (sentenceNo + 1) + SINGLE_SEPARATOR_WITH_SPACES);
                    value = SINGLE_SEPARATOR_WITH_SPACES;
                    minSentenceNumber++;
                }

                String nodeValue = GraphUtils.getNodeValue(node);
                if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
                    continue;
                }
                myWriter.append(nodeValue + SINGLE_SEPARATOR_WITH_SPACES);
                if (textExtractionState.getNounMappingsByNode(node)
                                       .isEmpty()) {
                    value += "0" + SINGLE_SEPARATOR_WITH_SPACES;
                } else {
                    value += "1" + SINGLE_SEPARATOR_WITH_SPACES;
                }
            }
            myWriter.append(LINE_SEPARATOR + value + LINE_SEPARATOR);

            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);
            myWriter.append(" Processing Time in minutes: " + min);

            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    public static void writeRecommendedRelationToFile(RecommendationState recommendationState) {
        File debugGraphSentences = new File("evaluations/EvalRecommendationRelations.txt");

        boolean fileCreated = createFileIfNonExistent(debugGraphSentences);
        if (!fileCreated) {
            return;
        }

        try (FileWriter myWriter = new FileWriter(debugGraphSentences)) {
            List<RecommendedRelation> recommendedRelations = recommendationState.getRecommendedRelations();

            Comparator<RecommendedRelation> comRecommendedRelationByName = //
                    getRecommendedRelationComparator();

            Collections.sort(recommendedRelations, comRecommendedRelationByName);

            myWriter.append("Recommended Instances | Sentences | Probability ");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);
            for (RecommendedRelation ri : recommendedRelations) {

                String recommendationString = EMPTY_STRING;

                List<RecommendedInstance> recommendedInstances = ri.getRelationInstances();

                for (RecommendedInstance rins : recommendedInstances) {
                    recommendationString += rins.getName() + " : " + rins.getType() + " , ";
                }
                recommendationString += SINGLE_SEPARATOR_WITH_SPACES;

                List<INode> positions = ri.getNodes();
                for (INode n : positions) {
                    recommendationString += Integer.valueOf(n.getAttributeValue("sentenceNumber")
                                                             .toString())
                            + 1 + ", ";
                }

                recommendationString += SINGLE_SEPARATOR_WITH_SPACES;
                double probability = ri.getProbability();
                recommendationString += probability + LINE_SEPARATOR;

                myWriter.append(recommendationString);

            }

            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void writeConnectionRelationsToFile(ConnectionState connectionState) {
        File debugGraphSentences = new File("evaluations/EvalRelationLinks.txt");

        boolean fileCreated = createFileIfNonExistent(debugGraphSentences);
        if (!fileCreated) {
            return;
        }

        try (FileWriter myWriter = new FileWriter(debugGraphSentences)) {
            List<RelationLink> relationLinks = connectionState.getRelationLinks();

            myWriter.append("UID (Ansatz) | Model Instances | Textual Instances| Probability");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

            for (RelationLink rl : relationLinks) {
                Relation modelRelation = rl.getModelRelation();
                List<Instance> modelInstances = modelRelation.getInstances();
                String modelInstanceNames = EMPTY_STRING;
                for (Instance i : modelInstances) {
                    modelInstanceNames += i.getLongestName() + ":" + i.getLongestType() + ", ";
                }

                String textualInstanceNames = EMPTY_STRING;
                for (RecommendedInstance ri : rl.getTextualRelation()
                                                .getRelationInstances()) {
                    textualInstanceNames += ri.getName() + ":" + ri.getType() + ", ";
                }

                String relationLinkString = //
                        modelRelation.getUid() + SINGLE_SEPARATOR_WITH_SPACES + //
                                modelInstanceNames + SINGLE_SEPARATOR_WITH_SPACES + //
                                textualInstanceNames + SINGLE_SEPARATOR_WITH_SPACES + //
                                rl.getProbability() + LINE_SEPARATOR;
                myWriter.append(relationLinkString);
            }

            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void writeConnectionsToFile(ConnectionState connectionState, double min) {
        File debugGraphSentences = new File("evaluations/EvalInstanceLinks.txt");

        boolean fileCreated = createFileIfNonExistent(debugGraphSentences);
        if (!fileCreated) {
            return;
        }

        try (FileWriter myWriter = new FileWriter(debugGraphSentences)) {

            List<InstanceLink> instanceLinks = connectionState.getInstanceLinks();

            myWriter.append(
                    "UID (Ansatz) | Model Name | ModelType | Textual Name| Names|Textual Type| Types |Probability ");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

            for (InstanceLink il : instanceLinks) {

                Set<String> names = new HashSet<>();
                List<Integer> namePositions = new ArrayList<>();
                Set<String> types = new HashSet<>();
                List<Integer> typePositions = new ArrayList<>();
                RecommendedInstance textualInstance = il.getTextualInstance();
                Instance modelInstance = il.getModelInstance();

                for (NounMapping nameMapping : textualInstance.getNameMappings()) {
                    names.addAll(nameMapping.getOccurrences());
                    namePositions.addAll(nameMapping.getMappingSentenceNo());
                }
                for (NounMapping typeMapping : textualInstance.getTypeMappings()) {
                    types.addAll(typeMapping.getOccurrences());
                    typePositions.addAll(typeMapping.getMappingSentenceNo());
                }

                String instanceLinkString = modelInstance.getUid() + SINGLE_SEPARATOR_WITH_SPACES
                        + modelInstance.getLongestName() + //
                        SINGLE_SEPARATOR_WITH_SPACES + String.join(", ", modelInstance.getLongestType())
                        + SINGLE_SEPARATOR_WITH_SPACES + textualInstance.getName() + //
                        SINGLE_SEPARATOR_WITH_SPACES + names.toString() + SINGLE_SEPARATOR_WITH_SPACES
                        + textualInstance.getType() + SINGLE_SEPARATOR_WITH_SPACES + types.toString()
                        + SINGLE_SEPARATOR_WITH_SPACES + //
                        il.getProbability() + LINE_SEPARATOR;

                myWriter.append(instanceLinkString);
            }

            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);
            myWriter.append(" Processing Time in minutes: " + min);

            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void writeRecommendationsToFile(RecommendationState recommendationState, double min) {

        File debugGraphSentences = new File("evaluations/EvalRecommendations.txt");

        boolean fileCreated = createFileIfNonExistent(debugGraphSentences);
        if (!fileCreated) {
            return;
        }

        try (FileWriter myWriter = new FileWriter(debugGraphSentences)) {
            List<RecommendedInstance> recommendedInstances = recommendationState.getRecommendedInstances();

            Comparator<RecommendedInstance> comRecommendedInstanceByName = //
                    getRecommendedInstancesComparator();
            Collections.sort(recommendedInstances, comRecommendedInstanceByName);

            myWriter.append("Name | Type | Probability | nameMappings| | | | | typeMappings| | | |");
            myWriter.append(LINE_SEPARATOR);
            myWriter.append(
                    "| | | reference | kind| occurrences | position| probability| reference | kind | occurrences | position| probability");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

            for (RecommendedInstance ri : recommendedInstances) {

                String name = ri.getName();
                String type = ri.getType();
                double probability = ri.getProbability();

                List<NounMapping> nameMappings = ri.getNameMappings();
                List<NounMapping> typeMappings = ri.getTypeMappings();

                List<String> typeMappingStrings = new ArrayList<>();
                List<String> nameMappingStrings = new ArrayList<>();

                for (NounMapping nameMapping : nameMappings) {

                    String nameString = EMPTY_STRING + //
                            nameMapping.getReference() + SINGLE_SEPARATOR_WITH_SPACES + //
                            nameMapping.getKind() + SINGLE_SEPARATOR_WITH_SPACES + //
                            String.join(", ", nameMapping.getOccurrences()) + SINGLE_SEPARATOR_WITH_SPACES + //
                            nameMapping.getMappingSentenceNo() + //
                            SINGLE_SEPARATOR_WITH_SPACES + nameMapping.getProbability() + SINGLE_SEPARATOR;
                    nameMappingStrings.add(nameString);
                }

                for (NounMapping typeMapping : typeMappings) {

                    String typeString = EMPTY_STRING + //
                            typeMapping.getReference() + SINGLE_SEPARATOR_WITH_SPACES + //
                            typeMapping.getKind() + SINGLE_SEPARATOR_WITH_SPACES + //
                            String.join(", ", typeMapping.getOccurrences()) + SINGLE_SEPARATOR_WITH_SPACES + //
                            typeMapping.getMappingSentenceNo() + //
                            SINGLE_SEPARATOR_WITH_SPACES + typeMapping.getProbability() + LINE_SEPARATOR;
                    typeMappingStrings.add(typeString);
                }

                String recommendationString = EMPTY_STRING + name + SINGLE_SEPARATOR_WITH_SPACES + type
                        + SINGLE_SEPARATOR_WITH_SPACES + probability + SPACE_STRING + SINGLE_SEPARATOR;

                if (nameMappingStrings.size() >= 1) {
                    recommendationString += nameMappingStrings.get(0);

                    nameMappingStrings.remove(0);
                } else {
                    recommendationString += FIVE_SEPARATORS;
                }

                if (typeMappingStrings.size() >= 1) {
                    recommendationString += typeMappingStrings.get(0);

                    typeMappingStrings.remove(0);
                } else {
                    recommendationString += LINE_SEPARATOR;
                }

                for (int i = 0; i < nameMappingStrings.size() && i < typeMappingStrings.size(); i++) {

                    recommendationString += THREE_SEPARATORS + nameMappingStrings.get(i) + typeMappingStrings.get(i);
                }

                int nameAmount = nameMappingStrings.size();
                int typeAmount = typeMappingStrings.size();

                if (nameAmount > 0 && typeAmount > nameAmount) {
                    for (int i = nameAmount - 1; i < typeAmount; i++) {
                        recommendationString += EIGHT_SEPARATORS + typeMappingStrings.get(i);
                    }
                } else if (typeAmount > 0 && nameAmount > typeAmount) {
                    for (int i = typeAmount - 1; i < nameAmount; i++) {
                        recommendationString += THREE_SEPARATORS + SPACE_STRING + nameMappingStrings.get(i) + LINE_SEPARATOR;
                    }
                }

                myWriter.append(recommendationString);

            }

            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);
            myWriter.append(" Processing Time in minutes: " + min);

            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    /***
     * Writes the states into a file
     *
     * @param extractionState
     *            the extraction state, containing the extracted elements of the model
     * @param ntrState
     *            the name type relation state, containing the mappings found in the text, sorted in name, type or
     *            name_or_type
     * @param recommendationState
     *            the supposing state, containing the supposing mappings for instances, as well as relations
     * @param connectionState
     *            containing all instances and relations, matched by supposed mappings
     * @param durationInMinutes
     *            past time in minutes the approach needed to calculate the results
     */
    public static void writeStatesToFile(ModelExtractionState extractionState, TextExtractionState ntrState, //
            RecommendationState recommendationState, ConnectionState connectionState, double durationInMinutes) {
        // TODO: clean this up and try to put parts in helping methods

        File resultFile = new File(ModelConnectorConfiguration.fileForResults_Path);

        boolean fileCreated = createFileIfNonExistent(resultFile);
        if (!fileCreated) {
            return;
        }

        try (FileWriter myWriter = new FileWriter(resultFile)) {

            myWriter.write("Results of ModelConnector: ");
            myWriter.append(LINE_SEPARATOR);
            myWriter.write(
                    "---------------------------------------------------------------------------------------------------------------------------------------------");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

            myWriter.write("ExtractorState: ");
            myWriter.append(LINE_SEPARATOR);
            myWriter.write(extractionState.toString());
            myWriter.write(
                    "---------------------------------------------------------------------------------------------------------------------------------------------");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

            myWriter.write("FoundNames as Set: ");
            myWriter.append(LINE_SEPARATOR);
            List<String> nameList = ntrState.getNameList();
            Collections.sort(nameList);
            myWriter.write(nameList.toString() + LINE_SEPARATOR);
            myWriter.write(
                    "---------------------------------------------------------------------------------------------------------------------------------------------");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

            myWriter.write("FoundNameTerms as Set: ");
            myWriter.append(LINE_SEPARATOR);
            List<String> nameTermList = ntrState.getNameTermList();
            Collections.sort(nameTermList);
            myWriter.write(nameTermList.toString() + LINE_SEPARATOR);
            myWriter.write(
                    "---------------------------------------------------------------------------------------------------------------------------------------------");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

            myWriter.write("FoundNORTs as Set: ");
            myWriter.append(LINE_SEPARATOR);
            List<String> nortList = ntrState.getNortList();
            Collections.sort(nortList);
            myWriter.write(nortList.toString() + LINE_SEPARATOR);
            myWriter.write(
                    "---------------------------------------------------------------------------------------------------------------------------------------------");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

            myWriter.write("FoundTypes as Set: ");
            myWriter.append(LINE_SEPARATOR);
            List<String> typeList = ntrState.getTypeList();
            Collections.sort(typeList);
            myWriter.write(typeList.toString() + LINE_SEPARATOR);
            myWriter.write(
                    "---------------------------------------------------------------------------------------------------------------------------------------------");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

            myWriter.write("FoundTypeTerms as Set: ");
            myWriter.append(LINE_SEPARATOR);
            List<String> typeTermList = ntrState.getTypeTermList();
            Collections.sort(typeTermList);
            myWriter.write(typeTermList.toString() + LINE_SEPARATOR);
            myWriter.write(
                    "---------------------------------------------------------------------------------------------------------------------------------------------");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

            myWriter.write("Instances of the Recommendation State: ");
            myWriter.append(LINE_SEPARATOR);

            List<RecommendedInstance> recommendedInstances = recommendationState.getRecommendedInstances();
            Comparator<RecommendedInstance> comRecommendedInstanceByName = getRecommendedInstancesComparator();
            Collections.sort(recommendedInstances, comRecommendedInstanceByName);

            for (RecommendedInstance ri : recommendedInstances) {
                myWriter.write(ri.toString() + LINE_SEPARATOR);
            }
            myWriter.write(
                    "---------------------------------------------------------------------------------------------------------------------------------------------");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

            myWriter.write("Instances of the Connection State: ");
            myWriter.append(LINE_SEPARATOR);

            Comparator<InstanceLink> compInstByUID = getInstanceLinkComparator();
            List<InstanceLink> instanceMappings = new ArrayList<>(connectionState.getInstanceLinks());
            Collections.sort(instanceMappings, compInstByUID);

            for (InstanceLink imap : instanceMappings) {

                myWriter.write(imap.toString() + LINE_SEPARATOR);
            }

            myWriter.write(
                    "---------------------------------------------------------------------------------------------------------------------------------------------");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

            myWriter.write("Relations of the Recommendation State: ");
            myWriter.append(LINE_SEPARATOR);

            List<RecommendedRelation> rels = recommendationState.getRecommendedRelations();
            Comparator<RecommendedRelation> compRRelationsByFirstInstanceName = getRecommendedRelationComparator();
            Collections.sort(rels, compRRelationsByFirstInstanceName);

            for (RecommendedRelation si : rels) {
                myWriter.write(si.toString() + LINE_SEPARATOR);
            }

            myWriter.write(
                    "---------------------------------------------------------------------------------------------------------------------------------------------");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

            myWriter.write("Relations of the Connection State: ");
            myWriter.append(LINE_SEPARATOR);

            Comparator<RelationLink> compRelByUID = getRelationLinkComparator();
            List<RelationLink> relationLinks = new ArrayList<>(connectionState.getRelationLinks());
            Collections.sort(relationLinks, compRelByUID);

            for (RelationLink rlink : relationLinks) {
                myWriter.write(rlink.toString() + LINE_SEPARATOR);
            }

            myWriter.write(
                    "---------------------------------------------------------------------------------------------------------------------------------------------");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);
            myWriter.write(
                    "---------------------------------------------------------------------------------------------------------------------------------------------");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);
            myWriter.write("ExecutionTime in minutes: " + durationInMinutes);
            myWriter.write(
                    "---------------------------------------------------------------------------------------------------------------------------------------------");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);
            myWriter.write(
                    "---------------------------------------------------------------------------------------------------------------------------------------------");
            myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    private static Comparator<RecommendedInstance> getRecommendedInstancesComparator() {
        return (ri1, ri2) -> ri1.getName()
                                .compareTo(ri2.getName());
    }

    private static Comparator<InstanceLink> getInstanceLinkComparator() {
        return (i1, i2) -> Integer.compare(i1.getModelInstance()
                                             .getUid(),
                (i2.getModelInstance()
                   .getUid()));
    }

    private static Comparator<RecommendedRelation> getRecommendedRelationComparator() {
        return (rl1, rl2) -> rl1.getRelationInstances()
                                .get(0)
                                .getName()
                                .compareTo(rl2.getRelationInstances()
                                              .get(0)
                                              .getName());
    }

    private static Comparator<RelationLink> getRelationLinkComparator() {
        return (i1, i2) -> Integer.compare(i1.getModelRelation()
                                             .getUid(),
                (i2.getModelRelation()
                   .getUid()));
    }

}
