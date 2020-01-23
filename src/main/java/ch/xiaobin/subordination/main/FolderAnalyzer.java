/**
 * 
 */
package ch.xiaobin.subordination.main;

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

import ch.xiaobin.subordination.annotator.AnnotationPipelineManager;
import ch.xiaobin.subordination.dao.AdjunctClause;
import ch.xiaobin.subordination.dao.ClauseType;
import ch.xiaobin.subordination.dao.ComplementClause;
import ch.xiaobin.subordination.dao.RelativeClause;
import ch.xiaobin.subordination.dao.SubordinateClause;
import ch.xiaobin.subordination.extractor.DocumentSubClauseExtractor;
import edu.stanford.nlp.ling.CoreAnnotations.DocIDAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;

/**
 * Analyzes texts or folders to extract dependent clause information. 
 * Supports multithreaded analysis. Results output in CSV format. 
 * @author xiaobin
 *
 */
public class FolderAnalyzer {

	AnnotationPipelineManager pipelineManager;
	AnnotationPipeline pipeline;

	private static final String CSV_SEPARATOR = "\t";
	private static final String RESULTS_FILE_HEADER = 
			"doc_id" + CSV_SEPARATOR
			+ "sent_idx" + CSV_SEPARATOR
			+ "sent_txt" + CSV_SEPARATOR
			+ "clause_type" + CSV_SEPARATOR
			+ "is_finite" + CSV_SEPARATOR
			+ "clause_txt" + CSV_SEPARATOR
			+ "clause_begin_idx" + CSV_SEPARATOR
			+ "clause_end_idx" + CSV_SEPARATOR
			+ "has_subordinator" + CSV_SEPARATOR
			+ "subordinator_begin_idx" + CSV_SEPARATOR
			+ "subordinator_end_idx" + CSV_SEPARATOR
			+ "subordinator" + CSV_SEPARATOR
			+ "embeddedness" + CSV_SEPARATOR
			//for complement clauses
			+ "complement_type" + CSV_SEPARATOR
			//for adverbial clauses
			+ "adjunct_function" + CSV_SEPARATOR
			//for relative clauses
			+ "is_restrictive" + CSV_SEPARATOR
			+ "has_head_noun" + CSV_SEPARATOR
			+ "head_noun_begin_idx" + CSV_SEPARATOR
			+ "head_noun_end_idx" + CSV_SEPARATOR
			+ "head_noun" + CSV_SEPARATOR
			+ "is_head_noun_animate" + CSV_SEPARATOR
			+ "head_noun_role_in_main" + CSV_SEPARATOR
			+ "head_noun_role_in_sub" + CSV_SEPARATOR
			+ "\n";
	private static final String ENCODING = "UTF8";

	Logger logger = LogManager.getLogger();

	public FolderAnalyzer() throws IOException {
		pipelineManager = new AnnotationPipelineManager();
		pipeline = pipelineManager.getLocalPipeLine();
		//pipeline = pipelineManager.getServerPipeline();
	}

	/**
	 * Analyze a folder to get dependent clause information.
	 * Usage: java FolderAnalyzer nThreads folderToAnalyze resultsFileName
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if(args.length != 3) {
			System.err.println("Usage: java FolderAnalyzer nThreads folderToAnalyze resultsFileName");
			return;
		}

		int nThreads = Integer.parseInt(args[0]);
		String folderToAnalyze = args[1];
		String resultsFileName = args[2];

		FolderAnalyzer analyzer = new FolderAnalyzer();
		analyzer.analyzeFolder(folderToAnalyze, resultsFileName, nThreads);

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
						String document = FileUtils.readFileToString(fileToAnalyze, ENCODING);
						String documentId = fileToAnalyze.getName();

						logger.trace("Thread {} analyzing file {}/{}: {}...", getName(), i+1, numFiles, documentId);
						//						List<SubordinateClause> depClauseList = analyzeText(documentId, text);
						DocumentSubClauseExtractor documentSubClauseExtractor = 
								new DocumentSubClauseExtractor(documentId, document, pipeline);
						List<SubordinateClause> subordinateClauses = 
								documentSubClauseExtractor.extractClauses();

						List<String> resultLines = new ArrayList<>();

						for(SubordinateClause subClause: subordinateClauses) {
							if(subClause == null) {
								continue; //skip null object
							}


							StringBuilder strBuilder = new StringBuilder(documentId + CSV_SEPARATOR);
							//output results
							strBuilder.append(subClause.getSentenceIdx()).append(CSV_SEPARATOR)
							.append(subClause.getSentenceText()).append(CSV_SEPARATOR)
							.append(subClause.getClauseType()).append(CSV_SEPARATOR)
							.append(subClause.isFinite()).append(CSV_SEPARATOR)
							.append(document.substring(subClause.getClauseBeginIdx(), subClause.getClauseEndIdx())).append(CSV_SEPARATOR)
							.append(subClause.getClauseBeginIdx()).append(CSV_SEPARATOR)
							.append(subClause.getClauseEndIdx()).append(CSV_SEPARATOR)
							.append(subClause.hasSubordinator()).append(CSV_SEPARATOR)
							.append(subClause.getSubordinatorBeginIdx()).append(CSV_SEPARATOR)
							.append(subClause.getSubordinatorEndIdx()).append(CSV_SEPARATOR)
							.append(subClause.getSubordinator()).append(CSV_SEPARATOR)
							.append(subClause.getEmbeddedness()).append(CSV_SEPARATOR);

							//for complement clause;
							if(subClause.getClauseType().equals(ClauseType.COMPLEMENT)) {
								ComplementClause complementClause = (ComplementClause) subClause;
								strBuilder.append(complementClause.getComplementType()).append(CSV_SEPARATOR);
							} else {
								strBuilder.append("null").append(CSV_SEPARATOR);
							}

							//for adverbial clause
							if(subClause.getClauseType().equals(ClauseType.ADJUNCT)) {
								AdjunctClause adjunctClause = (AdjunctClause) subClause;
								strBuilder.append(adjunctClause.getFunction()).append(CSV_SEPARATOR);
							} else {
								strBuilder.append("null").append(CSV_SEPARATOR);
							}

							//for relative clause
							if(subClause.getClauseType().equals(ClauseType.RELATIVE)) {
								RelativeClause relativeClause = (RelativeClause) subClause;
								strBuilder.append(relativeClause.isRestrictive()).append(CSV_SEPARATOR)
								.append(relativeClause.hasHeadNoun()).append(CSV_SEPARATOR)
								.append(relativeClause.getHeadNounBeginIdx()).append(CSV_SEPARATOR)
								.append(relativeClause.getHeadNounEndIdx()).append(CSV_SEPARATOR)
								.append(relativeClause.getHeadNoun()).append(CSV_SEPARATOR)
								.append(relativeClause.isHeadNounAnimate()).append(CSV_SEPARATOR)
								.append(relativeClause.getHeadNounRoleInMainClause()).append(CSV_SEPARATOR)
								.append(relativeClause.getHeadNounRoleInSubClause()).append(CSV_SEPARATOR);
							} else {
								strBuilder.append("null").append(CSV_SEPARATOR)
								.append("null").append(CSV_SEPARATOR)
								.append("null").append(CSV_SEPARATOR)
								.append("null").append(CSV_SEPARATOR)
								.append("null").append(CSV_SEPARATOR)
								.append("null").append(CSV_SEPARATOR)
								.append("null").append(CSV_SEPARATOR)
								.append("null").append(CSV_SEPARATOR);
							}

							resultLines.add(strBuilder.toString());
						}
						FileUtils.writeLines(resultsFile, resultLines, null, true);
						
						//////////////////////////////////////////////////////////////
						//remove file after analysis
						FileUtils.forceDelete(fileToAnalyze);

					}
					logger.trace("Thread {} completed!", threadName);

				} catch (IOException e) {
					logger.throwing(e);
				}
			}
		};

		analysisThread.start();
	}
}
