/**
 * 
 */
package ch.xiaobin.subordination.validation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.xiaobin.subordination.annotator.AnnotationPipelineManager;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.util.CoreMap;

/**
 * Generates a spreasheet that contains the sentences in the documents to be annotated.
 * Usage: java PrepareTexts folderToAnalyze resultsFileName
 * @author xiaobin
 *
 */
public class PrepareTexts {
	Logger logger = LogManager.getLogger();
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if(args.length != 2) {
			System.err.println("Usage: java PrepareTexts folderToAnalyze resultsFileName");
			return;
		}
		
		String folderToAnalyze = args[0];
		String resultsFileName = args[1];
		
		PrepareTexts prepareTexts = new PrepareTexts();
		
		AnnotationPipelineManager pipelineManager = new AnnotationPipelineManager();
		AnnotationPipeline pipeline = pipelineManager.getServerPipeline();

		//gets a list of all the files in the folder
		File folder = new File(folderToAnalyze);
		File resultsFile = new File(resultsFileName);
		
		//write the header
		Collection<File> files = FileUtils.listFiles(folder, TrueFileFilter.INSTANCE, null);
		List<String> lines = new ArrayList<>();
		lines.add("doc_id\tsent_idx\tsent_txt");
		for(File file: files) {
			String documentId = file.getName();
			String text = FileUtils.readFileToString(file, "UTF-8");
			Annotation document = new Annotation(text);
			pipeline.annotate(document);
			
			//gets the results
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			for(int i =0; i < sentences.size(); i++) {
				CoreMap sentence = sentences.get(i);
				String sentenceText = sentence.get(TextAnnotation.class);
				sentenceText = sentenceText.replaceAll("\\n", " ").replaceAll("\\t", " ");
				StringBuilder strBuilder = new StringBuilder();
				strBuilder.append(documentId).append("\t")
				.append(i).append("\t")
				.append(sentenceText);
				
				lines.add(strBuilder.toString());
			}
		}
		FileUtils.writeLines(resultsFile, lines, false);

		prepareTexts.logger.info("Texts in folder broken down into sentences and wrote to results file.");
	}

}
