package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.indirect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.luna.data.AbstractPipelineData;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PipelineDataCastException;
import edu.kit.ipd.parse.luna.pipeline.IPipelineStage;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.ipd.parse.parsebios.Facade;
import edu.kit.kastel.mcse.ardoco.core.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.PrePipelineData;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token.AbstractHypothesisToken;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token.Chunk;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token.ChunkIOB;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token.MainHypothesisToken;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token.Token;

@MetaInfServices(IPipelineStage.class)
public class TextSNLP implements IPipelineStage {

    private static final Logger logger = LoggerFactory.getLogger(TextSNLP.class);
    private static final String ID = "textSNLP";

    private Facade biosFacade;
    private Stanford stanford;

    @Override
    public void init() {
        stanford = new Stanford();
        biosFacade = new Facade();
    }

    @Override
    public void exec(AbstractPipelineData data) throws PipelineStageException {
        PrePipelineData prePipelineData;
        try {
            prePipelineData = (PrePipelineData) data.asPrePipelineData();
        } catch (PipelineDataCastException e) {
            var msg = "Cannot process on data - PipelineData unreadable";
            throw new PipelineStageException(msg, e);
        }

        // try to process on MainHypothesisTokens
        try {
            List<MainHypothesisToken> hypothesis = prePipelineData.getMainHypothesis();
            List<Token> taggedHypotheses = parse(hypothesis.stream().map(AbstractHypothesisToken::getWord).collect(Collectors.toList()));
            TextSNLP.transferTokenInformation(hypothesis, taggedHypotheses);
            List<List<Token>> result = new ArrayList<>(1);
            result.add(taggedHypotheses);
            prePipelineData.setTaggedHypotheses(result);
        } catch (MissingDataException e) {
            logger.info("No main hypothesis to process...");
        }

    }

    private List<Token> parse(List<String> words) throws PipelineStageException {

        String[] pos = stanford.posTag(words);
        if (pos.length != words.size()) {
            TextSNLP.logger.error("Word Tokens and POS Tokens differ in size");
            throw new PipelineStageException("Word Tokens and POS Tokens differ in size");
        }

        String[] chunks = biosFacade.parse(words.toArray(new String[words.size()]), pos);
        if (chunks.length != words.size()) {
            TextSNLP.logger.error("Word Tokens and CHUNK Tokens differ in size");
            throw new PipelineStageException("Word Tokens and CHUNK Tokens differ in size");
        }

        var instr = new int[words.size()];
        try {
            instr = CalcInstruction.calculateInstructionNumber(words, pos);
        } catch (IllegalArgumentException e) {
            TextSNLP.logger.error("Cannot calculate instruction number, instruction number is set to -1", e);
            Arrays.fill(instr, -1);
        }

        int[] sentenceNumberEndIndices = stanford.sentenceNumberEndIndices(words);

        List<Token> tokenList = TextSNLP.createTokens(words, pos, instr, chunks, sentenceNumberEndIndices);
        stanford.stemAndLemmatize(tokenList);
        return tokenList;
    }

    private static List<Token> createTokens(List<String> words, String[] pos, int[] instr, String[] chunksIOB, int[] sentenceNumberEndIndices) {
        List<Token> result = new ArrayList<>(words.size());
        Chunk[] chunks = new Chunk().convertIOB(chunksIOB);
        var sentenceIndex = 0;
        for (var i = 0; i < words.size(); i++) {
            if (sentenceIndex < sentenceNumberEndIndices.length && i > sentenceNumberEndIndices[sentenceIndex]) {
                sentenceIndex++;
            }
            var tmp = new Token(words.get(i), POSTag.get(pos[i]), ChunkIOB.get(chunksIOB[i]), chunks[i], i, instr[i]);
            tmp.setSentenceNumber(sentenceIndex);
            result.add(tmp);
        }
        return result;
    }

    private static void transferTokenInformation(List<MainHypothesisToken> source, List<Token> sink) throws PipelineStageException {
        if (sink.size() != source.size()) {
            TextSNLP.logger.error("A Hypothesis and a tagged Hypothesis differ in size");
            throw new PipelineStageException("A Hypothesis and a tagged Hypothesis differ in size");
        }
        for (var i = 0; i < source.size(); i++) {
            sink.get(i).consumeHypothesisToken(source.get(i));
        }
    }

    @Override
    public String getID() {
        return TextSNLP.ID;
    }

}
