/**
 * 
 */
package ch.xiaobin.depcl.extractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.xiaobin.depcl.annotator.AnnotationPipelineManager;
import edu.stanford.nlp.ling.CoreAnnotations.DocIDAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.util.CoreMap;

/**
 * Extracts number of sentences and tokens.
 * @author xiaobin
 *
 */
public class SentenceAndTokenAnalyzer {

	AnnotationPipelineManager pipelineManager;
	AnnotationPipeline pipeline;

	private static final String CSV_SEPARATOR = "\t";
	private static final String RESULTS_FILE_HEADER = "doc_id" + CSV_SEPARATOR
			+ "n_sent" + CSV_SEPARATOR
			+ "n_tokens" + CSV_SEPARATOR
			+ "\n";
	private static final String ENCODING = "UTF8";

	Logger logger = LogManager.getLogger();

	public SentenceAndTokenAnalyzer() throws IOException {
		pipelineManager = new AnnotationPipelineManager();
//		pipeline = pipelineManager.getLocalPipeLine();
		pipeline = pipelineManager.getServerPipeline();
	}
	
	/**
	 * Analyzing a folder to get dependent clause information.
	 * Usage: java DepClauseAnalyzer nThreads folderToAnalyze resultsFileName
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if(args.length != 3) {
			System.err.println("Usage: java SentenceAndTokenAnalyzer nThreads folderToAnalyze resultsFileName");
			return;
		}
		
		int nThreads = Integer.parseInt(args[0]);
		String folderToAnalyze = args[1];
		String resultsFileName = args[2];
		
		SentenceAndTokenAnalyzer analyzer = new SentenceAndTokenAnalyzer();
		analyzer.analyzeFolder(folderToAnalyze, resultsFileName, nThreads);
		
	}

	/**
	 * Analyse one text. 
	 * @param text
	 * @return
	 * @throws DepClauseExtractorException 
	 */
	public TwoValues analyzeText(String documentId, String text) throws DepClauseExtractorException {
//		logger.trace("Analyzing document {}: {}...", documentId, text.length() >= 40? text.substring(0, 40) : text);
		TwoValues twoValues = new TwoValues(); 

		Annotation document = new Annotation(text);
		document.set(DocIDAnnotation.class, documentId);

		pipeline.annotate(document);
		
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		List<CoreLabel> tokens = document.get(TokensAnnotation.class);
		twoValues.setnSentences(sentences.size());
		twoValues.setnTokens(tokens.size());

		return twoValues;
	}

	/**
	 * Analyze all the files in a folder and output the results to the result file.
	 * Single threaded.
	 * @param folderPath
	 * @param resultFilePath
	 */
	public void analyzeFolder(String folderPath, String resultFilePath) {
		analyzeFolder(folderPath, resultFilePath, 1);
	}

	/**
	 * Analyze all the files in a folder and output the results to the result file.
	 * Multi-threaded.
	 * @param folderPath
	 * @param resultFilePath
	 * @param numThreads
	 */
	public void analyzeFolder(String folderPath, String resultFilePath, int numThreads) {
		logger.trace("Analyzing folder {} with {} thread(s). Results output to {}.", folderPath, numThreads, resultFilePath);

		//gets a list of all the files in the folder
		List<File> fileList = getAllFilesInFolder(folderPath);
		
		int filesPerThread = fileList.size() / numThreads;
	
		//divide the list into n sublists
		List<List<File>> fileSubLists = ListUtils.partition(fileList, filesPerThread);

		//for each sublist of files, start a thread of analysis, output the results to resultFilePath.n
		for(int i = 0; i < fileSubLists.size(); i++) {
			List<File> filesToAnalyze = fileSubLists.get(i);
			String resultFileName = resultFilePath + "." + i;

			logger.trace("Starting thread {} to analyze {} file(s)...", i, filesToAnalyze.size()); 
			startAnalysisThread(i, filesToAnalyze, new File(resultFileName));
			
			
		}

	}

	private List<File> getAllFilesInFolder(String folderPath) {
		File folderToAnalyze = new File(folderPath);
		Collection<File> files = FileUtils.listFiles(folderToAnalyze, TrueFileFilter.INSTANCE, null);
		return new ArrayList<>(files);
	}

	private void startAnalysisThread(int threadIdx, List<File> filesToAnalyze, File resultsFile) {
		String threadName = threadIdx + "";
		Thread analysisThread = new Thread(threadName) {
			@Override
			public void run() {
				//output the results file header
				try {
					FileUtils.writeStringToFile(resultsFile, RESULTS_FILE_HEADER, ENCODING);
					
					int numFiles = filesToAnalyze.size();

					for(int i = 0; i < numFiles; i++ ) {
						File fileToAnalyze = filesToAnalyze.get(i);
						//for each file analyze and output results
						String text = FileUtils.readFileToString(fileToAnalyze, ENCODING);
						String documentId = fileToAnalyze.getName();

						logger.trace("Thread {} analyzing file {}/{}: {}...", getName(), i+1, numFiles, documentId);
						TwoValues twoValues = analyzeText(documentId, text);


						List<String> resultLines = new ArrayList<>();
						StringBuilder strBuilder = new StringBuilder(documentId + CSV_SEPARATOR);
						//output results
						strBuilder.append(twoValues.getnSentences()).append(CSV_SEPARATOR)
						.append(twoValues.getnTokens()).append(CSV_SEPARATOR);
						
						resultLines.add(strBuilder.toString());

						FileUtils.writeLines(resultsFile, resultLines, null, true);

					}
					logger.trace("Thread {} completed!", threadName);
				} catch (IOException | DepClauseExtractorException e1) {
					logger.throwing(e1);
				}
			}
		};
		analysisThread.start();
	}
}
