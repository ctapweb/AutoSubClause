package ch.xiaobin.subordination.dao;

/**
 * Roles of an NP in either main or subordinate clauses.
 * We follow the NP accessibility hierarchy as put forward by Keenan and Comrie (1977).
 * @author xiaobin
 *
 */
public enum NPRoles {
	SUBJECT, //nsubj
	DIRECT_OBJECT, //dobj
	INDIRECT_OBJECT, //iobj
//	OBLIQUE, // John put the money in [the chest]. 
//	GENETIVE, //John took [the man]'s hat.
	PREPOSITION_COMPLEMENT, // John is taller than [the man]. /nmod_than
	APPOSITIVE, //appos
}
