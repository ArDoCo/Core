/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.ModificationStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.Modifications;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.ModifiedElement;
import edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp.CoreNLPProvider;

public class DeleteOneSentenceEach implements ModificationStrategy {

    private File text;
    private List<String> lines;

    public DeleteOneSentenceEach(File text) {
        this.text = text;
        initLines();
    }

    private void initLines() {
        lines = new ArrayList<>();
        try (Scanner scan = new Scanner(text)) {
            while (scan.hasNextLine()) {
                lines.add(scan.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        lines.removeIf(Objects::isNull);
    }

    @Override
    public Iterator<ModifiedElement<Text, Integer>> getModifiedTexts() {
        return new DeleteOneSentenceEachIterator();
    }

    private class DeleteOneSentenceEachIterator implements Iterator<ModifiedElement<Text, Integer>> {
        private int currentDeletion = 0;

        @Override
        public boolean hasNext() {
            return currentDeletion < lines.size();
        }

        @Override
        public ModifiedElement<Text, Integer> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int deleted = currentDeletion++;

            try {
                File tmp = File.createTempFile(this.getClass().getSimpleName(), ".txt");
                write(tmp, deleted);
                Text newText = new CoreNLPProvider(new DataRepository(), new FileInputStream(tmp)).getAnnotatedText();
                return ModifiedElement.of(newText, deleted + 1, Modifications.DELETE_SENTENCE);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private void write(File target, int deleted) {
            try (FileWriter fw = new FileWriter(target)) {
                for (int i = 0; i < lines.size(); i++) {
                    if (i == deleted) {
                        continue;
                    }
                    fw.append(lines.get(i)).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Iterator<ModifiedElement<ModelConnector, ModelInstance>> getModifiedModelInstances() {
        throw new UnsupportedOperationException();
    }
}
