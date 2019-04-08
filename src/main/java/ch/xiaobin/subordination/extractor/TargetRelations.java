/**
 * 
 */
package ch.xiaobin.subordination.extractor;

import java.util.HashSet;
import java.util.Set;

import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;

/**
 * Lists the target relations of dependency parsing. 
 * @author xiaobin
 *
 */
public class TargetRelations {
	public final static String ACL = UniversalEnglishGrammaticalRelations.RELATIVE_CLAUSE_MODIFIER.getShortName();
	public final static String ADVCL = UniversalEnglishGrammaticalRelations.ADV_CLAUSE_MODIFIER.getShortName();
	public final static String CCOMP = UniversalEnglishGrammaticalRelations.CLAUSAL_COMPLEMENT.getShortName();
	public final static String CSUBJ = UniversalEnglishGrammaticalRelations.CLAUSAL_SUBJECT.getShortName();
	public final static String CSUBJPASS = UniversalEnglishGrammaticalRelations.CLAUSAL_PASSIVE_SUBJECT.getShortName();
	public final static String XCOMP = UniversalEnglishGrammaticalRelations.XCLAUSAL_COMPLEMENT.getShortName();
	public final static Set<String> TARGET_RELATIONS = new HashSet<>();
	
	static {
		TARGET_RELATIONS.add(ACL);
		TARGET_RELATIONS.add(ADVCL);
		TARGET_RELATIONS.add(CCOMP);
		TARGET_RELATIONS.add(CSUBJ);
		TARGET_RELATIONS.add(CSUBJPASS);
		TARGET_RELATIONS.add(XCOMP);
	}
	
	
}
