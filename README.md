# AutoSubClause

Automatic subordinate clause extractor

This application is for analyzing English texts to extract subordinate clauses
used in the texts. For those who do not want to work with programming or the
source code, you can simply download a pre-compiled package from the `Packages`
collection of this repo. It's the easiest to just download the package with
dependencies to make sure it has all the required libraries to run properly.
These are packages with a long name like
`extractor-1.0.x-jar-with-dependencies.jar`.

### To test the downloaded package

Run 

`java -cp extractor-1.0.1-jar-with-dependencies.jar ch.xiaobin.subordination.main.App`

It will output some log information and the extracted subordinate clause information from a sample document in the following format:

> documentId: test_doc
> sentenceIdx: 4
> clauseType: RELATIVE
> isFinite: true
> clauseBeginIdx: 212
> clauseEndIdx: 233
> hasSubordinator: true
> subordinatorBeginIdx: 212
> subordinatorEndIdx: 216
> subordinator: that
> embeddedness: 1
> isRestrictive: true
> hasHeadNoun: true
> headNounBeginIdx: 207
> headNounEndIdx: 211
> headNoun: show
> isHeadNounAnimate: false
> headNounRoleInMainClause: PREPOSITION_COMPLEMENT
> headNounRoleInSubClause: SUBJECT

### To analyze your own corpus stored in a folder

Run:

`java -cp extractor-1.0.1-jar-with-dependencies.jar ch.xiaobin.subordination.main.FolderAnalyzer  nThreads folderToAnalyze resultsFileName` 

Replace `nThreads` with the number of parallel threads you want to analyze the
corpus. On multi-core machines, using more parallel threads will greatly
increase the speed of the analysis. Please note that the number of parallel
threads should not exceed the total number of texts in your corpus. For example,
don't ask for 4 threads if you are only analyzing 3 texts. 

Depending on the number of threads, the results will be stored in the files named
resultsFileName.x in Tab Separated Values (a type of CSV format but with TABs as
column separators) format.

### Using AutoSubClause programmatically

For Java programmers, if you want to have more control over the analysis or use AutoSubClause as a library, following the following steps:

```java
//create a PipelineManager object:
AnnotationPipelineManager pipelineManager = new AnnotationPipelineManager();

//get an AnnotationPipeline from the PipelineManager
AnnotationPipeline pipeline = pipelineManager.getLocalPipeLine();
//You can also run  a CoreNLP server and supply the server details in the `src/main/resources/config.properties` file. 
//Then from the PipelineManager you can get a server pipe line:
//AnnotationPipeline pipeline = pipelineManager.getServerPipeLine();

//the document to analyze
String document = "I went to the show that was very popular.";
String documentId = "docID";

//create a DocumentSubClauseExtractor object by supplying the document id,
//document text, and an NLP pipeline
DocumentSubClauseExtractor documentSubClauseExtractor = 
		new DocumentSubClauseExtractor(documentId, document, pipeline);
		
//run the analysis by calling the extractClauses() function
List<SubordinateClause> subordinateClauses = 
		documentSubClauseExtractor.extractClauses();

//get the extracted subordinate clauses information
String CSV_SEPARATOR = "\t";
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

```

### See also

`ch.xiaobin.subordination.extractor.SentenceSubClauseExtractorTest` for examples on how to use the library programmatically.
