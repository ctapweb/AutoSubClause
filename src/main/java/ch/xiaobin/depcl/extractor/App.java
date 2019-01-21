package ch.xiaobin.depcl.extractor;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations.DependencyAnnotation;
import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations.DocumentIdAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.DocIDAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws DepClauseExtractorException
    {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,depparse");
        StanfordCoreNLPClient pipeline = new StanfordCoreNLPClient(props, "http://localhost/", 9000);
        
//        String textToAnalyze = "The door opened because the man pushed it.";
        String textToAnalyze = "I wondered whether the homework was necessary.";
        
        Annotation document = new Annotation(textToAnalyze);
        pipeline.annotate(document);
//        document.set(DocIDAnnotation.class, "abcid");
//        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
//        CoreMap sentence = sentences.get(0);
//        String docId = sentence.get(DocIDAnnotation.class);
//        System.out.println(docId);
        
        DepClauseExtractor depClauseExtractor = new DepClauseExtractor(pipeline, document);
        List<DepClause> depClauseList = depClauseExtractor.getDepClauses();
        for(DepClause depClause: depClauseList) {
        	System.out.println("sentence: " + depClause.getSentence());
        	System.out.println("clauseText: " + depClause.getClauseText());
        	System.out.println("beginTokenIdx: " + depClause.getBeginTokenIdx());
        	System.out.println("endTokenIdx: " + depClause.getEndTokenIdx());
        	System.out.println("subConjunction idx: " + depClause.getSubordinatingConjunctionIdx());
        	System.out.println("subConjunction: " + depClause.getSubordinatingConjunction());
        	System.out.println("clauseType: " + depClause.getClauseType());
        }
    }
}
