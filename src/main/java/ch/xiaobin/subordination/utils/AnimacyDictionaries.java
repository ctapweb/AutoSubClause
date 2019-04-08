package ch.xiaobin.subordination.utils;

import java.util.HashSet;
import java.util.Set;

import edu.stanford.nlp.dcoref.Dictionaries;

public class AnimacyDictionaries {

	static Set<String> animateWords = null;
	static Set<String> inanimateWords = null;

	public static Set<String> getAnimateWords() {
		if(animateWords == null) {
			animateWords = new HashSet<>();
			Dictionaries dictionaries = new Dictionaries();
			animateWords.addAll(dictionaries.animateWords);
			animateWords.addAll(dictionaries.animatePronouns);
		}

		return animateWords;
	}
	
	public static Set<String> getInanimateWords() {
		if(inanimateWords == null) {
			inanimateWords = new HashSet<>();
			Dictionaries dictionaries = new Dictionaries();
			inanimateWords.addAll(dictionaries.inanimateWords);
			inanimateWords.addAll(dictionaries.inanimatePronouns);
		}
		return inanimateWords;
	}
}
