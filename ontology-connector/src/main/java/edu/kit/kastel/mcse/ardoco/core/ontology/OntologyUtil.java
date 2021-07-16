package edu.kit.kastel.mcse.ardoco.core.ontology;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.jena.ontology.OntModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OntologyUtil {
    private static Logger logger = LogManager.getLogger(OntologyUtil.class);
    private static Random random = new Random();

    private static Set<String> assignedIDs = new HashSet<>();

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
    public static String generateRandomURI(OntModel ontModel, String prefix) {
        return createUri(ontModel, prefix, OntologyUtil.generateRandomID());
    }

    private static String createUri(OntModel ontModel, String prefix, String suffix) {
        String encodedSuffix = suffix;
        try {
            encodedSuffix = URLEncoder.encode(suffix, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return ontModel.expandPrefix(prefix + ":" + encodedSuffix);
    }

    /**
     * Creates a uri for the given {@link OntologyConnector} based on the prefix and suffix
     *
     * @param ontologyConnector {@link OntologyConnector} that the URI will be made for
     * @param prefix            prefix that should be used
     * @param suffix            suffix that should be used
     * @return URI
     */
    public static String createUri(OntologyConnector ontologyConnector, String prefix, String suffix) {
        return createUri(ontologyConnector.getOntModel(), prefix, suffix);
    }

}
