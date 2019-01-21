package ch.xiaobin.depcl.extractor;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import ch.xiaobin.depcl.annotator.AnnotationPipelineManager;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;

/**
 * To run this test, coreNLP server should first be started.
 * Set the server detail in the variables.
 * @author xiaobin
 *
 */
public class DepClauseExtractorTest {
	private AnnotationPipeline pipeline;
	private AnnotationPipelineManager pipelineManager;
	private final Logger logger = LogManager.getLogger();
	
	public DepClauseExtractorTest() throws IOException {
		pipelineManager = new AnnotationPipelineManager();
		pipeline = pipelineManager.getServerPipeline();
//		pipeline = pipelineManager.getLocalPipeLine();
	}

	@Test
	public void testAdvcl() throws DepClauseExtractorException {
		logger.trace("Testing advcl extraction...");
		String sentence = "The door opened because the man pushed it.";
		String clause = "because the man pushed it";
		String clauseType = DepClause.ClauseType.ADVCL;
		int clBeginIdx = 3;
		int clEndIdx = 8;
		int subConjIdx = 3;
		String subConjunction = "because";
		
		checkDepClause(sentence, clause, clBeginIdx, clEndIdx, subConjIdx, subConjunction, clauseType);
		
	}

	private void checkDepClause(String sentence, 
			String clause, int clBeginIdx, int clEndIdx, int subConjIdx,
			String subConjunction, String clauseType) throws DepClauseExtractorException {

		logger.trace("Analyzing sentence: {}", sentence);
		Annotation document = new Annotation(sentence);
		DepClauseExtractor depClauseExtractor = new DepClauseExtractor(pipeline, document);
		List<DepClause> depClauseList = depClauseExtractor.getDepClauses();
		
		if(depClauseList.isEmpty()) {
			logger.trace("\tNo dependent clause found.");
			return;
		}

		DepClause depClause = depClauseList.get(0);

		logger.trace("\tFound dependent clause: {}, {}", depClause.getClauseType(), depClause.getClauseText());
		
		int sentenceIndex = depClause.getSentenceIdx();
//				depClause.getSentenceAnnotation().get(SentenceIndexAnnotation.class);
		logger.trace("\tIndex of sentence: {}", sentenceIndex);
		
		//check that there is only one dependent clause
		Assert.assertEquals(depClauseList.size(), 1);
		Assert.assertEquals(sentence, depClause.getSentence());
		Assert.assertEquals(clause, depClause.getClauseText());
		Assert.assertEquals(clBeginIdx, depClause.getBeginTokenIdx());
		Assert.assertEquals(clEndIdx, depClause.getEndTokenIdx());
		Assert.assertEquals(subConjIdx, depClause.getSubordinatingConjunctionIdx());
		Assert.assertEquals(subConjunction, depClause.getSubordinatingConjunction());
		Assert.assertEquals(clauseType, depClause.getClauseType());
		logger.trace("\tTesting passed!");
	}

	@Test
	public void testCcomp() throws DepClauseExtractorException {
		logger.trace("Testing ccomp extraction...");
		String sentence = "I wondered whether the homework was necessary.";
		String clause = "whether the homework was necessary";
		int clBeginIdx = 2;
		int clEndIdx = 7;
		int subConjIdx = 2;
		String subConjunction = "whether";
		String clauseType = DepClause.ClauseType.CCOMP;
		
		checkDepClause(sentence, clause, clBeginIdx, clEndIdx, subConjIdx, subConjunction, clauseType);
	}

	@Test
	public void testAcl() throws DepClauseExtractorException {
		logger.trace("Testing acl:relcl extraction...");
		String sentence = "I went to the show that was very popular.";
		String clause = "that was very popular";
		int clBeginIdx = 5;
		int clEndIdx = 9;
		int subConjIdx = -1;
		String subConjunction = null;
		String clauseType = DepClause.ClauseType.ACL;
		
		checkDepClause(sentence, clause, clBeginIdx, clEndIdx, subConjIdx, subConjunction, clauseType);
	}
	
	@Test
	public void testAcl1() throws DepClauseExtractorException {
		logger.trace("Testing another acl:relcl extraction...");
		String sentence = "I saw the book which you bought.";
		String clause = "which you bought";
		int clBeginIdx = 4;
		int clEndIdx = 7;
		int subConjIdx = -1;
		String subConjunction = null;
		String clauseType = DepClause.ClauseType.ACL;
		
		checkDepClause(sentence, clause, clBeginIdx, clEndIdx, subConjIdx, subConjunction, clauseType);
	}

	@Test
	public void testCSubj() throws DepClauseExtractorException {
		logger.trace("Testing csubj extraction...");
		String sentence = "Whoever ate the last piece of pie owes me!";
		String clause = "Whoever ate the last piece of pie";
		int clBeginIdx = 0;
		int clEndIdx = 7;
		int subConjIdx = -1;
		String subConjunction = null;
		String clauseType = DepClause.ClauseType.CSUBJ;
		
		checkDepClause(sentence, clause, clBeginIdx, clEndIdx, subConjIdx, subConjunction, clauseType);
	}
	
}
