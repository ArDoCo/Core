package edu.kit.kastel.mcse.ardoco.core.pipeline.helpers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.kit.kastel.mcse.ardoco.core.datastructures.NounMappingWithDistribution;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstanceLink;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRelationLink;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.MappingKind;

public class FilePrinter {
    private static final Logger logger = LogManager.getLogger(FilePrinter.class);

    private static final String GENERIC_ERROR = "An error occurred.";
    private static final String SUCCESS_WRITE = "Successfully wrote to the file.";

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
    public static void writeSentencesInFile(File target, IText graph) {
        boolean fileCreated = createFileIfNonExistent(target);
        if (!fileCreated) {
            return;
        }

        try (FileWriter myWriter = new FileWriter(target)) {
            int minSentenceNumber = 0;
            for (IWord node : graph.getWords()) {
                int sentenceNumber = Integer.parseInt(String.valueOf(node.getSentenceNo()));
                if (sentenceNumber + 1 > minSentenceNumber) {
                    myWriter.append(LINE_SEPARATOR + sentenceNumber + ": ");
                    minSentenceNumber++;
                }
                myWriter.append(" ").append(node.getText());
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

        if (textState.getAllMappings().isEmpty() || !(textState.getAllMappings().get(0) instanceof NounMappingWithDistribution)) {
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

            NounMappingWithDistribution eagleMapping = (NounMappingWithDistribution) mapping;
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
                for (IWord word : nameMapping.getWords()) {
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

    private static String convertToCSV(String[] data) {
        return Stream.of(data).map(FilePrinter::escapeSpecialCharacters).collect(Collectors.joining(","));
    }

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
