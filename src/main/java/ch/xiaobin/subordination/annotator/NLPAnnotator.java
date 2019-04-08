package ch.xiaobin.subordination.annotator;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;

/**
 * For getting an NLP pipeline, which can either be local or remote/from a server.
 * Default to get local.
 * @author xiaobin
 *
 */
public class NLPAnnotator {
	private boolean isUseLocalPipeline = true;
	private AnnotationPipeline annotationPipeline = null;
	private Logger logger = LogManager.getLogger();

	public NLPAnnotator() throws IOException {
		//init annotation pipeline
		AnnotationPipelineManager apManager = new AnnotationPipelineManager();
		if(isUseLocalPipeline) {
			annotationPipeline = apManager.getLocalPipeLine();
		} else {
			annotationPipeline = apManager.getServerPipeline();
		}
	}

	public void annotate(Annotation document) {
		//check that pipeline initiated
		if(annotationPipeline == null) {
			throw logger.throwing(new NullPointerException("NLP annotation pipeline has not been initialized."));
		}
		
		annotationPipeline.annotate(document);
	}

}
