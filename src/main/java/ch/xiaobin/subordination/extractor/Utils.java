package ch.xiaobin.subordination.extractor;

import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.Pair;

public class Utils {
	private static Logger logger = LogManager.getLogger();

	//This is a make shift solution, because the yieldSpan function in SemanticGraph could have indefinite loop
	public static Pair<Integer, Integer> yieldSpan(SemanticGraph sentenceSemGraph, IndexedWord word, int numTokens) {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		Stack<IndexedWord> fringe = new Stack<>();
		fringe.push(word);
		int count = 0;
		while (!fringe.isEmpty()) {
			IndexedWord parent = fringe.pop();
			min = Math.min(min, parent.index() - 1);
			max = Math.max(max, parent.index());
			for (SemanticGraphEdge edge : sentenceSemGraph.outgoingEdgeIterable(parent)) {
				if (!edge.isExtra()) {
//					logger.trace("Pushing edge dependent: {}", edge.getDependent().originalText());
					fringe.push(edge.getDependent());
				}
				if(max >= numTokens + 1 || fringe.size() >= numTokens + 1) {
					break;
				}
			}
//			logger.trace("numTokens: {}, max:{}, min{}, fringeSize: {}", numTokens, max, min, fringe.size());
			if(max >= numTokens + 1 || fringe.size() >= numTokens + 1 || count++ >= 10000) {
				break;
			}
		}
		return Pair.makePair(min, max);
	}

}
