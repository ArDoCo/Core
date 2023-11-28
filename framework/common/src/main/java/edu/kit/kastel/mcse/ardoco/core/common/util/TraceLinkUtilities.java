/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadSamTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TransitiveTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;

public class TraceLinkUtilities {

    private static final String ENTRY_SEPARATOR = ",";

    private TraceLinkUtilities() {
        throw new IllegalStateException();
    }

    public static String createTraceLinkString(String firstElementId, String secondElementId) {
        return firstElementId + ENTRY_SEPARATOR + secondElementId;
    }

    public static ImmutableList<String> getSadSamTraceLinksAsStringList(ImmutableList<SadSamTraceLink> sadSamTraceLinks) {
        return sadSamTraceLinks.collect(tl -> createTraceLinkString(tl.getModelElementUid(), String.valueOf(tl.getSentenceNumber() + 1)));
    }

    public static ImmutableList<String> getSamCodeTraceLinksAsStringList(ImmutableList<SamCodeTraceLink> samCodeTraceLinks) {
        MutableList<String> resultsMut = Lists.mutable.empty();
        for (var traceLink : samCodeTraceLinks) {
            EndpointTuple endpointTuple = traceLink.getEndpointTuple();
            var modelElement = endpointTuple.firstEndpoint();
            var codeElement = (CodeCompilationUnit) endpointTuple.secondEndpoint();
            String traceLinkString = createTraceLinkString(modelElement.getId(), codeElement.toString());
            resultsMut.add(traceLinkString);
        }
        return resultsMut.toImmutable();
    }

    public static ImmutableList<String> getSadCodeTraceLinksAsStringList(ImmutableList<SadCodeTraceLink> sadCodeTraceLinks) {
        MutableList<String> resultsMut = Lists.mutable.empty();
        for (var traceLink : sadCodeTraceLinks) {
            EndpointTuple endpointTuple = traceLink.getEndpointTuple();
            var codeElement = (CodeCompilationUnit) endpointTuple.secondEndpoint();
            String sentenceNumber;
            if (traceLink instanceof TransitiveTraceLink transitiveTraceLink) {
                sentenceNumber = String.valueOf(((SadSamTraceLink) transitiveTraceLink.getFirstTraceLink()).getSentenceNumber() + 1);
            } else if (traceLink.getEndpointTuple().firstEndpoint() instanceof RecommendedInstance) {
                // Direct trace links
                // Assumption: Only one type of trace link
                return getDirectSadCodeTraceLinksAsStringList(sadCodeTraceLinks);
            } else {
                throw new IllegalArgumentException("Unsupported type of tracelink: " + traceLink);
            }

            String traceLinkString = TraceLinkUtilities.createTraceLinkString(sentenceNumber, codeElement.toString());
            resultsMut.add(traceLinkString);

        }
        return resultsMut.toImmutable();
    }

    private static ImmutableList<String> getDirectSadCodeTraceLinksAsStringList(ImmutableList<SadCodeTraceLink> sadCodeTraceLinks) {
        MutableList<String> result = Lists.mutable.empty();
        for (var traceLink : sadCodeTraceLinks) {
            if (!(traceLink.getEndpointTuple().firstEndpoint() instanceof RecommendedInstance recommendedInstance))
                return result.toImmutable();

            var codeElement = (CodeCompilationUnit) traceLink.getEndpointTuple().secondEndpoint();
            ImmutableSortedSet<Integer> sentenceNumbers = recommendedInstance.getSentenceNumbers();
            for (var sentence : sentenceNumbers) {
                String traceLinkString = TraceLinkUtilities.createTraceLinkString(String.valueOf(sentence + 1), codeElement.toString());
                result.add(traceLinkString);
            }
        }
        return result.toImmutable();
    }

}
