package edu.kit.kastel.mcse.ardoco.core.pipeline.helpers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInconsistency;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstanceLink;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelInstance;
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
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.Tracelink;

/**
 * The Class FilePrinter contains some helpers for stats.
 */
public final class FilePrinter {
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
     * @param target the target file
     * @param text   the text to use.
     */
    public static void writeSentencesInFile(File target, IText text) {
        boolean fileCreated = createFileIfNonExistent(target);
        if (!fileCreated) {
            return;
        }

        try (var myWriter = new FileWriter(target)) {
            var minSentenceNumber = 0;
            for (IWord node : text.getWords()) {
                var sentenceNumber = Integer.parseInt(String.valueOf(node.getSentenceNo()));
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
                logger.info("File created: {}", file.getAbsolutePath());

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
        ImmutableList<String> nameList = ntrState.getNameList().toSortedList().toImmutable();
        myWriter.write(nameList.toString() + LINE_SEPARATOR);
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("FoundNameTerms as Set: ");
        myWriter.append(LINE_SEPARATOR);
        ImmutableList<String> nameTermList = ntrState.getNameTermList().toSortedList().toImmutable();
        myWriter.write(nameTermList.toString() + LINE_SEPARATOR);
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("FoundNORTs as Set: ");
        myWriter.append(LINE_SEPARATOR);
        ImmutableList<String> nortList = ntrState.getNortList().toSortedList().toImmutable();
        myWriter.write(nortList.toString() + LINE_SEPARATOR);
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("FoundTypes as Set: ");
        myWriter.append(LINE_SEPARATOR);
        ImmutableList<String> typeList = ntrState.getTypeList().toSortedList().toImmutable();
        myWriter.write(typeList.toString() + LINE_SEPARATOR);
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("FoundTypeTerms as Set: ");
        myWriter.append(LINE_SEPARATOR);
        ImmutableList<String> typeTermList = ntrState.getTypeTermList().toSortedList().toImmutable();
        myWriter.write(typeTermList.toString() + LINE_SEPARATOR);
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("Instances of the Recommendation State: ");
        myWriter.append(LINE_SEPARATOR);

        Comparator<IRecommendedInstance> comRecommendedInstanceByName = getRecommendedInstancesComparator();
        ImmutableList<IRecommendedInstance> recommendedInstances = recommendationState.getRecommendedInstances()
                .toSortedList(comRecommendedInstanceByName)
                .toImmutable();

        for (IRecommendedInstance ri : recommendedInstances) {
            myWriter.write(ri.toString() + LINE_SEPARATOR);
        }
        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("Instances of the Connection State: ");
        myWriter.append(LINE_SEPARATOR);

        Comparator<IInstanceLink> compInstByUID = getInstanceLinkComparator();
        ImmutableList<IInstanceLink> instanceMappings = Lists.immutable.withAll(connectionState.getInstanceLinks()).toSortedList(compInstByUID).toImmutable();

        for (IInstanceLink imap : instanceMappings) {

            myWriter.write(imap.toString() + LINE_SEPARATOR);
        }

        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("Relations of the Recommendation State: ");
        myWriter.append(LINE_SEPARATOR);

        Comparator<IRecommendedRelation> compRRelationsByFirstInstanceName = getRecommendedRelationComparator();
        ImmutableList<IRecommendedRelation> rels = recommendationState.getRecommendedRelations().toSortedList(compRRelationsByFirstInstanceName).toImmutable();

        for (IRecommendedRelation si : rels) {
            myWriter.write(si.toString() + LINE_SEPARATOR);
        }

        myWriter.write(HORIZONTAL_RULE);
        myWriter.append(LINE_SEPARATOR + LINE_SEPARATOR);

        myWriter.write("Relations of the Connection State: ");
        myWriter.append(LINE_SEPARATOR);

        Comparator<IRelationLink> compRelByUID = getRelationLinkComparator();
        ImmutableList<IRelationLink> relationLinks = Lists.immutable.withAll(connectionState.getRelationLinks()).toSortedList(compRelByUID).toImmutable();

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

    /**
     * * Writes the states into a file.
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

        try (var myWriter = new FileWriter(resultFile)) {
            writeStates(myWriter, extractionState, ntrState, recommendationState, connectionState, duration);

        } catch (IOException e) {
            logger.error(GENERIC_ERROR);
            logger.debug(e.getMessage(), e.getCause());
        }

    }

    /**
     * Write model instances in csv file.
     *
     * @param destination the destination
     * @param modelState  the model state
     * @param name        the name
     */
    public static void writeModelInstancesInCsvFile(File destination, IModelState modelState, String name) {
        ImmutableList<String[]> dataLines = getInstancesFromModelState(modelState, name);
        writeDataLinesInFile(destination, dataLines);
    }

    private static ImmutableList<String[]> getInstancesFromModelState(IModelState modelState, String name) {
        MutableList<String[]> dataLines = Lists.mutable.empty();

        dataLines.add(new String[] { "Found Model Elements in " + name + ":", "", "" });
        dataLines.add(new String[] { "" });
        dataLines.add(new String[] { "UID", "Name", "Type" });

        for (IModelInstance instance : modelState.getInstances()) {

            dataLines.add(new String[] { instance.getUid(), instance.getLongestName(), instance.getLongestType() });

        }

        return dataLines.toImmutable();
    }

    /**
     * Write trace links in csv file.
     *
     * @param resultFile      the result file
     * @param connectionState the connection state
     */
    public static void writeTraceLinksInCsvFile(File resultFile, IConnectionState connectionState) {
        ImmutableList<String[]> dataLines = getLinksAsDataLinesOfConnectionState(connectionState);
        writeDataLinesInFile(resultFile, dataLines);
    }

    /**
     * Write noun mappings in csv file.
     *
     * @param resultFile the result file
     * @param textState  the text state
     */
    public static void writeNounMappingsInCsvFile(File resultFile, ITextState textState) {
        ImmutableList<String[]> dataLines = getMappingsAsDataLinesOfTextState(textState);
        writeDataLinesInFile(resultFile, dataLines);
    }

    private static ImmutableList<String[]> getMappingsAsDataLinesOfTextState(ITextState textState) {
        MutableList<String[]> dataLines = Lists.mutable.empty();

        dataLines.add(new String[] { "Found NounMappings: ", "", "", "" });
        dataLines.add(new String[] { "" });
        dataLines.add(new String[] { "Reference", "Name", "Type", "NameOrType" });

        if (textState.getNounMappings().isEmpty() || !(textState.getNounMappings().get(0) instanceof NounMapping)) {
            for (INounMapping mapping : textState.getNounMappings()) {

                MappingKind kind = mapping.getKind();

                var nameProb = Double.toString(kind == MappingKind.NAME ? mapping.getProbability() : 0);
                var typeProb = Double.toString(kind == MappingKind.TYPE ? mapping.getProbability() : 0);
                var nortProb = Double.toString(kind == MappingKind.NAME_OR_TYPE ? mapping.getProbability() : 0);

                dataLines.add(new String[] { mapping.getReference(), nameProb, typeProb, nortProb });

            }
            return dataLines.toImmutable();
        }

        for (INounMapping mapping : textState.getNounMappings()) {

            NounMapping eagleMapping = (NounMapping) mapping;
            Map<MappingKind, Double> distribution = eagleMapping.getDistribution();
            var nameProb = Double.toString(distribution.get(MappingKind.NAME));
            var typeProb = Double.toString(distribution.get(MappingKind.TYPE));
            var nortProb = Double.toString(distribution.get(MappingKind.NAME_OR_TYPE));

            dataLines.add(new String[] { eagleMapping.getReference(), nameProb, typeProb, nortProb });

        }
        return dataLines.toImmutable();
    }

    private static ImmutableList<String[]> getLinksAsDataLinesOfConnectionState(IConnectionState connectionState) {
        MutableList<String[]> dataLines = Lists.mutable.empty();

        dataLines.add(new String[] { "#Found TraceLinks: ", "", "" });
        dataLines.add(new String[] { "" });
        dataLines.add(new String[] { "modelElementID", "sentence", "confidence" });

        // TODO previously, there was a selection of InstanceLinks with highest confidence for a modelElementUid
        // Have to check, what that actually did and what the difference is
        // Previously: multiple tracelinks for a single modelElement in a single sentence possible
        // Now: Only one tracelink for one ModelElement(Id) in one sentence. Can be adapted by not using a set

        Set<Tracelink> tracelinks = new HashSet<>(connectionState.getTraceLinks().castToCollection());
        for (var tracelink : tracelinks) {
            var modelElementUid = tracelink.getModelElementUid();
            // sentence offset is 1 because real sentences are 1-indexed
            var sentenceNumber = Integer.toString(tracelink.getSentenceNumber() + 1);
            var probability = Double.toString(tracelink.getProbability());
            dataLines.add(new String[] { modelElementUid, sentenceNumber, probability });
        }

        return dataLines.toImmutable();
    }

    /**
     * Write data lines in file.
     *
     * @param file      the file
     * @param dataLines the data lines
     */
    public static void writeDataLinesInFile(File file, ImmutableList<String[]> dataLines) {

        try (var pw = new FileWriter(file)) {
            dataLines.collect(FilePrinter::convertToCSV).forEach(s -> {
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

    public static void writeInconsistenciesToFile(File file, IInconsistencyState inconsistencyState) {
        List<IInconsistency> inconsistencies = inconsistencyState.getInconsistencies();
        List<String> inconsistencyReasons = inconsistencies.stream().map(IInconsistency::getReason).toList();

        try (var pw = new FileWriter(file, StandardCharsets.UTF_8)) {
            inconsistencyReasons.stream().forEach(s -> {
                logger.debug("Found Inconsistency: {}", s);
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

    private static String escapeSpecialCharacters(String in) {
        String data = in;
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
