/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;

public class GoldStandard {
    private File goldStandard;
    private IModelConnector model;

    private MutableList<MutableList<IModelInstance>> sentence2instance = Lists.mutable.empty();

    public GoldStandard(File goldStanard, IModelConnector model) {
        goldStandard = goldStanard;
        this.model = model;
        load();
    }

    private void load() {
        try (Scanner scan = new Scanner(goldStandard)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line == null || line.isBlank() || line.contains("modelElementID")) {
                    // continue if line is empty, null, or is the header (that starts with "modelElementID")
                    continue;
                }

                String[] idXline = line.strip().split(",");
                IModelInstance instance = model.getInstances().select(i -> i.getUid().equals(idXline[0])).getFirst();
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
            e.printStackTrace();
        }
    }

    public ImmutableList<IModelInstance> getModelInstances(int sentenceNo) {
        // Index starts at 1
        return sentence2instance.get(sentenceNo).toImmutable();
    }

    public ImmutableList<Integer> getSentencesWithElement(IModelInstance elem) {
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
