/* Licensed under MIT 2021. */
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

import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.IModificationStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.Modifications;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.ModifiedElement;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.ParseProvider;

public class DeleteOneSentenceEach implements IModificationStrategy {

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
    public Iterator<ModifiedElement<IText, Integer>> getModifiedTexts() {
        return new DeleteOneSentenceEachIterator();
    }

    private class DeleteOneSentenceEachIterator implements Iterator<ModifiedElement<IText, Integer>> {
        private int currentDeletion = 0;

        @Override
        public boolean hasNext() {
            return currentDeletion < lines.size();
        }

        @Override
        public ModifiedElement<IText, Integer> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int deleted = currentDeletion++;

            try {
                File tmp = File.createTempFile(this.getClass().getSimpleName(), ".txt");
                write(tmp, deleted);
                ITextConnector textConnector = new ParseProvider(new FileInputStream(tmp));
                IText newText = textConnector.getAnnotatedText();
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
    public Iterator<ModifiedElement<IModelConnector, IModelInstance>> getModifiedModelInstances() {
        throw new UnsupportedOperationException();
    }
}
