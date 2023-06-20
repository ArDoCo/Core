/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadSamTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TransitiveTraceLink;

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
            } else {
                sentenceNumber = traceLink.getEndpointTuple().firstEndpoint().getId();
            }
            String traceLinkString = TraceLinkUtilities.createTraceLinkString(sentenceNumber, codeElement.toString());
            resultsMut.add(traceLinkString);
        }
        return resultsMut.toImmutable();
    }

}
