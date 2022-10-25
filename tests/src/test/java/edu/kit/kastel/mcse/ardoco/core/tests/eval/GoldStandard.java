/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;

public class GoldStandard {
    private Logger logger = LoggerFactory.getLogger(GoldStandard.class);

    private File goldStandard;
    private ModelConnector model;

    private MutableList<MutableList<ModelInstance>> sentence2instance = Lists.mutable.empty();

    public GoldStandard(File goldStandard, ModelConnector model) {
        this.goldStandard = goldStandard;
        this.model = model;
        load();
    }

    private void load() {
        try (Scanner scan = new Scanner(goldStandard, StandardCharsets.UTF_8.name())) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line == null || line.isBlank() || line.startsWith("modelElement") || line.contains("modelElementID")) {
                    // continue if line is empty, null, or is the header (that starts with "modelElementID")
                    continue;
                }

                String[] idXline = line.strip().split(",", -1);
                ModelInstance instance = model.getInstances().select(i -> i.getUid().equals(idXline[0])).getFirst();
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
        } catch (FileNotFoundException e) {
            logger.warn(e.getMessage(), e.getCause());
        }
    }

    public ImmutableList<ModelInstance> getModelInstances(int sentenceNo) {
        // Index starts at 1
        return sentence2instance.get(sentenceNo).toImmutable();
    }

    public ImmutableList<Integer> getSentencesWithElement(ModelInstance elem) {
        MutableList<Integer> sentences = Lists.mutable.empty();
        for (int i = 0; i < sentence2instance.size(); i++) {
            var instances = sentence2instance.get(i);
            if (instances.anySatisfy(e -> e.getUid().equals(elem.getUid()))) {
                sentences.add(i);
            }
        }
        return sentences.toImmutable();
    }
}
