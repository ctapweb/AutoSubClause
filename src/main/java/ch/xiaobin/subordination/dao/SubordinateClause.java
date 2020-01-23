/**
 * 
 */
package ch.xiaobin.subordination.dao;

import java.util.Stack;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.xiaobin.subordination.extractor.TargetRelations;
import ch.xiaobin.subordination.extractor.Utils;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

/**
 * Abstract class for storing subordinate clause information.
 * Actual subordinate clauses extend this class.
 * @author xiaobin
 *
 */
public class SubordinateClause {
	private String documentId;
	private int sentenceIdx;
	private transient CoreMap annotatedSentence;
	private transient SemanticGraph sentSemGraph; //semantic graph of sentence
	private transient SemanticGraphEdge sgEdge; //the edge that identify the clause
	private transient IndexedWord clauseRoot;
	private String sgEdgeRelationName;

	//if the clause is finite or not
	//finite clauses are clauses with inflected verbs, otherwise nonfinite, e.g.
	// I want [to emigrate to Australia]. nonfinite
	private boolean isFinite;

	//type of subordinate clause
	private ClauseType clauseType;

	//begin and end Idx of clause in sentence string
	private int clauseBeginIdx;
	private int clauseEndIdx;

	//subordinator
	private boolean hasSubordinator;
	private int subordinatorBeginIdx;
	private int subordinatorEndIdx;
	private String subordinator;

	//level of embeddedness, main clause at level 0
	private int embeddedness;
	
	private transient final Logger logger = LogManager.getLogger();
	
	//empty constructor
	public SubordinateClause() {
	}

	public SubordinateClause(SubordinateClause anotherSubordinateClause) {
		this.documentId = anotherSubordinateClause.documentId;
		this.sentenceIdx = anotherSubordinateClause.sentenceIdx;
		this.annotatedSentence = anotherSubordinateClause.annotatedSentence;
		this.sentSemGraph = anotherSubordinateClause.sentSemGraph;
		this.sgEdge = anotherSubordinateClause.sgEdge;
		this.clauseRoot = anotherSubordinateClause.clauseRoot;
		this.sgEdgeRelationName = anotherSubordinateClause.sgEdgeRelationName;
		this.isFinite = anotherSubordinateClause.isFinite;
		this.clauseType = anotherSubordinateClause.clauseType;
		this.clauseBeginIdx = anotherSubordinateClause.clauseBeginIdx;
		this.clauseEndIdx = anotherSubordinateClause.clauseEndIdx;
		this.hasSubordinator = anotherSubordinateClause.hasSubordinator;
		this.subordinatorBeginIdx = anotherSubordinateClause.subordinatorBeginIdx;
		this.subordinatorEndIdx = anotherSubordinateClause.subordinatorEndIdx;
		this.subordinator = anotherSubordinateClause.subordinator;
		this.embeddedness = anotherSubordinateClause.embeddedness;
	}

	/**
	 * 
	 * @param sentenceIdx
	 * @param sentSemGraph the sentence from which the clause is extracted
	 * @param sgEdge the edge identifying the clause
	 */
	public SubordinateClause(int sentenceIdx, CoreMap annotatedSentence, SemanticGraph sentSemGraph, SemanticGraphEdge sgEdge) {
		this.sentenceIdx = sentenceIdx;
		this.annotatedSentence = annotatedSentence;
		this.sentSemGraph = sentSemGraph;
		this.sgEdge = sgEdge;
		this.clauseRoot = sgEdge.getDependent();
		this.sgEdgeRelationName = getRelationName(sgEdge);
		
		//identify clause type
		identifyClauseType();
		logger.trace("\tIdentified clause type: {}", this.clauseType);
		
		//identify clause finiteness
		identifyFiniteness();
		logger.trace("\tIdentified finiteness: {}", this.isFinite);
		
		//set clause begin and end
		setClauseSpan();
		logger.trace("\tIdentified clause span: {}, {}", this.clauseBeginIdx, this.clauseEndIdx);

		//set subordinator, which is 'mark' in adverbial clauses and complement clause, but 'ref' in relative clause
		identifySubordinator();
		logger.trace("\tIdentified subordinator: {}, beginIdx: {}, endIdx: {}, subordinator: {}", 
				this.hasSubordinator, this.subordinatorBeginIdx, this.subordinatorEndIdx, this.subordinator);
		
		//set level of embeddedness
		countEmbeddedness();
		logger.trace("\tIdentified embeddedness: {}", this.embeddedness);
		
	}
	
	private void countEmbeddedness() {
//		IndexedWord sentRoot = sentSemGraph.getFirstRoot();
//		int levels = sentSemGraph.commonAncestor(sentRoot, clauseRoot);
//		this.embeddedness = levels;
		
		this.embeddedness = sentSemGraph.getPathToRoot(clauseRoot).size();
		
		
	}

	private void identifySubordinator() {
		//for relative clauses, find the "referent"
		if(this.clauseType == ClauseType.RELATIVE) {
			IndexedWord headNoun = sgEdge.getGovernor();
			for(SemanticGraphEdge outEdge: sentSemGraph.outgoingEdgeIterable(headNoun)) {
				if(UniversalEnglishGrammaticalRelations.REFERENT.getShortName().equals(getRelationName(outEdge))) {
					IndexedWord subordinatorIndexedWord = outEdge.getDependent();
					setSubordinatorDetails(subordinatorIndexedWord);
					return;
				}
			}
			
		}

		//for adverbial and complement clauses, find the "mark"
		for(SemanticGraphEdge outEdge: sentSemGraph.outgoingEdgeIterable(clauseRoot)) {
			String outEdgeRel = getRelationName(outEdge);

			if(outEdgeRel.equals(UniversalEnglishGrammaticalRelations.MARKER.getShortName())) { 
				IndexedWord subordinatorIndexedWord = outEdge.getDependent();
				setSubordinatorDetails(subordinatorIndexedWord);
				return;
			}
			
		}

		//no subordinator
		this.hasSubordinator = false;
		
	}
	
	private void setSubordinatorDetails(IndexedWord subordinatorIndexedWord) {
				this.hasSubordinator = true;
				this.subordinatorBeginIdx = subordinatorIndexedWord.beginPosition();
				this.subordinatorEndIdx = subordinatorIndexedWord.endPosition();
				this.subordinator = subordinatorIndexedWord.originalText();
	}

	private void setClauseSpan() {
		
//		Pair<Integer, Integer> clauseSpan = sentSemGraph.yieldSpan(clauseRoot);
		Pair<Integer, Integer> clauseSpan = Utils.yieldSpan(sentSemGraph, clauseRoot, getNumTokens());

//		logger.trace("clauseSpan: {}, {}", clauseSpan.first, clauseSpan.second);
//		logger.trace("get Node: {}, {}", 1, sentSemGraph.getNodeByIndex(1).originalText());
		int first;
		if(getClauseType().equals(ClauseType.RELATIVE)) {
			first = clauseSpan.first;
		} else {
			first = clauseSpan.first + 1;
		}

		//span may result in 0 index, which will result in not getting the node
		first = first == 0 ? 1: first; 
		int beginIdx = sentSemGraph.getNodeByIndex(first).beginPosition();
		int endIdx = sentSemGraph.getNodeByIndex(clauseSpan.second()).endPosition();
		this.clauseBeginIdx = beginIdx;
		this.clauseEndIdx = endIdx;
	}
	
	//gets number of tokens of the sentence
	int getNumTokens() {
		return annotatedSentence.get(TokensAnnotation.class).size();
	}
	
	private void identifyClauseType() {
		if(TargetRelations.ACL.equals(sgEdgeRelationName)) {
			this.clauseType = ClauseType.RELATIVE;
		} else if(TargetRelations.ADVCL.equals(sgEdgeRelationName)) {
			this.clauseType = ClauseType.ADJUNCT;
		} else if(TargetRelations.CCOMP.equals(sgEdgeRelationName) ||
				TargetRelations.CSUBJ.equals(sgEdgeRelationName) || 
				TargetRelations.CSUBJPASS.equals(sgEdgeRelationName) ||
				TargetRelations.XCOMP.equals(sgEdgeRelationName)
				) {
			this.clauseType = ClauseType.COMPLEMENT;
		} 	
	}
	
	//check if the sub clause finite
	// Finite clauses are clauses that contain verbs which show tense. Otherwise they are nonfinite.
	// some examples:
	//    I had something to eat [before leaving]. 
	//    [After having spent six hours at the hospital], they eventually came home.
	//    [Helped by local volunteers], staff at the museum have spent many years sorting and cataloguing more than 100,000 photographs.
	//    He left the party and went home, [not having anyone to talk to].
	//    The person to ask [about going to New Zealand] is Beck.
	//    You have to look at the picture really carefully [in order to see all the detail].
	private void identifyFiniteness() {
		System.out.println("identifying finiteness....");
		//xcomp is nonfinite by definition
		if(sgEdgeRelationName.equals(TargetRelations.XCOMP)) {
			isFinite = false;
			return;
		}

		//if the verb follows TO or a preposition, it is nonfinite 
		//the verb is the root of the clause
		int idxWordBeforeVerb = clauseRoot.index() - 1;
		IndexedWord wordBeforeVerb = sentSemGraph.getNodeByIndexSafe(idxWordBeforeVerb);
		String posVerb = clauseRoot.get(PartOfSpeechAnnotation.class);
		if(wordBeforeVerb == null) {
			if(posVerb.equals("VBG") || posVerb.equals("VBN")) {
				isFinite = false;
			} else {
				isFinite =true;
			}
			//not VBG or VBN, then finite
			return;
		}

		String posWordBeforeVerb = wordBeforeVerb.get(PartOfSpeechAnnotation.class);
		if(posWordBeforeVerb.equals("IN") || posWordBeforeVerb.equals("TO")) {
			isFinite = false;
			return;
		}

		//if verb is gerund (VBG), it must have an aux, otherwise nonfinite
		if(posVerb.equals("VBG")) {
			boolean hasAux = false;
			//check if there is aux
			for(SemanticGraphEdge outgoingEdge: sentSemGraph.outgoingEdgeIterable(clauseRoot)) {
				String rel = getRelationName(outgoingEdge);
				if(rel.equals("aux")) {
					hasAux= true;
				}
			}
			if(!hasAux) {
				// nonfinite
				isFinite = false;

				return;
			}
		}

		//if verb is past participle (VBN), it must have aux/auxpass which is not VBGs, otherwise non-finite
		if(posVerb.equals("VBN")) {
			boolean hasVBGAux = false;
			//check if there is aux that is not in gerund form
			for(SemanticGraphEdge outgoingEdge: sentSemGraph.outgoingEdgeIterable(clauseRoot)) {
				String rel = getRelationName(outgoingEdge);
				if(rel.equals("aux") || rel.equals("auxpass")) {
					//get pos of aux
					IndexedWord aux = outgoingEdge.getDependent();
					String auxPOS = aux.get(PartOfSpeechAnnotation.class);
					if(auxPOS.equals("VBG")) {
						hasVBGAux= true;
					}
				}
			}
			if(hasVBGAux) {
				isFinite = false; // nonfinite
				return; 
			}

		}

		isFinite = true;
	}

	String getRelationName(SemanticGraphEdge sgEdge) {
		return sgEdge.getRelation().getShortName();
	}

	public String getSentenceText() {
		return annotatedSentence.get(TextAnnotation.class);
	}

	public SemanticGraphEdge getSgEdge() {
		return sgEdge;
	}

	public void setSgEdge(SemanticGraphEdge sgEdge) {
		this.sgEdge = sgEdge;
	}

	public String getDocumentId() {
		return documentId;
	}
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}
	public int getSentenceIdx() {
		return sentenceIdx;
	}
	public void setSentenceIdx(int sentenceIdx) {
		this.sentenceIdx = sentenceIdx;
	}
	public boolean isFinite() {
		return isFinite;
	}
	public void setFinite(boolean isFinite) {
		this.isFinite = isFinite;
	}
	public ClauseType getClauseType() {
		return clauseType;
	}
	public void setClauseType(ClauseType clauseType) {
		this.clauseType = clauseType;
	}
	public int getClauseBeginIdx() {
		return clauseBeginIdx;
	}
	public void setClauseBeginIdx(int clauseBeginIdx) {
		this.clauseBeginIdx = clauseBeginIdx;
	}
	public int getClauseEndIdx() {
		return clauseEndIdx;
	}
	public void setClauseEndIdx(int clauseEndIdx) {
		this.clauseEndIdx = clauseEndIdx;
	}
	public boolean hasSubordinator() {
		return hasSubordinator;
	}
	public void setHasSubordinator(boolean hasSubordinator) {
		this.hasSubordinator = hasSubordinator;
	}
	public int getSubordinatorBeginIdx() {
		return subordinatorBeginIdx;
	}
	public void setSubordinatorBeginIdx(int subordinatorBeginIdx) {
		this.subordinatorBeginIdx = subordinatorBeginIdx;
	}
	public int getSubordinatorEndIdx() {
		return subordinatorEndIdx;
	}
	public void setSubordinatorEndIdx(int subordinatorEndIdx) {
		this.subordinatorEndIdx = subordinatorEndIdx;
	}
	public int getEmbeddedness() {
		return embeddedness;
	}
	public void setEmbeddedness(int embeddedness) {
		this.embeddedness = embeddedness;
	}

	public String getSubordinator() {
		return subordinator;
	}

	public void setSubordinator(String subordinator) {
		this.subordinator = subordinator;
	}

	public SemanticGraph getSentSemGraph() {
		return sentSemGraph;
	}

	public void setSentSemGraph(SemanticGraph sentSemGraph) {
		this.sentSemGraph = sentSemGraph;
	}

	public IndexedWord getClauseRoot() {
		return clauseRoot;
	}

	public void setClauseRoot(IndexedWord clauseRoot) {
		this.clauseRoot = clauseRoot;
	}

	public String getSgEdgeRelationName() {
		return sgEdgeRelationName;
	}

	public void setSgEdgeRelationName(String sgEdgeRelationName) {
		this.sgEdgeRelationName = sgEdgeRelationName;
	}
	
	@Override
	public String toString() {
		return new StringBuilder("")
				.append("documentId: ").append(documentId).append("\n")
				.append("sentenceIdx: ").append(sentenceIdx).append("\n")
				.append("clauseType: ").append(clauseType).append("\n")
				.append("isFinite: ").append(isFinite).append("\n")
				.append("clauseBeginIdx: ").append(clauseBeginIdx).append("\n")
				.append("clauseEndIdx: ").append(clauseEndIdx).append("\n")
				.append("hasSubordinator: ").append(hasSubordinator).append("\n")
				.append("subordinatorBeginIdx: ").append(subordinatorBeginIdx).append("\n")
				.append("subordinatorEndIdx: ").append(subordinatorEndIdx).append("\n")
				.append("subordinator: ").append(subordinator).append("\n")
				.append("embeddedness: ").append(embeddedness).append("\n")
				.toString();
	}
	
	public String toJSONString() {
		return new StringBuilder("{\n")
				.append(stringifyFields())
				.append("}")
				.toString();
	}
	
	//prepare for JSON output
	String stringifyFields() {
		return new StringBuilder("")
				.append("\"").append("documentId").append("\": \"").append(documentId).append("\",\n")
				.append("\"").append("sentenceIdx").append("\": ").append(sentenceIdx).append(",\n")
//				.append("\"").append("sentenceText").append("\": \"").append(getSentenceText().replaceAll("\\\"", "\\\\\"")).append("\",\n")
				.append("\"").append("sentenceText").append("\": \"").append(StringEscapeUtils.escapeJson(getSentenceText())).append("\",\n")
				.append("\"").append("clauseType").append("\": \"").append(clauseType).append("\",\n")
				.append("\"").append("isFinite").append("\": ").append(isFinite).append(",\n")
				.append("\"").append("clauseBeginIdx").append("\": ").append(clauseBeginIdx).append(",\n")
				.append("\"").append("clauseEndIdx").append("\": ").append(clauseEndIdx).append(",\n")
				.append("\"").append("hasSubordinator").append("\": ").append(hasSubordinator).append(",\n")
				.append("\"").append("subordinatorBeginIdx").append("\": ").append(subordinatorBeginIdx).append(",\n")
				.append("\"").append("subordinatorEndIdx").append("\": ").append(subordinatorEndIdx).append(",\n")
				.append("\"").append("subordinator").append("\": \"").append(subordinator).append("\",\n")
				.append("\"").append("embeddedness").append("\": ").append(embeddedness)
				.toString();
	}
	
	 
}
