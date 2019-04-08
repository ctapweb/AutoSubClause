package ch.xiaobin.subordination.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.xiaobin.subordination.utils.AnimacyDictionaries;
import edu.stanford.nlp.dcoref.Dictionaries;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;
import edu.stanford.nlp.util.Pair;

public class RelativeClause extends SubordinateClause {
	private boolean isRestrictive;
	
	//head noun
	private boolean hasHeadNoun;
	private int headNounBeginIdx;
	private int headNounEndIdx;
	private String headNoun;
	private boolean isHeadNounAnimate;
	
	//head noun role in main clause
	private NPRoles headNounRoleInMainClause;
	private NPRoles headNounRoleInSubClause;
	
	private final Logger logger = LogManager.getLogger();
	
	/**
	 * Constructor with parent type.
	 * 
	 * @param basicSubordinateClause
	 */
	public RelativeClause(SubordinateClause basicSubordinateClause) {
		super(basicSubordinateClause); //copy information into new object
		
		//extract further information related to relative clause
		
		//restrictiveness
		setRestrictiveness();
		logger.trace("\tRestrictiveness: {}", this.isRestrictive);
		
		setHeadNounDetails();
		
		setEmbeddedness(getEmbeddedness() - 1); // relative clauses's embeddedness is different from the other two types of clause
		
		//set animacy, need a better method
		setHeadNounAnimacy();
		logger.trace("\thasHeadNoun: {}, headNounBeginIdx: {}, headNounEndIdx: {}, headNoun: {}, isHeadNounAnimate: {}", 
				this.hasHeadNoun, this.headNounBeginIdx, this.headNounEndIdx, this.headNoun, this.isHeadNounAnimate);
		
		setHeadNounRoles();
		logger.trace("\theadNounRoleInMainClause: {}, headNounRoleInSubClause: {}", 
				this.headNounRoleInMainClause, this.headNounRoleInSubClause);
		
	}
	
	private void setHeadNounAnimacy() {
		//use the coreNLP dictionary to determine anaimacy, probably a better way is to use a ML model, see https://www.aclweb.org/anthology/C18-1001 
		Set<String> animateWords = AnimacyDictionaries.getAnimateWords();
//		logger.trace("animateWords size: {}, has word 'woman': {}, headNoun: {}", animateWords.size(), animateWords.contains("woman"), headNoun);
		if(AnimacyDictionaries.getAnimateWords().contains(headNoun)) {
			this.isHeadNounAnimate = true;
		} else {
			this.isHeadNounAnimate = false;
		}
	}
	
	//https://www.brighthubeducation.com/english-homework-help/32754-the-functions-of-nouns-and-noun-phrases/
	private void setHeadNounRoles() {
		IndexedWord headNoun = getSgEdge().getGovernor();
		Pair<Integer, Integer> clauseSpan = getSentSemGraph().yieldSpan(getClauseRoot());
		
		for(SemanticGraphEdge edge: getSentSemGraph().incomingEdgeIterable(headNoun)) {
			boolean isFromInsideRC = false;
			String relation = getRelationName(edge);

			//see if it is from inside or outside of the RC
			int governorIdx = edge.getGovernor().index();
//			int governorBeginPosition = getSentSemGraph().
//					getNodeByIndex(governorIdx).beginPosition();

			if(governorIdx >= clauseSpan.first() && governorIdx <= clauseSpan.second()) {
				isFromInsideRC = true;
			}
			
//			logger.trace("governorIdx: {}, clauseSpanFirst: {}, clauseSpanSecond: {}", governorIdx, 
//					clauseSpan.first, clauseSpan.second);
//			logger.trace("headNounIncomingEdge: {}, isFromInsideRC: {}", relation, isFromInsideRC);

			if(UniversalEnglishGrammaticalRelations.NOMINAL_SUBJECT.getShortName().equals(relation) ||
					UniversalEnglishGrammaticalRelations.NOMINAL_PASSIVE_SUBJECT.getShortName().equals(relation)) {
				setRole(NPRoles.SUBJECT, isFromInsideRC);
			} else if(UniversalEnglishGrammaticalRelations.DIRECT_OBJECT.getShortName().equals(relation)) {
				setRole(NPRoles.DIRECT_OBJECT, isFromInsideRC);
			} else if(UniversalEnglishGrammaticalRelations.INDIRECT_OBJECT.getShortName().equals(relation)) {
				setRole(NPRoles.INDIRECT_OBJECT, isFromInsideRC);
			} else if(UniversalEnglishGrammaticalRelations.NOMINAL_MODIFIER.getShortName().equals(relation)) {
				setRole(NPRoles.PREPOSITION_COMPLEMENT, isFromInsideRC);
			} else if(UniversalEnglishGrammaticalRelations.APPOSITIONAL_MODIFIER.getShortName().equals(relation)) {
				setRole(NPRoles.APPOSITIVE, isFromInsideRC);
			}
		}

	}
	
	private void setRole(NPRoles role, boolean isFromInsideRC) {
				if(isFromInsideRC) {
					this.headNounRoleInSubClause = role;
				} else {
					this.headNounRoleInMainClause = role;
				}
	}
	
	private void setHeadNounDetails() {
		IndexedWord headNoun = getSgEdge().getGovernor();
		if(headNoun != null) {
			this.hasHeadNoun = true;
			this.headNounBeginIdx = headNoun.beginPosition();
			this.headNounEndIdx = headNoun.endPosition();
			this.headNoun = headNoun.originalText();
		}

		//set head noun roles
		setHeadNounRoles();
	}
	
	private void setRestrictiveness() {

		//if zero relativizer or "that", restrictive
		if(hasSubordinator() == false || "that".equalsIgnoreCase(getSubordinator())) {
			this.isRestrictive = true;
			return;
		}
		
		//if the head noun is personal pronoun or proper noun(s), the clause is nonrestrictive
		IndexedWord headNoun = getSgEdge().getGovernor();
		String headNounPOS = headNoun.get(PartOfSpeechAnnotation.class);
		if(headNounPOS.equals("NNP") || headNounPOS.equals("NNPS") ||
				headNounPOS.equals("PRP")) {
			this.isRestrictive = false;
			return;
		}
		
		//if the head noun is modified by an indefinite determiner like 'a', 'some', or 'any', the clause is restrictive
		for(SemanticGraphEdge edge: getSentSemGraph().outgoingEdgeIterable(headNoun)) {
			String relation = getRelationName(edge);
			if(UniversalEnglishGrammaticalRelations.DETERMINER.getShortName().equals(relation)) {
				String determiner = edge.getDependent().originalText();
				if(determiner.equalsIgnoreCase("a") ||
						determiner.equalsIgnoreCase("an") ||
						determiner.equalsIgnoreCase("some") || 
						determiner.equalsIgnoreCase("any")) {
					this.isRestrictive = true;
					return;
				}
			}
		}
		
		this.isRestrictive = true;
	}
	
	public boolean isRestrictive() {
		return isRestrictive;
	}
	public void setRestrictive(boolean isRestrictive) {
		this.isRestrictive = isRestrictive;
	}
	public boolean isHasHeadNoun() {
		return hasHeadNoun;
	}
	public void setHasHeadNoun(boolean hasHeadNoun) {
		this.hasHeadNoun = hasHeadNoun;
	}
	public int getHeadNounBeginIdx() {
		return headNounBeginIdx;
	}
	public void setHeadNounBeginIdx(int headNounBeginIdx) {
		this.headNounBeginIdx = headNounBeginIdx;
	}
	public int getHeadNounEndIdx() {
		return headNounEndIdx;
	}
	public void setHeadNounEndIdx(int headNounEndIdx) {
		this.headNounEndIdx = headNounEndIdx;
	}
	public boolean isHeadNounAnimate() {
		return isHeadNounAnimate;
	}
	public void setHeadNounAnimate(boolean isHeadNounAnimate) {
		this.isHeadNounAnimate = isHeadNounAnimate;
	}
	public NPRoles getHeadNounRoleInMainClause() {
		return headNounRoleInMainClause;
	}
	public void setHeadNounRoleInMainClause(NPRoles headNounRoleInMainClause) {
		this.headNounRoleInMainClause = headNounRoleInMainClause;
	}
	public NPRoles getHeadNounRoleInSubClause() {
		return headNounRoleInSubClause;
	}
	public void setHeadNounRoleInSubClause(NPRoles headNounRoleInSubClause) {
		this.headNounRoleInSubClause = headNounRoleInSubClause;
	}

	public String getHeadNoun() {
		return headNoun;
	}

	public void setHeadNoun(String headNoun) {
		this.headNoun = headNoun;
	}
	
	@Override
	public String toString() {
		return new StringBuilder(super.toString())
				.append("isRestrictive: ").append(isRestrictive).append("\n")
				.append("hasHeadNoun: ").append(hasHeadNoun).append("\n")
				.append("headNounBeginIdx: ").append(headNounBeginIdx).append("\n")
				.append("headNounEndIdx: ").append(headNounEndIdx).append("\n")
				.append("headNoun: ").append(headNoun).append("\n")
				.append("isHeadNounAnimate: ").append(isHeadNounAnimate).append("\n")
				.append("headNounRoleInMainClause: ").append(headNounRoleInMainClause).append("\n")
				.append("headNounRoleInSubClause: ").append(headNounRoleInSubClause).append("\n")
				.toString();

	}
	
	
	
}
