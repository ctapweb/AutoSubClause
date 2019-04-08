package ch.xiaobin.subordination.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		//check governor of edge. If it is outside the clause, it is an object complement, otherwise subject,
		//because English is an SVO language
		int governorIdx = getSgEdge().getGovernor().index();
		
		Pair<Integer, Integer> clauseSpan = getSentSemGraph().yieldSpan(getClauseRoot());
		
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
	
	
}
