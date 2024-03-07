/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.id.tests.eval.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CombineGoldStandards {
    public static void main(String[] args) throws IOException {
        try (Scanner scan = new Scanner(System.in)) {
            System.out.println("Enter TextFile1: ");
            File text1 = new File(scan.nextLine());
            System.out.println("Enter GoldStandard1: ");
            File gs1 = new File(scan.nextLine());

            System.out.println("Enter TextFile2: ");
            File text2 = new File(scan.nextLine());
            System.out.println("Enter GoldStandard2: ");
            File gs2 = new File(scan.nextLine());

            merge(text1, gs1, text2, gs2);
        }
    }

    private static void merge(File text1file, File gs1file, File text2file, File gs2file) throws IOException {
        System.out.println(""//
                + "Merging Text " + text1file.getAbsolutePath() + " and " + text2file.getAbsolutePath() //
                + " with " + gs1file.getAbsolutePath() + " and " + gs2file.getAbsolutePath() //
        );

        var text1 = read(text1file);
        var text2 = read(text2file);
        var gs1 = read(gs1file);
        var gs2 = read(gs2file);

        List<String> textLines = new ArrayList<>();
        List<String> goldStandard = new ArrayList<>();

        final int offset = text1.size();

        textLines.addAll(text1);
        textLines.addAll(text2);
        goldStandard.addAll(gs1);

        for (var gsLine : gs2) {
            String[] nameXLine = gsLine.strip().split(",");
            if (nameXLine.length != 2 || !nameXLine[1].matches("\\d+")) {
                System.err.println("Skipping Line: " + gsLine);
                continue;
            }
            int newLine = offset + Integer.parseInt(nameXLine[1]);
            goldStandard.add(nameXLine[0] + "," + newLine);
        }

        Files.write(Path.of(".", "target", "merged_text.txt"), textLines);
        Files.write(Path.of(".", "target", "merged_gs.txt"), goldStandard);
    }

    private static List<String> read(File file) throws FileNotFoundException {
        try (var scan = new Scanner(file).useDelimiter("\\A")) {
            return Arrays.stream(scan.next().strip().split("\\n")).map(String::strip).toList();
        }
    }
}
