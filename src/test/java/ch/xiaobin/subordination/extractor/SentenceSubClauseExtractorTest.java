package ch.xiaobin.subordination.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import ch.xiaobin.subordination.annotator.AnnotationPipelineManager;
import ch.xiaobin.subordination.dao.AdjunctClause;
import ch.xiaobin.subordination.dao.AdjunctClause.AdjunctClauseFunction;
import ch.xiaobin.subordination.dao.ClauseType;
import ch.xiaobin.subordination.dao.ComplementClause;
import ch.xiaobin.subordination.dao.NPRoles;
import ch.xiaobin.subordination.dao.RelativeClause;
import ch.xiaobin.subordination.dao.SubordinateClause;
import ch.xiaobin.subordination.dao.ComplementClause.ComplementType;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class SentenceSubClauseExtractorTest {
	AnnotationPipeline annotationPipeline;
//	SentenceSubClauseExtractor sentSubClauseExtractor;

	private final Logger logger = LogManager.getLogger();

	/** Constructor
	 * @throws IOException 
	 * 
	 */
	public SentenceSubClauseExtractorTest() throws IOException {
		//init annotation pipeline
		AnnotationPipelineManager pipelineManager = new AnnotationPipelineManager();
		this.annotationPipeline = pipelineManager.getServerPipeline();
	}
	
	@Test
	public void testRelativeClause() {
		String document = "I went to the show that was very popular.";
//		String document = "The woman, who had long blonde hair, was very pretty.";
		
		//annotate document
		Annotation annotatedDocument = new Annotation(document);
		annotationPipeline.annotate(annotatedDocument);
		
		//get the sentence
		CoreMap annotatedSentence = annotatedDocument.get(SentencesAnnotation.class).get(0);
		
		SentenceSubClauseExtractor sentSubClauseExtractor = new SentenceSubClauseExtractor(annotatedSentence);

		List<SubordinateClause> extractedClauses = sentSubClauseExtractor.extractClauses();
		SubordinateClause subordinateClause = extractedClauses.get(0);
		
		//convert to relative clause
		RelativeClause relativeClause = (RelativeClause) subordinateClause;
		String clauseText = document.substring(subordinateClause.getClauseBeginIdx(), 
				subordinateClause.getClauseEndIdx());
		logger.info("\tclauseText: {}", clauseText);

		assertEquals(ClauseType.RELATIVE, relativeClause.getClauseType());
		assertTrue(relativeClause.isFinite());
		assertEquals(19, relativeClause.getClauseBeginIdx());
		assertEquals(40, relativeClause.getClauseEndIdx());
		assertTrue(relativeClause.hasSubordinator());
		assertEquals(19, relativeClause.getSubordinatorBeginIdx());
		assertEquals(23, relativeClause.getSubordinatorEndIdx());
		assertEquals("that", relativeClause.getSubordinator());
		assertEquals(1, relativeClause.getEmbeddedness());
		assertTrue(relativeClause.isRestrictive());
		assertTrue(relativeClause.isHasHeadNoun());
		assertEquals(14, relativeClause.getHeadNounBeginIdx());
		assertEquals(18, relativeClause.getHeadNounEndIdx());
		assertEquals("show", relativeClause.getHeadNoun());
		assertTrue(relativeClause.isHeadNounAnimate());
		assertEquals(NPRoles.PREPOSITION_COMPLEMENT, relativeClause.getHeadNounRoleInMainClause());
		assertEquals(NPRoles.SUBJECT, relativeClause.getHeadNounRoleInSubClause());
		assertEquals("that was very popular", clauseText);

//					subordinateClause.getClauseEndIdx()));

				
	}
	
	@Test
	public void testSubjComplementClause() {
		String document = "Whoever ate the last piece of pie owes me!";
		
		SubordinateClause subordinateClause = getSubordinateClause(document);

		//convert to complement clause
		ComplementClause complementClause = (ComplementClause) subordinateClause;
		String clauseText = document.substring(subordinateClause.getClauseBeginIdx(), 
				subordinateClause.getClauseEndIdx());
		logger.info("\tclauseText: {}", clauseText);

		assertEquals(ClauseType.COMPLEMENT, complementClause.getClauseType());
		assertTrue(complementClause.isFinite());
		assertEquals(0, complementClause.getClauseBeginIdx());
		assertEquals(33, complementClause.getClauseEndIdx());
		assertFalse(complementClause.hasSubordinator());
		assertEquals(0, complementClause.getSubordinatorBeginIdx());
		assertEquals(0, complementClause.getSubordinatorEndIdx());
		assertNull(complementClause.getSubordinator());
		assertEquals(1, complementClause.getEmbeddedness());
		assertEquals(ComplementType.SUBJECT_COMPLEMENT, complementClause.getComplementType());

//			logger.info(subordinateClause + "\n");

				
	}
	
	@Test
	public void testObjComplementClause() {
		String document = "Zelda knows that Zeke eats leeks.";
		
		SubordinateClause subordinateClause = getSubordinateClause(document);
		
		//convert to complement clause
		ComplementClause complementClause = (ComplementClause) subordinateClause;
		String clauseText = document.substring(subordinateClause.getClauseBeginIdx(), 
				subordinateClause.getClauseEndIdx());
		logger.info("\tclauseText: {}", clauseText);

		assertEquals(ClauseType.COMPLEMENT, complementClause.getClauseType());
		assertTrue(complementClause.isFinite());
		assertEquals(12, complementClause.getClauseBeginIdx());
		assertEquals(32, complementClause.getClauseEndIdx());
		assertTrue(complementClause.hasSubordinator());
		assertEquals(12, complementClause.getSubordinatorBeginIdx());
		assertEquals(16, complementClause.getSubordinatorEndIdx());
		assertEquals("that", complementClause.getSubordinator());
		assertEquals(1, complementClause.getEmbeddedness());
		assertEquals(ComplementType.OBJECT_COMPLEMENT, complementClause.getComplementType());
		assertEquals("that Zeke eats leeks", clauseText);

//			logger.info(subordinateClause + "\n");
	}

	@Test
	public void testAdjunctClause() {
		String document = "They will visit you before they go to the airport.";
		
		SubordinateClause subordinateClause = getSubordinateClause(document);
		String clauseText = document.substring(subordinateClause.getClauseBeginIdx(), 
				subordinateClause.getClauseEndIdx());
		logger.info("\tclauseText: {}", clauseText);

		//convert to adjunct clause
		AdjunctClause adjunctClause = (AdjunctClause) subordinateClause;

		assertEquals(ClauseType.ADJUNT, adjunctClause.getClauseType());
		assertTrue(adjunctClause.isFinite());
		assertEquals(20, adjunctClause.getClauseBeginIdx());
		assertEquals(49, adjunctClause.getClauseEndIdx());
		assertTrue(adjunctClause.hasSubordinator());
		assertEquals(20, adjunctClause.getSubordinatorBeginIdx());
		assertEquals(26, adjunctClause.getSubordinatorEndIdx());
		assertEquals("before", adjunctClause.getSubordinator());
		assertEquals(1, adjunctClause.getEmbeddedness());
		assertEquals(AdjunctClauseFunction.TIME, adjunctClause.getFunction());
		assertEquals("before they go to the airport", clauseText);

//			logger.info(subordinateClause + "\n");
	}
	
	private SubordinateClause getSubordinateClause(String document) {
		//annotate document
		Annotation annotatedDocument = new Annotation(document);
		annotationPipeline.annotate(annotatedDocument);
		
		//get the sentence
		CoreMap annotatedSentence = annotatedDocument.get(SentencesAnnotation.class).get(0);
		
		SentenceSubClauseExtractor sentSubClauseExtractor = new SentenceSubClauseExtractor(annotatedSentence);

		List<SubordinateClause> extractedClauses = sentSubClauseExtractor.extractClauses();
		return extractedClauses.get(0);
	}
}
