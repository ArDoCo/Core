/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;

public class GoldStandard {
    private Logger logger = LoggerFactory.getLogger(GoldStandard.class);

    private File goldStandard;
    private ArchitectureModel model;

    private MutableList<MutableList<ModelElement>> sentence2instance = Lists.mutable.empty();

    public GoldStandard(File goldStandard, ArchitectureModel model) {
        this.goldStandard = goldStandard;
        this.model = model;
        load();
    }

    private void load() {
        try (Scanner scan = new Scanner(goldStandard, StandardCharsets.UTF_8)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line == null || line.isBlank() || line.startsWith("modelElement") || line.contains("modelElementID")) {
                    // continue if line is empty, null, or is the header (that starts with "modelElementID")
                    continue;
                }

                String[] idXline = line.strip().split(",", -1);
                ModelElement instance = Lists.immutable.withAll(model.getContent()).select(i -> i.getId().equals(idXline[0])).getFirst();
                if (instance == null) {
                    System.err.println("No instance found for id \"" + idXline[0] + "\"");
                    continue;
                }
                int sentence = Integer.parseInt(idXline[1]);
                while (sentence2instance.size() <= sentence) {
                    sentence2instance.add(Lists.mutable.empty());
                }
                sentence2instance.get(sentence).add(instance);
            }
        } catch (IOException e) {
            logger.warn(e.getMessage(), e.getCause());
        }
    }

    public ImmutableList<ModelElement> getModelInstances(int sentenceNo) {
        // Index starts at 1
        return sentence2instance.get(sentenceNo).toImmutable();
    }

    public ImmutableList<Integer> getSentencesWithElement(ModelElement elem) {
        MutableList<Integer> sentences = Lists.mutable.empty();
        for (int i = 0; i < sentence2instance.size(); i++) {
            var instances = sentence2instance.get(i);
            if (instances.anySatisfy(e -> e.getId().equals(elem.getId()))) {
                sentences.add(i);
            }
        }
        return sentences.toImmutable();
    }
}
