package ch.xiaobin.subordination.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.xiaobin.subordination.extractor.Utils;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.util.Pair;

public class ComplementClause extends SubordinateClause {
	
	private ComplementType complementType;
	private final Logger logger = LogManager.getLogger();

	public ComplementClause(SubordinateClause basicSubordinateClause) {
		super(basicSubordinateClause);

		//set complement type
		identifyComplementType();
		logger.trace("\tType of complement clause: {}", this.complementType);
	}

	private void identifyComplementType() {
		//ccomp is always object complement by definition
		if(getSgEdge().getRelation().getShortName().equals("ccomp")) {
			complementType = ComplementType.OBJECT_COMPLEMENT;
			return;
		}
		
		//check governor of edge. If it is outside the clause, it is an object complement, otherwise subject,
		//because English is an SVO language
		int governorIdx = getSgEdge().getGovernor().index();
		
//		Pair<Integer, Integer> clauseSpan = getSentSemGraph().yieldSpan(getClauseRoot());
		Pair<Integer, Integer> clauseSpan = Utils.yieldSpan(getSentSemGraph(), getClauseRoot(), getNumTokens());


		
		logger.trace("governorIdx: {}, clauseSpan: {}, {}", governorIdx, clauseSpan.first, clauseSpan.second);
		
		if(governorIdx >= clauseSpan.first() && governorIdx <= clauseSpan.second()) {
			this.complementType = ComplementType.OBJECT_COMPLEMENT;
		} else {
			this.complementType = ComplementType.SUBJECT_COMPLEMENT;
		}

	}

	
	
	public ComplementType getComplementType() {
		return complementType;
	}


	public void setComplementType(ComplementType complementType) {
		this.complementType = complementType;
	}


	public enum ComplementType {
		SUBJECT_COMPLEMENT,
		OBJECT_COMPLEMENT
	}
	
	@Override
	public String toString() {
		return new StringBuilder(super.toString())
				.append("complementType: ").append(complementType).append("\n")
				.toString();
	}
	
	@Override
	String stringifyFields() {
		return new StringBuilder(super.stringifyFields() + ",\n")
				.append("\"").append("complementType").append("\": \"").append(complementType).append("\"\n")
				.toString();
	}
	
}
