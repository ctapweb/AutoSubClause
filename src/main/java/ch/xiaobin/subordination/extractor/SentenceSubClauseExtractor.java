package ch.xiaobin.subordination.extractor;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilterFactory;

import ch.xiaobin.subordination.dao.AdjunctClause;
import ch.xiaobin.subordination.dao.ClauseType;
import ch.xiaobin.subordination.dao.ComplementClause;
import ch.xiaobin.subordination.dao.RelativeClause;
import ch.xiaobin.subordination.dao.SubordinateClause;
import edu.stanford.nlp.ling.CoreAnnotations.DocIDAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreNLPProtos.IndexedWordOrBuilder;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

/**
 * Two ways to use the extractor:
 * 		1. SentenceSubClauseExtractor extractor = new SentenceSubClauseExtractor(annotatedSentence);
 * 			extractor.extractClauses;
 * 		2. SentenceSubClauseExtractor extractor = new SentenceSubClauseExtractor();
 * 			extractor.extractClauses(annotatedSentence);
 * 
 * @author xiaobin
 *
 */
public class SentenceSubClauseExtractor implements SubClauseExtractor {
	
	private int sentenceIdx;
	private CoreMap annotatedSentence = null;
	private SemanticGraph sentSemGraph;
	private Logger logger = LogManager.getLogger();
	
	//empty constructor
//	public SentenceSubClauseExtractor() {}

	public SentenceSubClauseExtractor(int sentenceIdx, CoreMap annotatedSentence) {
		this.sentenceIdx = sentenceIdx;
		this.annotatedSentence = annotatedSentence;
		//get the parse
		this.sentSemGraph = annotatedSentence.get(
				SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
	}
	
	public SentenceSubClauseExtractor(CoreMap annotatedSentence) {
		this(annotatedSentence.get(SentenceIndexAnnotation.class), annotatedSentence);
	}

	@Override
	public List<SubordinateClause> extractClauses() {
		logger.info("Extracting subordinate clause(s) for sentence {}: {}", sentenceIdx, annotatedSentence.get(TextAnnotation.class));

		List<SubordinateClause> subClauses = new ArrayList<>();
		//check if sentence annotated
		if(annotatedSentence == null) {
			throw logger.throwing(new NullPointerException("No sentence to extract clauses from."));
		}
		
		if(!isSentenceAnnotated(annotatedSentence)) {
			throw logger.throwing(new ExtractorException("Sentence not annotated/dependency parsed."));
		}
		

		//find subordinate relations from dependency parsed results
		List<SemanticGraphEdge> sgEdges = findSubordinateEdges(sentSemGraph);
		
		logger.info("Found {} subordinate edges from dependency parse graph.", sgEdges.size());
		
		//for each found relation, do further extraction
		for(SemanticGraphEdge sgEdge: sgEdges) {
			logger.trace("Extracting basic subordinate clause info for edge {}...", sgEdge.getRelation().getShortName());
			//extract basic subordinate information
			//Basic subordinate clause objects contain elements that are common to all subordinate clause types.
			SubordinateClause basicSubClause = new SubordinateClause(sentenceIdx, annotatedSentence, sentSemGraph, sgEdge);
			
			//get document id from annotation
			basicSubClause.setDocumentId(annotatedSentence.get(DocIDAnnotation.class));
			
			//extract information of specific clause type
			logger.trace("Extracting info for specific clause type...");
			switch(basicSubClause.getClauseType()) {
			case RELATIVE:
				RelativeClause relativeClause = new RelativeClause(basicSubClause);
				subClauses.add(relativeClause);
				break;
			case ADJUNCT:
				AdjunctClause adjunctClause = new AdjunctClause(basicSubClause);
				subClauses.add(adjunctClause);
				break;
			case COMPLEMENT:
				ComplementClause complementClause = new ComplementClause(basicSubClause);
				subClauses.add(complementClause);
				break;
			}
		}
		
		return subClauses;
	}
	
	public List<SubordinateClause> extractClauses(CoreMap annotatedSentence) {
		this.annotatedSentence = annotatedSentence;
		return extractClauses();
	}
	
	private boolean isSentenceAnnotated(CoreMap sentence) {
		return sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class) == null ? false: true;
	}
	
	private List<SemanticGraphEdge> findSubordinateEdges(SemanticGraph sentSemGraph) {
		logger.trace("Looking for subordinate edges...");
		List<SemanticGraphEdge> subordinateEdges = new ArrayList<>();
		
		//iterate the edges to find dependent clauses relations
		for(SemanticGraphEdge sgEdge: sentSemGraph.edgeIterable()) {
			if(TargetRelations.TARGET_RELATIONS.contains(sgEdge.getRelation().getShortName())) {
				subordinateEdges.add(sgEdge);
			}
		}
		return subordinateEdges;
	}

}
