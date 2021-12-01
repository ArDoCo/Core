/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.misc.Pair;

public class TextRandomizer {

    private TextRandomizer() {
        throw new IllegalAccessError();
    }

    public static Pair<File, List<Pair<String, Map<Integer, Integer>>>> combineFilesRandomly(File file1, File file2, File file3) throws IOException {

        Map<String, Integer> lineNumbers1 = collectLines(file1);
        Map<String, Integer> lineNumbers2 = collectLines(file2);
        Map<String, Integer> lineNumbers3 = collectLines(file3);

        List<String> resultLines = new ArrayList<>();
        // oldLine, newLine
        Map<Integer, Integer> file1Lines = new HashMap<>();
        Map<Integer, Integer> file2Lines = new HashMap<>();
        Map<Integer, Integer> file3Lines = new HashMap<>();

        int resultLine = 0;
        int current1Line = 0;
        int current2Line = 0;
        int current3Line = 0;
        List<String> lines1 = new ArrayList<>(lineNumbers1.keySet());
        List<String> lines2 = new ArrayList<>(lineNumbers2.keySet());
        List<String> lines3 = new ArrayList<>(lineNumbers2.keySet());

        int[] randomNumbers = { 1, 2, 3 };

        for (int i = 0; i <= lines1.size() + lines2.size() + lines3.size(); i++) {

            int random = randomNumbers[new Random().nextInt(randomNumbers.length)];

            if (random == 1 && current1Line < lines1.size()) {
                String currentLine = lines1.get(current1Line);
                resultLines.add(currentLine);
                file1Lines.put(lineNumbers1.get(currentLine), resultLine);
                current1Line++;
            } else if (random == 2 && current2Line < lines2.size()) {
                String currentLine = lines2.get(current2Line);
                resultLines.add(currentLine);
                file2Lines.put(lineNumbers2.get(currentLine), resultLine);
                current2Line++;
            } else if (random == 3 && current3Line < resultLines.size()) {
                String currentLine = lines3.get(current3Line);
                resultLines.add(currentLine);
                file3Lines.put(lineNumbers3.get(currentLine), resultLine);
                current3Line++;
            } else {
                continue;
            }
            resultLine++;
        }

        String fileName = "permutation" + System.currentTimeMillis() + ".txt";
        FileWriter fw = new FileWriter(fileName);

        for (String line : resultLines) {
            fw.write(line);
        }

        fw.close();

        Pair<String, Map<Integer, Integer>> goldStandard1 = new Pair<>(file1.getName(), file1Lines);
        Pair<String, Map<Integer, Integer>> goldStandard2 = new Pair<>(file2.getName(), file2Lines);
        Pair<String, Map<Integer, Integer>> goldStandard3 = new Pair<>(file3.getName(), file3Lines);
        List<Pair<String, Map<Integer, Integer>>> goldStandards = new ArrayList<>();
        goldStandards.add(goldStandard1);
        goldStandards.add(goldStandard2);
        goldStandards.add(goldStandard3);
        return new Pair<>(new File(fileName), goldStandards);
    }

    private static Map<String, Integer> collectLines(File file) {
        Map<String, Integer> lines = new HashMap<>();
        String fileName = file.getName();
        int currentLine = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line; (line = br.readLine()) != null; currentLine++) {
                lines.put(line, currentLine);

            }
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found: " + fileName);
        } catch (IOException e) {
            System.out.println("IOException while reading: " + fileName);
        }
        return lines;
    }

}
