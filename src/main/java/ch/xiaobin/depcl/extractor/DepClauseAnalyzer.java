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
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;

/**
 * Analyzes texts or folders to extract dependent clause information. 
 * Supports multithreaded analysis. Results output in CSV format. 
 * @author xiaobin
 *
 */
public class DepClauseAnalyzer {

	AnnotationPipelineManager pipelineManager;
	AnnotationPipeline pipeline;

	private static final String CSV_SEPARATOR = "\t";
	private static final String RESULTS_FILE_HEADER = "doc_id" + CSV_SEPARATOR
			+ "sent_idx" + CSV_SEPARATOR
			+ "sent_txt" + CSV_SEPARATOR
			+ "clause_type" + CSV_SEPARATOR
			+ "clause_txt" + CSV_SEPARATOR
			+ "beginToken_idx" + CSV_SEPARATOR
			+ "endToken_idx" + CSV_SEPARATOR
			+ "sub_conj_idx" + CSV_SEPARATOR
			+ "sub_conj" + CSV_SEPARATOR
			+ "ref_idx" + CSV_SEPARATOR
			+ "referent" + CSV_SEPARATOR
			+ "clause_root_idx" + CSV_SEPARATOR
			+ "governor_idx" + CSV_SEPARATOR
			+ "is_defining" + CSV_SEPARATOR
			+ "is_ante_human" + CSV_SEPARATOR
			+ "\n";
	private static final String ENCODING = "UTF8";

	Logger logger = LogManager.getLogger();

	public DepClauseAnalyzer() throws IOException {
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
			System.err.println("Usage: java DepClauseAnalyzer nThreads folderToAnalyze resultsFileName");
			return;
		}
		
		int nThreads = Integer.parseInt(args[0]);
		String folderToAnalyze = args[1];
		String resultsFileName = args[2];
		
		DepClauseAnalyzer analyzer = new DepClauseAnalyzer();
		analyzer.analyzeFolder(folderToAnalyze, resultsFileName, nThreads);
		
	}

	/**
	 * Analyzes one text 
	 * @param text
	 * @return
	 * @throws DepClauseExtractorException 
	 */
	public List<DepClause> analyzeText(String documentId, String text) throws DepClauseExtractorException {
//		logger.trace("Analyzing document {}: {}...", documentId, text.length() >= 40? text.substring(0, 40) : text);
		List<DepClause> clauseList; 

		Annotation document = new Annotation(text);
		document.set(DocIDAnnotation.class, documentId);
		DepClauseExtractor depClauseExtractor = new DepClauseExtractor(pipeline, document);
		
//		logger.trace("getting dependent clauses...");
		clauseList = depClauseExtractor.getDepClauses();

		return clauseList;
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
						List<DepClause> depClauseList = analyzeText(documentId, text);
						List<String> resultLines = new ArrayList<>();

						logger.trace("Thread {} analyzing file {}/{}: {}...", getName(), i+1, numFiles, documentId);
						for(DepClause depClause: depClauseList) {
							if(depClause == null) {
								continue; //skip null object
							}
							
							StringBuilder strBuilder = new StringBuilder(documentId + CSV_SEPARATOR);
							//output results
							strBuilder.append(depClause.getSentenceIdx()).append(CSV_SEPARATOR)
							.append(depClause.getSentence()).append(CSV_SEPARATOR)
							.append(depClause.getClauseType()).append(CSV_SEPARATOR)
							.append(depClause.getClauseText()).append(CSV_SEPARATOR)
							.append(depClause.getBeginTokenIdx()).append(CSV_SEPARATOR)
							.append(depClause.getEndTokenIdx()).append(CSV_SEPARATOR)
							.append(depClause.getSubordinatingConjunctionIdx()).append(CSV_SEPARATOR)
							.append(depClause.getSubordinatingConjunction()).append(CSV_SEPARATOR)
							.append(depClause.getReferentIdx()).append(CSV_SEPARATOR)
							.append(depClause.getReferent()).append(CSV_SEPARATOR)
							.append(depClause.getClauseRootIdx()).append(CSV_SEPARATOR)
							.append(depClause.getGovernorIdx()).append(CSV_SEPARATOR)
							.append(depClause.isDefining()).append(CSV_SEPARATOR)
							.append(depClause.isAntecedentHuman());

							resultLines.add(strBuilder.toString());
						}
						FileUtils.writeLines(resultsFile, resultLines, null, true);
						
						//remove analyzed file
//						FileUtils.deleteQuietly(fileToAnalyze);
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
