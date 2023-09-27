/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.statehelper.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.InstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping;

public class ConnectionStateFile {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionStateFile.class);

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String VALUE_SEPARATOR = "|";
    private static final String LIST_SEPARATOR = ",";

    private static final DecimalFormat df = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.US));

    private ConnectionStateFile() {
        throw new IllegalAccessError("This constructor should not be called!");
    }

    /**
     * Writes out the differences of new and old results.
     *
     * @param targetFile   file to write into
     * @param arDoCoResult the ArDoCoResult
     * @throws IOException if writing fails
     */
    public static void write(Path targetFile, ArDoCoResult arDoCoResult, String modelId) throws IOException {

        var builder = new StringBuilder();

        var connectionState = arDoCoResult.getConnectionState(modelId);
        if (connectionState == null) {
            logger.error("Could not load ConnectionState. Abort writing.");
            return;
        }

        builder.append("# ").append(arDoCoResult.getProjectName());
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        builder.append(String.join(VALUE_SEPARATOR, "UID [M]", "Name [M]", "Type [M]", "Probability [L]", "Sentences [L]", "Name [T]", "Type [T]", "Names [T]",
                "Types [T]", "Probability [T]", "Claimants [T]", LINE_SEPARATOR));
        builder.append(LINE_SEPARATOR);

        for (InstanceLink instanceLink : connectionState.getInstanceLinks().toSortedListBy(il -> il.getEntity().getId())) {

            var entity = instanceLink.getEntity();
            var textInstance = instanceLink.getTextualInstance();

            builder.append(entity.getId());
            builder.append(VALUE_SEPARATOR);
            builder.append(entity.getName());
            builder.append(VALUE_SEPARATOR);
            builder.append(entity.getClass().getName());
            builder.append(VALUE_SEPARATOR);

            builder.append(df.format(instanceLink.getConfidence()));
            builder.append(VALUE_SEPARATOR);
            ImmutableList<String> sentences = textInstance.getSentenceNumbers().toSortedList().collect(no -> Integer.toString(no)).toImmutable();
            builder.append(String.join(LIST_SEPARATOR, sentences));
            builder.append(VALUE_SEPARATOR);

            builder.append(textInstance.getName());
            builder.append(VALUE_SEPARATOR);
            builder.append(textInstance.getType());
            builder.append(VALUE_SEPARATOR);
            builder.append(String.join(LIST_SEPARATOR, textInstance.getNameMappings().collect(NounMapping::getReference)));
            builder.append(VALUE_SEPARATOR);
            builder.append(String.join(LIST_SEPARATOR, textInstance.getTypeMappings().collect(NounMapping::getReference)));
            builder.append(VALUE_SEPARATOR);
            builder.append(df.format(textInstance.getProbability()));
            builder.append(VALUE_SEPARATOR);
            builder.append(String.join(LIST_SEPARATOR, textInstance.getClaimants().collect(c -> c.getClass().getSimpleName())));
            builder.append(LINE_SEPARATOR);

        }

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

    }

    public static boolean writeDiff(Path sourceFile, Path targetFile, Path diffFile, ArDoCoResult arDoCoResult, String modelId) throws IOException {

        var builder = new StringBuilder();

        var currentConnectionState = arDoCoResult.getConnectionState(modelId);
        if (currentConnectionState == null) {
            logger.error("Could not load ConnectionState. Abort writing diff.");
            return false;
        }
        var currentLinks = currentConnectionState.getInstanceLinks().toSortedListBy(il -> il.getEntity().getId());

        builder.append("# ").append(arDoCoResult.getProjectName());
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        builder.append(String.join(VALUE_SEPARATOR, "Probability [L]", "Sentences [L]", "Name [T]", "Type [T]", "Names [T]", "Types [T]", "Probability [T]",
                "Claimants [T]", LINE_SEPARATOR));
        builder.append(LINE_SEPARATOR);

        List<String> lines = Files.readAllLines(sourceFile);
        MutableList<String> differentLinks = Lists.mutable.empty();
        MutableList<String> missingLinks = Lists.mutable.empty();
        MutableList<String> additionalLinks = Lists.mutable.empty();

        int valueStart = 4;

        for (int i = valueStart; i < lines.size(); i++) {

            String line = lines.get(i);
            var parts = Lists.mutable.withAll(List.of(line.split(Pattern.quote(VALUE_SEPARATOR), -1)));

            if (i - valueStart >= currentLinks.size()) {
                missingLinks.add(line);
                missingLinks.add(LINE_SEPARATOR);
                continue;
            }

            var currentLink = currentLinks.get(i - valueStart);
            var currentModelInstance = currentLink.getEntity();
            var currentTextInstance = currentLink.getTextualInstance();

            int modelOrder = parts.get(0).compareTo(currentModelInstance.getId());

            if (modelOrder >= 0) {

                String uid = currentModelInstance.getId();
                String modelName = currentModelInstance.getName();
                String modelType = currentModelInstance.getClass().getName();
                String linkProbability = df.format(currentLink.getConfidence());
                ImmutableList<String> sentences = currentTextInstance.getSentenceNumbers().toSortedList().collect(no -> Integer.toString(no)).toImmutable();
                String name = currentTextInstance.getName();
                String type = currentTextInstance.getType();
                ImmutableList<String> names = currentTextInstance.getNameMappings().collect(NounMapping::getReference).toSet().toImmutableList();
                ImmutableList<String> types = currentTextInstance.getTypeMappings().collect(NounMapping::getReference).toSet().toImmutableList();
                String probability = df.format(currentTextInstance.getProbability());
                ImmutableSet<String> claimants = currentTextInstance.getClaimants().collect(c -> c.getClass().getSimpleName());

                String currentModelLine = String.join(VALUE_SEPARATOR, uid, modelName, modelType);
                String currentLinkLine = String.join(VALUE_SEPARATOR, linkProbability, String.join(LIST_SEPARATOR, sentences), name, type, String.join(
                        LIST_SEPARATOR, names), String.join(LIST_SEPARATOR, types), probability, String.join(LIST_SEPARATOR, claimants));
                if (modelOrder == 0) {

                    if (!parts.get(3).equals(linkProbability) ||//
                            TextStateFile.wordsNotEqual(parts.get(4).split(Pattern.quote(LIST_SEPARATOR), -1), sentences) ||//
                            !parts.get(5).equals(name) ||//
                            !parts.get(6).equals(type) ||//
                            TextStateFile.wordsNotEqual(parts.get(7).split(Pattern.quote(LIST_SEPARATOR), -1), names) ||//
                            TextStateFile.wordsNotEqual(parts.get(8).split(Pattern.quote(LIST_SEPARATOR), -1), types) ||//
                            !parts.get(9).equals(probability) ||//
                            TextStateFile.claimantsNotEqual(parts.get(10).split(Pattern.quote(LIST_SEPARATOR), -1), claimants.toList())) {

                        differentLinks.add(currentModelLine);
                        differentLinks.add(": " + LINE_SEPARATOR);

                        differentLinks.add(currentLinkLine);
                        differentLinks.add(LINE_SEPARATOR);
                        differentLinks.add("instead of" + LINE_SEPARATOR);
                        differentLinks.add(String.join(VALUE_SEPARATOR, parts.subList(3, 11)));
                        differentLinks.add(LINE_SEPARATOR);
                        differentLinks.add(LINE_SEPARATOR);
                    }

                } else {
                    additionalLinks.add(currentLinkLine);
                    additionalLinks.add(LINE_SEPARATOR);
                }
            } else {
                missingLinks.add(line);
                missingLinks.add(LINE_SEPARATOR);
            }

        }

        builder.append("Different InstanceLinks:").append(LINE_SEPARATOR);
        differentLinks.forEach(builder::append);
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        builder.append("Missing InstanceLinks:").append(LINE_SEPARATOR);
        missingLinks.forEach(builder::append);
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        builder.append("Additional InstanceLinks:").append(LINE_SEPARATOR);
        additionalLinks.forEach(builder::append);
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        Files.writeString(diffFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        ConnectionStateFile.write(targetFile, arDoCoResult, modelId);

        return missingLinks.size() == 0 && differentLinks.size() == 0 && additionalLinks.size() == 0;
    }

}
