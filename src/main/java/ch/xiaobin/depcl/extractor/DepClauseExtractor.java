/**
 * 
 */
package ch.xiaobin.depcl.extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.xiaobin.depcl.extractor.DepClause.ClauseType;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.DocIDAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

/**
 * Extracts dependent clauses from annotated sentences.
 * @author xiaobin
 *
 */
public class DepClauseExtractor {

	private AnnotationPipeline annotationPipeline;
	private Annotation document;
	private String documentId;

	private final String EX_MSG_MISSING_SG_ANNO = "Missing semantic graph annotations. Annotate document with a dependency parser first.";
	
	private static final Set<String> HUMAN_REFERENTS = new HashSet<>(Arrays.asList("who", "whom", "whose"));
	private Logger logger = LogManager.getLogger();

	public DepClauseExtractor(AnnotationPipeline annotationPipeline, Annotation document) {
		this.annotationPipeline = annotationPipeline;
		this.document = document;
		this.documentId = document.get(DocIDAnnotation.class);
	}

	private void annotateDocument(Annotation document) {
		annotationPipeline.annotate(document);
	}

	/**
	 * Checks if a document contains dependency parse annotations.
	 * @return
	 */
	public boolean containsSemanticGraphAnnotations(Annotation document) {
		return document.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class) == null ? false: true;
	}

	/**
	 * Checks if a sentence contains dependency parse annotations.
	 * @param sentence
	 * @return
	 */
	public boolean containsSemanticGraphAnnotations(CoreMap sentence) {
		return sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class) == null ? false: true;
	}

	/**
	 * Gets dependent clauses from the annotated document.
	 * @return
	 * @throws DepClauseExtractorException 
	 */
	public List<DepClause> getDepClauses() throws DepClauseExtractorException {
		List<DepClause> depClauseList = new ArrayList<>();

		//check if document annotated
		if(!containsSemanticGraphAnnotations(document)) {
			annotateDocument(document);
		}

		//gets dep clauses from each sentence
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for(CoreMap sentence: sentences) {
			depClauseList.addAll(getDepClauses(sentence));
		}

		return depClauseList;
	}

	/**
	 * Gets dependent clauses information from a sentence.
	 * @param sentence
	 * @return
	 * @throws DepClauseExtractorException 
	 */
	public List<DepClause> getDepClauses(CoreMap sentence) throws DepClauseExtractorException {
		List<DepClause> depClauseList = new ArrayList<>();

		//check if SG annotation exists
//		logger.trace("Checking if semantic graph exist...");
		if(!containsSemanticGraphAnnotations(sentence)) {
			throw new DepClauseExtractorException(EX_MSG_MISSING_SG_ANNO);
		}

//		logger.trace("Getting semantic graph ...");
		SemanticGraph sentSemGraph = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
//		logger.trace("Got semantic graph ...");

		//iterate the edges to find dependent clauses relations
		int i = 0;
		for(SemanticGraphEdge sgEdge: sentSemGraph.edgeIterable()) {
//			logger.trace("iterating {}, edgeName: {}...", i++, sgEdge.getRelation().getShortName());

			switch(sgEdge.getRelation().getShortName()) {
			case DepClause.ClauseType.ACL: //adjectival clause
				depClauseList.add(
						createDepClauseObj(sentence, sgEdge.getDependent(), 
								sgEdge.getGovernor(), DepClause.ClauseType.ACL));
				break;
			case DepClause.ClauseType.ADVCL: //adverbial clause
				depClauseList.add(
						createDepClauseObj(sentence, sgEdge.getDependent(), 
								sgEdge.getGovernor(), DepClause.ClauseType.ADVCL));
				break;
			case DepClause.ClauseType.CCOMP: //complement clause
				depClauseList.add(
						createDepClauseObj(sentence, sgEdge.getDependent(), 
								sgEdge.getGovernor(), DepClause.ClauseType.CCOMP));
				break;
			case DepClause.ClauseType.CSUBJ: //subjectival clause
				depClauseList.add(
						createDepClauseObj(sentence, sgEdge.getDependent(), 
								sgEdge.getGovernor(), DepClause.ClauseType.CSUBJ));
				break;
			case DepClause.ClauseType.CSUBJPASS: //passive subjectival clause
				depClauseList.add(
						createDepClauseObj(sentence, sgEdge.getDependent(), 
								sgEdge.getGovernor(), DepClause.ClauseType.CSUBJPASS));
				break;
				//			default: 
				//				System.out.println(sgEdge.getRelation().getShortName());
			}
//			logger.trace("next iteration: {}.", i);
		}

//		logger.trace("Returning dep clause....");
		return depClauseList;
	}

	private DepClause createDepClauseObj(CoreMap sentenceAnnotation, IndexedWord clauseRoot, IndexedWord governor, String clauseType) {
//		logger.trace("creating dep clause object...");
		int sentenceIdx = sentenceAnnotation.get(SentenceIndexAnnotation.class);
		String sentence = sentenceAnnotation.get(TextAnnotation.class);

		//get the clause span indexes
//		logger.trace("getting clause span...");
		SemanticGraph sentenceSemGraph = 
				sentenceAnnotation.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
//		logger.trace("got clause span...");

//		logger.trace("yeilding span of word: {}", clauseRoot.originalText());
//		Pair<Integer, Integer> clauseSpan = sentenceSemGraph.yieldSpan(clauseRoot);
		Pair<Integer, Integer> clauseSpan = yieldSpan(sentenceSemGraph, clauseRoot);
//		logger.trace("yielt clause span...");

		int beginTokenIdx = clauseSpan.first();
		int endTokenIdx = clauseSpan.second();

//		logger.trace("beginTokenIdx: {}", beginTokenIdx);

		DepClause depClause = new DepClause(documentId, sentenceIdx, sentence, clauseType, beginTokenIdx, endTokenIdx);

		//sets sub conjunction
		for(SemanticGraphEdge outgoingEdge: sentenceSemGraph.outgoingEdgeIterable(clauseRoot)) {
			String edgeName = outgoingEdge.getRelation().getShortName();
			if(edgeName.equals("mark")) {
				IndexedWord subConjunction = outgoingEdge.getDependent();
				depClause.setSubordinatingConjunction(subConjunction.originalText());
				depClause.setSubordinatingConjunctionIdx(subConjunction.index() -1);
			}
		}

		//sets governor and clause root indices
		depClause.setGovernorIdx(governor.index());
		depClause.setClauseRootIdx(clauseRoot.index());

		//if it is a relative clause, update the clause info to include the referent if there is one
		// e.g. `I saw the book [which] you bought.' to include [which] in the clause.
		if(ClauseType.ACL.equals(clauseType)) {
			IndexedWord referent = null;
			for(SemanticGraphEdge outgoingEdge: sentenceSemGraph.outgoingEdgeIterable(governor)) {
				if(outgoingEdge.getRelation().getShortName().equals("ref")) {
					referent = outgoingEdge.getDependent();
					depClause.setBeginTokenIdx(referent.index() -1);

					//set relative pronoun/adverb
					depClause.setReferentIdx(referent.index() - 1);
					depClause.setReferent(referent.originalText());
					
					//also set if referent human
					if(HUMAN_REFERENTS.contains(referent.originalText().toLowerCase())) {
						depClause.setAntecedentHuman(true);
					}
					
					//check if relative clause defining
					int prevTokenIdx = depClause.getBeginTokenIdx() - 1;
					CoreLabel prevToken = 
							sentenceAnnotation.get(TokensAnnotation.class).get(prevTokenIdx);
					if(prevToken.originalText().equals(",")) {
						depClause.setDefining(true);
					}
				}
			}
		}

		//set clause text
		List<CoreLabel> sentTokenList = sentenceAnnotation.get(TokensAnnotation.class);
		int clauseTextBeginOffset = sentTokenList.get(depClause.getBeginTokenIdx()).beginPosition();
		int clauseTextEndOffset = //sentTokenList.get(depClause.getEndTokenIdx()).endPosition();
		depClause.getEndTokenIdx() >= sentTokenList.size() ? 
				sentTokenList.get(sentTokenList.size() - 1).endPosition() + 1:
				sentTokenList.get(depClause.getEndTokenIdx()).endPosition();

//		logger.trace("beginOffset: {}, endOffset: {}", clauseTextBeginOffset, clauseTextEndOffset);

		//parser would sometimes make mistake, in this case, return null
		if(clauseTextBeginOffset >= clauseTextEndOffset) {
			return null;
		}

		String clauseText = document.get(TextAnnotation.class)
				.substring(clauseTextBeginOffset, clauseTextEndOffset - 1);
		depClause.setClauseText(clauseText);


//		depClause.setClauseText(sentenceAnnotation.get(TextAnnotation.class).substring(clauseTextBeginOffset, clauseTextEndOffset));
		
//		List<CoreLabel> clauseTokenList = 
//				sentTokenList.subList(depClause.getBeginTokenIdx(), depClause.getEndTokenIdx());
//
//		StringBuilder strBuilder = new StringBuilder();
//		for(CoreLabel token: clauseTokenList) {
//			strBuilder.append(token.originalText() + " ");
//		}
//		String clauseText = 
//				strBuilder.length() > 0 ? strBuilder.deleteCharAt(strBuilder.length() - 1).toString() : ""; //remove the last space character
//		depClause.setClauseText(clauseText);

		return depClause;
	}

	//This is a make shift solution, because the yieldSpan function in SemanticGraph could have indefinite loop
	public Pair<Integer, Integer> yieldSpan(SemanticGraph sentenceSemGraph, IndexedWord word) {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		Stack<IndexedWord> fringe = new Stack<>();
		fringe.push(word);
		while (!fringe.isEmpty()) {
			IndexedWord parent = fringe.pop();
			min = Math.min(min, parent.index() - 1);
			max = Math.max(max, parent.index());
			for (SemanticGraphEdge edge : sentenceSemGraph.outgoingEdgeIterable(parent)) {
				if (!edge.isExtra()) {
//					logger.trace("Pushing edge dependent: {}", edge.getDependent().originalText());
					fringe.push(edge.getDependent());
				}
			}
//			logger.trace("max:{}, min{}, fringeSize: {}", max, min, fringe.size());
			if(fringe.size() > 1000) {
				break;
			}
		}
		return Pair.makePair(min, max);
	}

}
