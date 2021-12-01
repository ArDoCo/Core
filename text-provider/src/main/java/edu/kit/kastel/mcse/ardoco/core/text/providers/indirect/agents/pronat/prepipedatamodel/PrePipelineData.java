/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.parse.luna.data.AbstractPrePipelineData;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token.MainHypothesisToken;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token.Token;

/**
 * @author Sebastian Weigelt
 * @author Jan Keim
 */
@MetaInfServices(AbstractPrePipelineData.class)
public class PrePipelineData extends AbstractPrePipelineData {

    private String transcription;
    private String[] transcriptions;
    private Token[] tokens;
    private List<MainHypothesisToken> mainHypothesis;
    private List<List<MainHypothesisToken>> altHypotheses;
    private List<List<Token>> taggedHypotheses;
    private Path inputFilePath;

    public PrePipelineData() {
        super();
        transcription = null;
        transcriptions = null;
        tokens = null;
        altHypotheses = null;
        taggedHypotheses = null;
        mainHypothesis = null;
        inputFilePath = null;
    }

    /**
     * returns the transcription as String
     *
     * @return the transcription
     * @throws MissingDataException thrown iff the transcription string is missing
     */
    public String getTranscription() throws MissingDataException {
        if (transcription != null) {
            return transcription;
        } else {
            throw new MissingDataException("Transcription string is undefined", new NullPointerException());
        }

    }

    /**
     * sets the transcription
     *
     * @param transcription the transcription to set
     */
    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }

    /**
     * @return the transcriptions
     * @throws MissingDataException thrown iff no transcriptions were defined
     */
    public String[] getTranscriptions() throws MissingDataException {
        if (transcriptions != null) {
            return transcriptions;
        } else {
            throw new MissingDataException("No transcriptions defined", new NullPointerException());
        }
    }

    /**
     * sets the transcriptions (Array of Strings)
     *
     * @param transcriptions the transcriptions to set
     */
    public void setTranscriptions(String[] transcriptions) {
        this.transcriptions = transcriptions;
    }

    /**
     * returns the tokens
     *
     * @return the tokens
     * @throws MissingDataException thrown if no tokens were defined
     */
    public Token[] getTokens() throws MissingDataException {
        if (tokens != null) {
            return tokens;
        } else {
            throw new MissingDataException("No tokens defined", new NullPointerException());
        }
    }

    /**
     * sets the tokens
     *
     * @param tokens the tokens to set
     */
    public void setTokens(Token[] tokens) {
        this.tokens = tokens;
    }

    /**
     * returns the main hypothesis as list.
     *
     * @return the main hypothesis
     * @throws MissingDataException thrown iff no main hypothesis was defined
     */
    public List<MainHypothesisToken> getMainHypothesis() throws MissingDataException {
        if (mainHypothesis != null) {
            return mainHypothesis;
        } else {
            throw new MissingDataException("No main hypothesis defined", new NullPointerException());
        }
    }

    /**
     * sets the main hypothesis
     *
     * @param hypothesis the main hypothesis to set
     */
    public void setMainHypothesis(List<MainHypothesisToken> hypothesis) {
        mainHypothesis = hypothesis;
    }

    /**
     * returns all alternative hypotheses as list.
     *
     * @return the alt. hypotheses
     * @throws MissingDataException thrown iff no alternative hypotheses were defined
     */
    public List<List<MainHypothesisToken>> getAltHypotheses() throws MissingDataException {
        if (altHypotheses != null) {
            return altHypotheses;
        } else {
            throw new MissingDataException("No alternative hypotheses defined", new NullPointerException());
        }
    }

    /**
     * return the alternative hypothesis at the given index.
     *
     * @param index the index of the hypothesis
     * @return the alt. hypothesis
     * @throws MissingDataException thrown iff no alternative hypotheses were defined
     */
    public List<MainHypothesisToken> getAltHypothesis(int index) throws MissingDataException {
        if (altHypotheses != null) {
            return altHypotheses.get(index);
        } else {
            throw new MissingDataException("No alternative hypotheses defined", new NullPointerException());
        }
    }

    /**
     * sets the alternative hypotheses
     *
     * @param hypotheses the alt. hypotheses to set
     */
    public void setAltHypotheses(List<List<MainHypothesisToken>> hypotheses) {
        altHypotheses = hypotheses;
    }

    /**
     * adds a hypothesis at the end of the list of alternative hypotheses.
     *
     * @param hypothesis the hypothesis to add
     */
    public void addAltHypothesis(List<MainHypothesisToken> hypothesis) {
        if (altHypotheses == null) {
            altHypotheses = new ArrayList<>();
        }
        altHypotheses.add(hypothesis);
    }

    /**
     * adds a hypothesis at a given index to the list of alternative hypotheses.
     *
     * @param hypothesis the hypothesis to add
     * @param index      the index
     */
    public void addAltHypothesisAtIndex(List<MainHypothesisToken> hypothesis, int index) {
        if (altHypotheses == null) {
            altHypotheses = new ArrayList<>();
        }
        altHypotheses.add(index, hypothesis);
    }

    /**
     * returns the hypotheses (List of List of Token) tagged by SNLP
     *
     * @return the taggedHypotheses
     * @throws MissingDataException thrown iff no tagged hypotheses were defined
     */
    public List<List<Token>> getTaggedHypotheses() throws MissingDataException {
        if (taggedHypotheses != null) {
            return taggedHypotheses;
        } else {
            throw new MissingDataException("No tagged hypotheses defined", new NullPointerException());
        }
    }

    /**
     * returns the tagged hypothesis
     *
     * @param index the index
     * @return the tagged hypothesis at the given index
     * @throws MissingDataException thrown iff no tagged hypotheses were defined at the given index
     */
    public List<Token> getTaggedHypothesis(int index) throws MissingDataException {
        if (taggedHypotheses != null) {
            return taggedHypotheses.get(index);
        } else {
            throw new MissingDataException("No tagged hypothesis defined at the given index", new NullPointerException());
        }
    }

    /**
     * set tagged hypotheses (should be used exclusively by SNLP)
     *
     * @param taggedHypotheses the taggedHypotheses to set
     */
    public void setTaggedHypotheses(List<List<Token>> taggedHypotheses) {
        this.taggedHypotheses = taggedHypotheses;
    }

    /**
     * adds a tagged hypothesis at the end of the list of tagged hypotheses.
     *
     * @param taggedHypothesis the tagged hypothesis to add
     */
    public void addTaggedHypothesis(List<Token> taggedHypothesis) {
        if (taggedHypotheses == null) {
            taggedHypotheses = new ArrayList<>();
        }
        taggedHypotheses.add(taggedHypothesis);
    }

    /**
     * adds a tagged hypothesis at a given index to the list of tagged hypotheses.
     *
     * @param taggedHypothesis the tagged hypothesis to add
     * @param index            the index
     */
    public void addTaggedHypothesisAtIndex(List<Token> taggedHypothesis, int index) {
        if (taggedHypotheses == null) {
            taggedHypotheses = new ArrayList<>();
        }
        taggedHypotheses.add(index, taggedHypothesis);
    }

    /**
     * returns the path of the input file containing the utterance
     *
     * @return the inputFilePath
     * @throws MissingDataException thrown if no tagged input file path was defined
     */
    public Path getInputFilePath() throws MissingDataException {
        if (inputFilePath != null) {
            return inputFilePath;

        } else {
            throw new MissingDataException("No tagged input file path defined", new NullPointerException());
        }

    }

    /**
     * sets the path of the input file containing the utterance
     *
     * @param inputFilePath the inputFilePath to set
     */
    public void setInputFilePath(Path inputFilePath) {
        this.inputFilePath = inputFilePath;
    }
}
