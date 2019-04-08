package ch.xiaobin.subordination.main;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import ch.xiaobin.subordination.annotator.AnnotationPipelineManager;
import ch.xiaobin.subordination.dao.SubordinateClause;
import ch.xiaobin.subordination.extractor.DocumentSubClauseExtractor;
import edu.stanford.nlp.dcoref.Dictionaries;
import edu.stanford.nlp.pipeline.AnnotationPipeline;

public class App {

	public static void main(String[] args) throws IOException {
		String document = "The door opened because the man pushed it.\n" + 
				"I wondered whether the homework was necessary.\n" + 
				"They will visit you before they go to the airport.\n" + 
				"Before they go to the airport, they will visit you.\n" + 
				"I went to the show that was very popular.\n" + 
				"";
		AnnotationPipelineManager pipelineManager = new AnnotationPipelineManager();

		DocumentSubClauseExtractor documentSubClauseExtractor = new DocumentSubClauseExtractor("test_doc",
				document, pipelineManager.getServerPipeline());
		List<SubordinateClause> subordinateClauses = documentSubClauseExtractor.extractClauses();
		for(SubordinateClause subClause: subordinateClauses) {
			System.out.println(subClause);
		}
	}

}
