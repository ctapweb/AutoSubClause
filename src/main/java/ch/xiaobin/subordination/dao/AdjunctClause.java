package ch.xiaobin.subordination.dao;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.time.SUTime.Time;

public class AdjunctClause extends SubordinateClause {
	
	//function of clause, e.g. temporal, modal, instrumental...
	private AdjunctClauseFunction function;
	
	private List<String> TIME_SUBORDINATORS = 
			Arrays.asList("when", "before", "after", "since", "while", "as", "till", "until");
	private List<String> PLACE_SUBORDINATORS = 
			Arrays.asList("where", "wherever", "anywhere", "everywhere");
	private List<String> CONDITION_SUBORDINATORS = 
			Arrays.asList("if", "unless", "lest", "provided");
	private List<String> REASON_SUBORDINATORS = 
			Arrays.asList("because", "since", "as", "given");
	private List<String> CONCESSION_SUBORDINATORS = 
			Arrays.asList("although", "though");
	private List<String> PURPOSE_SUBORDINATORS = 
			Arrays.asList("so", "to");
	private List<String> COMPARISON_SUBORDINATORS = 
			Arrays.asList("than");
	private List<String> MANNER_SUBORDINATORS = 
			Arrays.asList("like", "way");
	private List<String> RESULTS_SUBORDINATORS = 
			Arrays.asList("so", "such");

	private final Logger logger = LogManager.getLogger();
	
	public AdjunctClause(SubordinateClause anotherSubordinateClause) {
		super(anotherSubordinateClause);

		assignFunction();
		logger.trace("\tFunction of adjunct clause: {}", this.function);

	}

	private void assignFunction() {
		String subordinator = getSubordinator().toLowerCase();
		if(TIME_SUBORDINATORS.contains(subordinator)) {
			this.function = AdjunctClauseFunction.TIME;
		} else if(PLACE_SUBORDINATORS.contains(subordinator)) {
			this.function = AdjunctClauseFunction.PLACE;
		} else if(CONDITION_SUBORDINATORS.contains(subordinator)) {
			this.function = AdjunctClauseFunction.CONDITION;
		} else if(REASON_SUBORDINATORS.contains(subordinator))	{
			this.function = AdjunctClauseFunction.REASON;
		} else if(CONCESSION_SUBORDINATORS.contains(subordinator))	{
			this.function = AdjunctClauseFunction.CONCESSION;
		} else if(PURPOSE_SUBORDINATORS.contains(subordinator)) {
			this.function = AdjunctClauseFunction.PURPOSE;
		} else if(COMPARISON_SUBORDINATORS.contains(subordinator)) {
			this.function = AdjunctClauseFunction.COMPARISION;
		} else if(MANNER_SUBORDINATORS.contains(subordinator)) {
			this.function = AdjunctClauseFunction.MANNER;
		} else if(RESULTS_SUBORDINATORS.contains(subordinator)) {
			this.function = AdjunctClauseFunction.RESULTS;
		}
	}


	public AdjunctClauseFunction getFunction() {
		return function;
	}

	public void setFunction(AdjunctClauseFunction function) {
		this.function = function;
	}


	public enum AdjunctClauseFunction {
		TIME,
		CONDITION,
		PURPOSE,
		REASON,
		CONCESSION,
		PLACE,
		COMPARISION,
		MANNER,
		RESULTS
	}

	@Override
	public String toString() {
		return new StringBuilder(super.toString())
				.append("function: ").append(function).append("\n")
				.toString();
	}
	
}
