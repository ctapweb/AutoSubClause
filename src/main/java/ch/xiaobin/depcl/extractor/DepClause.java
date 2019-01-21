/**
 * 
 */
package ch.xiaobin.depcl.extractor;

/**
 * A class for storing dependent clause information of a sentence.
 * @author xiaobin
 *
 */
public class DepClause {
	private String documentId;
	private int sentenceIdx;
	private String sentence;
	/**
	 * The annotated sentence where the clause is from.
	 */
	private String clauseType;
	private String clauseText;
	private int beginTokenIdx;
	private int endTokenIdx;
	private int subordinatingConjunctionIdx = -1; //default to no subordinating conjunctions
	private String subordinatingConjunction;
	private int referentIdx = -1; //relativ pronoun/adverb
	private String referent;

	//	for relative clause
	private boolean isDefining = false; 
	private boolean isAntecedentHuman = false;

	int governorIdx;
	int clauseRootIdx;

	public DepClause(String documentId, int sentenceIdx, String sentence, String clauseType, int beginTokenIdx, int endTokenIdx) {
		this.documentId = documentId;
		this.sentenceIdx = sentenceIdx;
		this.sentence = sentence;
		this.clauseType = clauseType;
		this.beginTokenIdx = beginTokenIdx;
		this.endTokenIdx = endTokenIdx;
	}

	public int getReferentIdx() {
		return referentIdx;
	}

	public void setReferentIdx(int referentIdx) {
		this.referentIdx = referentIdx;
	}

	public String getReferent() {
		return referent;
	}

	public void setReferent(String referent) {
		this.referent = referent;
	}

	public String getClauseText() {
		return clauseText;
	}

	public void setClauseText(String clauseText) {
		this.clauseText = clauseText;
	}

	public String getClauseType() {
		return clauseType;
	}

	public void setClauseType(String clauseType) {
		this.clauseType = clauseType;
	}

	public int getBeginTokenIdx() {
		return beginTokenIdx;
	}

	public void setBeginTokenIdx(int beginTokenIdx) {
		this.beginTokenIdx = beginTokenIdx;
	}

	public int getEndTokenIdx() {
		return endTokenIdx;
	}

	public void setEndTokenIdx(int endTokenIdx) {
		this.endTokenIdx = endTokenIdx;
	}


	public String getSubordinatingConjunction() {
		return subordinatingConjunction;
	}

	public void setSubordinatingConjunction(String subordinatingConjunction) {
		this.subordinatingConjunction = subordinatingConjunction;
	}

	public void setSubordinatingConjunctionIdx(int subordinatingConjunctionIdx) {
		this.subordinatingConjunctionIdx = subordinatingConjunctionIdx;
	}

	public boolean isDefining() {
		return isDefining;
	}

	public void setDefining(boolean isDefining) {
		this.isDefining = isDefining;
	}

	public boolean isAntecedentHuman() {
		return isAntecedentHuman;
	}

	public void setAntecedentHuman(boolean isAntecedentHuman) {
		this.isAntecedentHuman = isAntecedentHuman;
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

	public int getGovernorIdx() {
		return governorIdx;
	}

	public void setGovernorIdx(int governorIdx) {
		this.governorIdx = governorIdx;
	}

	public int getClauseRootIdx() {
		return clauseRootIdx;
	}

	public void setClauseRootIdx(int clauseRootIdx) {
		this.clauseRootIdx = clauseRootIdx;
	}


	public int getSubordinatingConjunctionIdx() {
		return subordinatingConjunctionIdx;
	}


	public String getSentence() {
		return sentence.replaceAll("\\n", " ");
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public class ClauseType {
		public static final String ADVCL = "advcl";
		public static final String CCOMP = "ccomp";
		public static final String ACL = "acl:relcl";
		public static final String CSUBJ = "csubj";
		public static final String CSUBJPASS = "csubjpass";
	}

}
