package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

/**
 *
 */
public enum DependencyTag {

    /**
     * An appositional modifier of an NP is an NP immediately to the right of the first NP that serves to define or
     * modify that NP. It includes parenthesized examples, as well as defining abbreviations in one of these structures.
     */
    APPOS,

    /**
     * A nominal subject is a noun phrase which is the syntactic subject of a clause. The governor of this relation
     * might not always be a verb: when the verb is a copular verb, the root of the clause is the complement of the
     * copular verb, which can be an adjective or noun.
     */
    NSUBJ,

    /**
     * The possession modifier relation holds between the head of an NP and its possessive determiner,
     * or agenitive ’s complement.
     */
    POSS,

    /**
     * The direct object of a VP is the noun phrase which is the (accusative) object of the verb.
     */
    DOBJ,

    /**
     * The indirect object of a VP is the noun phrase which is the (dative) object of the verb.
     */
    IOBJ,

    /**
     * The nmod relation is used for nominal modifiers of nouns or clausal predicates.
     * nmod is a noun functioning as a non-core (oblique) argument or adjunct. In English, nmod is used
     * - for prepositional complements
     * - for ‘s genitives
     */
    NMOD,

    /**
     * A passive nominal subject is a noun phrase which is the syntactic subject of a passive clause.
     */
    NSUBJPASS,

    /**
     * The object of a preposition is the head of a noun phrase following the preposition,
     * or the adverbs "here" and "there".
     */
    POBJ,

    /**
     * An agent is the complement of a passive verb which is introduced by the preposition "by" and does the action.
     * This relation only appears in the collapsed dependencies, where it can replace prep_by, where appropriate.
     * It does not appear in basic dependencies output.
     */
    AGENT,

    /**
     * A numeric modifier of a noun is any number phrase that serves to modify the meaning of the noun with a quantity.
     */
    NUM,

    /**
     * A predeterminer is the relation between the head of an NP and a word that precedes and modifies the meaning
     * of the NP determiner.
     */
    PREDET,

    /**
     * A relative clause modifier of an NP is a relative clause modifying the NP. The relation points from the head
     * noun of the NP to the head of the relative clause, normally a verb.
     */
    RCMOD;
}
