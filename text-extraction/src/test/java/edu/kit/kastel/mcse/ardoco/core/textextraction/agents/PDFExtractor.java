/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Not a test, but it extracts the data from the downloaded pdfs.
 */
public class PDFExtractor {
    @BeforeEach
    public void checkForCI() {
        Assumptions.assumeFalse(Objects.equals("true", System.getenv("CI")), "Running in CI");
    }

    @Test
    public void extractISO24765() throws IOException {
        var file = new File("./src/main/resources/pdfs/24765-2017.pdf");
        var pdf = Loader.loadPDF(file);
        var maxPages = pdf.getNumberOfPages();
        Set<String> foundWords = new LinkedHashSet<>();
        for (int i = 1; i <= maxPages; i++) {
            PDFTextStripper pts = new PDFTextStripper();
            pts.setStartPage(i);
            pts.setEndPage(i);
            var text = pts.getText(pdf);
            processTextOfPage(text, foundWords);
        }

        new ObjectMapper().writeValue(new File("./src/main/resources/pdfs/24765-2017.pdf.words.txt"), foundWords.stream().toList());
    }

    private void processTextOfPage(String text, Set<String> foundWords) {
        String[] lines = text.lines().toArray(String[]::new);
        boolean lastLineWasIdentifier = false;
        Predicate<String> isIdentifier = l -> l != null && l.trim().matches("^3\\.[0-9]+$");
        for (String line : lines) {
            if (lastLineWasIdentifier) {
                // TODO Maybe skip elements with numbers? e.g., 1GL
                foundWords.add(line.trim());
                lastLineWasIdentifier = false;
                continue;
            }
            if (isIdentifier.test(line)) {
                lastLineWasIdentifier = true;
            }
        }
    }
}
