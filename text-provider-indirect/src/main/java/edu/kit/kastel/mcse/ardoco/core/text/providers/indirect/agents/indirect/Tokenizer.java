package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.indirect;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.parse.luna.data.AbstractPipelineData;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PipelineDataCastException;
import edu.kit.ipd.parse.luna.pipeline.IPipelineStage;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.PrePipelineData;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token.MainHypothesisToken;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;

@MetaInfServices(IPipelineStage.class)
public class Tokenizer implements IPipelineStage {

    private static final String ID = "indirect_tokenizer";

    private TokenizerFactory<CoreLabel> tokenizerFactory;

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.ipd.parse.luna.pipeline.IPipelineStage#init()
     */
    @Override
    public void init() {
        tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.ipd.parse.luna.pipeline.IPipelineStage#exec(edu.kit.ipd.parse .luna.data.AbstractPipelineData)
     */
    @Override
    public void exec(AbstractPipelineData data) throws PipelineStageException {

        // try to get data as pre pipeline data. If this fails, return
        PrePipelineData prePipeData;
        try {
            prePipeData = (PrePipelineData) data.asPrePipelineData();
        } catch (PipelineDataCastException e) {
            var msg = "Cannot process on data - PipelineData unreadable";
            throw new PipelineStageException(msg, e);
        }

        // try to process on transcription. This is the default option
        try {
            String hypothesis = prePipeData.getTranscription();
            List<MainHypothesisToken> hypothesisList = generateTokens(hypothesis);
            prePipeData.setMainHypothesis(hypothesisList);
        } catch (MissingDataException e) {
            var msg = "No transcription to process, abborting...";
            throw new PipelineStageException(msg, e);
        }

    }

    private List<MainHypothesisToken> generateTokens(String text) {
        List<CoreLabel> tokenized = tokenizerFactory.getTokenizer(new StringReader(text)).tokenize();
        var position = 0;
        List<MainHypothesisToken> tokenList = new ArrayList<>();
        for (CoreLabel word : tokenized) {
            var token = new MainHypothesisToken(word.word(), position);
            tokenList.add(token);
            position++;
        }
        return tokenList;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.ipd.parse.luna.pipeline.IPipelineStage#getID()
     */
    @Override
    public String getID() {
        return ID;
    }

}
