package ch.xiaobin.subordination.extractor;

import java.util.List;

import ch.xiaobin.subordination.dao.SubordinateClause;
import edu.stanford.nlp.pipeline.Annotation;

public interface SubClauseExtractor {

	public List<SubordinateClause> extractClauses();
	
}
