package edu.kit.kastel.mcse.ardoco.core.ontology;

import java.security.SecureRandom;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OntologyUtil {
    private static Random random = new SecureRandom();

    private static Set<String> assignedIDs = ConcurrentHashMap.newKeySet();

    private OntologyUtil() {
        throw new IllegalAccessError("This constructor should not be called!");
    }

    /**
     * Generates a random ID with target length 10.
     *
     * @return String containing a random ID
     */
    public static String generateRandomID() {
        return generateRandomID(10);
    }

    /**
     * Generates a random ID with the given target length
     *
     * @return String containing a random ID
     */
    public static String generateRandomID(int targetStringLength) {
        var leftLimit = 48; // numeral '0'
        var rightLimit = 122; // letter 'z'
        var id = "";
        do {
            var startLetter = Character.toString((char) (random.nextInt(26) + 'a'));
            id = startLetter + random.ints(leftLimit, rightLimit + 1)
                    .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                    .limit(targetStringLength - 1L)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        } while (!assignedIDs.add(id));
        return id;
    }

    /**
     * Generates a random URI using the given prefix.
     *
     * @param prefix Prefix that should be used for namespace
     * @return random URI with the given prefix
     */
    public static String generateRandomURI(OntologyInterface oc, String prefix) {
        return oc.createUri(prefix, OntologyUtil.generateRandomID());
    }

}
