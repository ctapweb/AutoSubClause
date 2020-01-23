package ch.xiaobin.subordination.extractor;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.xiaobin.subordination.dao.SubordinateClause;
import edu.stanford.nlp.ling.CoreAnnotations.DocIDAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.util.CoreMap;

public class DocumentSubClauseExtractor implements SubClauseExtractor {

	private String documentId;
	private String document;
	private AnnotationPipeline annotationPipeline;
	private Annotation annotatedDocument;
	
	private Logger logger = LogManager.getLogger();

	public DocumentSubClauseExtractor(String documentId, String document, AnnotationPipeline annotationPipeline) {
		this.documentId = documentId;
		this.document = document;
		this.annotationPipeline = annotationPipeline;
		this.annotatedDocument = new Annotation(document);
		
		//annotate document
		annotatedDocument.set(DocIDAnnotation.class, documentId);
		annotationPipeline.annotate(annotatedDocument);
		
	}

	public DocumentSubClauseExtractor(Annotation annotatedDocument) {
		this.annotatedDocument = annotatedDocument;
		this.documentId = annotatedDocument.get(DocIDAnnotation.class);
		this.document = annotatedDocument.get(TextAnnotation.class);
	}

	@Override
	public List<SubordinateClause> extractClauses() {
		List<SubordinateClause> allClauses = new ArrayList<>();

		//get all annotated sentences
		List<CoreMap> annotatedSentences = annotatedDocument.get(SentencesAnnotation.class);
		for(CoreMap annotatedSentence: annotatedSentences) {
			int sentenceIdx = annotatedSentence.get(SentenceIndexAnnotation.class);
			SentenceSubClauseExtractor sentSubClauseExtractor = 
					new SentenceSubClauseExtractor(sentenceIdx, annotatedSentence);
			List<SubordinateClause> clauses = sentSubClauseExtractor.extractClauses();
			allClauses.addAll(clauses);
		}
		
		return allClauses;
	}

	/**
	 * Gets the text of a sentence.
	 * @param sentenceIdx
	 * @return
	 */
	public String getSentenceText(int sentenceIdx) {
		return annotatedDocument.get(SentencesAnnotation.class).get(sentenceIdx).get(TextAnnotation.class);
	}
	
	

}
