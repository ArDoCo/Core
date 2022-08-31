/* Licensed under MIT 2022. */
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

import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;

public class RecommendationStateFile {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String VALUE_SEPARATOR = "|";
    private static final String LIST_SEPARATOR = ",";

    private static final DecimalFormat df = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.US));

    private RecommendationStateFile() {
        throw new IllegalAccessError("This constructor should not be called!");
    }

    /**
     * Writes out the differences of new and old results.
     *
     * @param targetFile   file to write into
     * @param arDoCoResult the ArDoCoResult
     * @throws IOException if writing fails
     */
    public static void write(Path targetFile, ArDoCoResult arDoCoResult, Metamodel metaModel) throws IOException {

        var builder = new StringBuilder();

        var recommendationState = arDoCoResult.getRecommendationState(metaModel);

        builder.append("# ").append(arDoCoResult.getProjectName());
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        builder.append(String.join(VALUE_SEPARATOR, "Name", "Type", "Probability", "Names", "Types", "Sentences", "Claimants", LINE_SEPARATOR));
        builder.append(LINE_SEPARATOR);

        for (RecommendedInstance recommendation : recommendationState.getRecommendedInstances()
                .toSortedListBy(RecommendedInstance::getType)
                .sortThisBy(RecommendedInstance::getName)) {
            builder.append(recommendation.getName());
            builder.append(VALUE_SEPARATOR);
            builder.append(recommendation.getType());
            builder.append(VALUE_SEPARATOR);
            builder.append(df.format(recommendation.getProbability()));
            builder.append(VALUE_SEPARATOR);
            builder.append(String.join(LIST_SEPARATOR, recommendation.getNameMappings().collect(NounMapping::getReference)));
            builder.append(VALUE_SEPARATOR);
            builder.append(String.join(LIST_SEPARATOR, recommendation.getTypeMappings().collect(NounMapping::getReference)));
            builder.append(VALUE_SEPARATOR);
            ImmutableList<String> sentences = recommendation.getSentenceNumbers().toSortedList().collect(no -> Integer.toString(no)).toImmutable();
            builder.append(String.join(LIST_SEPARATOR, sentences));
            builder.append(VALUE_SEPARATOR);
            builder.append(String.join(LIST_SEPARATOR, recommendation.getClaimants().collect(c -> c.getClass().getSimpleName())));
            builder.append(LINE_SEPARATOR);

        }

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

    }

    public static boolean writeDiff(Path sourceFile, Path targetFile, Path diffFile, ArDoCoResult arDoCoResult, Metamodel metaModel) throws IOException {

        var builder = new StringBuilder();

        var currentRecommendationState = arDoCoResult.getRecommendationState(metaModel);
        var currentRecommendedInstances = currentRecommendationState.getRecommendedInstances()
                .toSortedListBy(RecommendedInstance::getType)
                .sortThisBy(RecommendedInstance::getName);

        builder.append("# ").append(arDoCoResult.getProjectName());
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        builder.append(String.join(VALUE_SEPARATOR, "Name", "Type", "Probability", "Names", "Types", "Sentences", "Claimants", LINE_SEPARATOR));
        builder.append(LINE_SEPARATOR);

        List<String> lines = Files.readAllLines(sourceFile);
        MutableList<String> differentRecommendations = Lists.mutable.empty();
        MutableList<String> missingRecommendations = Lists.mutable.empty();
        MutableList<String> additionalRecommendations = Lists.mutable.empty();

        int valueStart = 4;

        for (int i = valueStart; i < lines.size(); i++) {

            String line = lines.get(i);
            var parts = line.split(Pattern.quote(VALUE_SEPARATOR), -1);

            if (i - valueStart >= currentRecommendedInstances.size()) {
                missingRecommendations.add(line);
                missingRecommendations.add(LINE_SEPARATOR);
                continue;
            }

            var currentRecommendedInstance = currentRecommendedInstances.get(i - valueStart);
            int nameOrder = parts[0].compareTo(currentRecommendedInstance.getName());

            if (nameOrder >= 0) {

                String name = currentRecommendedInstance.getName();
                String type = currentRecommendedInstance.getType();

                int typeOrder = parts[1].compareTo(type);

                String probability = df.format(currentRecommendedInstance.getProbability());
                ImmutableList<String> names = currentRecommendedInstance.getNameMappings().collect(NounMapping::getReference).toSet().toImmutableList();
                ImmutableList<String> types = currentRecommendedInstance.getTypeMappings().collect(NounMapping::getReference).toSet().toImmutableList();
                ImmutableList<String> sentences = currentRecommendedInstance.getSentenceNumbers()
                        .toSortedList()
                        .collect(no -> Integer.toString(no))
                        .toImmutable();
                ImmutableSet<String> claimants = currentRecommendedInstance.getClaimants().collect(c -> c.getClass().getSimpleName());

                String currentLine = String.join(VALUE_SEPARATOR, name, type, probability, String.join(LIST_SEPARATOR, names), String.join(LIST_SEPARATOR,
                        types), String.join(LIST_SEPARATOR, sentences), String.join(LIST_SEPARATOR, claimants));
                if (nameOrder == 0 && typeOrder == 0) {

                    if (!parts[2].equals(probability) ||//
                            TextStateFile.wordsNotEqual(parts[3].split(Pattern.quote(LIST_SEPARATOR), -1), names) ||//
                            TextStateFile.wordsNotEqual(parts[4].split(Pattern.quote(LIST_SEPARATOR), -1), types) ||//
                            TextStateFile.wordsNotEqual(parts[5].split(Pattern.quote(LIST_SEPARATOR), -1), sentences) ||//
                            TextStateFile.claimantsNotEqual(parts[6].split(Pattern.quote(LIST_SEPARATOR), -1), claimants.toList())) {

                        differentRecommendations.add(currentLine);
                        differentRecommendations.add(LINE_SEPARATOR);
                        differentRecommendations.add("instead of" + LINE_SEPARATOR);
                        differentRecommendations.add(line);
                        differentRecommendations.add(LINE_SEPARATOR);
                        differentRecommendations.add(LINE_SEPARATOR);
                    }

                } else if (nameOrder > 0 || typeOrder > 0) {
                    additionalRecommendations.add(currentLine);
                    additionalRecommendations.add(LINE_SEPARATOR);
                } else {
                    missingRecommendations.add(line);
                    missingRecommendations.add(LINE_SEPARATOR);
                }
            } else {
                missingRecommendations.add(line);
                missingRecommendations.add(LINE_SEPARATOR);
            }

        }

        builder.append("Different RecommendedInstances:").append(LINE_SEPARATOR);
        differentRecommendations.forEach(builder::append);
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        builder.append("Missing RecommendedInstances:").append(LINE_SEPARATOR);
        missingRecommendations.forEach(builder::append);
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        builder.append("Additional RecommendedInstances:").append(LINE_SEPARATOR);
        additionalRecommendations.forEach(builder::append);
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        Files.writeString(diffFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        RecommendationStateFile.write(targetFile, arDoCoResult, metaModel);

        return missingRecommendations.size() == 0 && differentRecommendations.size() == 0 && additionalRecommendations.size() == 0;
    }

}
