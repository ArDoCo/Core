/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.IPhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;

public class PhraseMapping implements IPhraseMapping {

	/**
	 * Phrases encapsulated in the mapping.
	 */
	private final MutableSet<IPhrase> phrases;

	public PhraseMapping(IPhrase phrase) {

		Objects.requireNonNull(phrase);
		this.phrases = Sets.mutable.with(phrase);
	}

	public PhraseMapping(ImmutableList<IPhrase> phrases) {
		this(phrases.get(0));

		for (int i = 1; i < phrases.size(); i++) {
			if (phrases.get(0).getPhraseType() != phrases.get(i).getPhraseType()) {
				// TODO: Combine different phraseTypes in PMs
				// throw new IllegalArgumentException("All phrases in a phrase mapping should
				// have the same phrase type!");
			}
			this.addPhrase(phrases.get(i));
		}
	}

	@Override
	public void addPhrases(ImmutableList<IPhrase> phrases) {
		for (IPhrase phrase : phrases) {
			if (!this.phrases.contains(phrase)) {
				this.phrases.add(phrase);
			}
		}
	}

	@Override
	public ImmutableList<INounMapping> getNounMappings(ITextState textState) {
		return textState.getNounMappings().select(nm -> nm.getWords().equals(phrases.flatCollect(IPhrase::getContainedWords)));
	}

	@Override
	public ImmutableList<IPhrase> getPhrases() {
		return phrases.toImmutableList();
	}

	@Override
	public void addPhrase(IPhrase phrase) {
		if (!phrase.getPhraseType().equals(this.getPhraseType())) {
			// TODO: Combine different phraseTypes in PMs
			// throw new IllegalArgumentException("added a different phrase type for
			// mapping");
		}
		phrases.add(phrase);
	}

	@Override
	public void removePhrase(IPhrase phrase) {

		// select all noun mappings that contain the phrase
		phrases.remove(phrase);

	}

	@Override
	public PhraseType getPhraseType() {
		if (phrases.isEmpty()) {
			throw new IllegalStateException("A phrase mapping should always contain some phrases!");
		}
		return phrases.getAny().getPhraseType();
	}

	@Override
	public Map<IWord, Integer> getPhraseVector() {

		MutableList<IWord> words = Lists.mutable.empty();

		for (IPhrase phrase : phrases) {
			words.addAllIterable(phrase.getContainedWords());
		}

		Map<IWord, Integer> phraseVector = new HashMap<>();
		var grouped = words.groupBy(IWord::getText).toMap();
		grouped.forEach((key, value) -> phraseVector.put(value.getAny(), value.size()));
		// TODO: Thing about norm

		return phraseVector;
	}

	@Override
	public IPhraseMapping merge(IPhraseMapping phraseMapping) {

		if (phraseMapping.getPhraseType() != this.getPhraseType()) {
			// TODO: add different phraseTypes to one PM
			// throw new IllegalArgumentException("The phrase types inside a phrase mapping
			// should be the same!");
		}
		this.phrases.addAllIterable(phrases.select(p -> !this.getPhrases().contains(p)));
		return this;
	}

	@Override
	public boolean containsExactNounMapping(ITextState textState, INounMapping nm) {

		return getNounMappings(textState).anySatisfy(cnm -> cnm.containsSameWordsAs(nm));
	}

	public boolean isAlmostEqual(ITextState textState, IPhraseMapping other) {

		return Objects.equals(getPhraseType(), other.getPhraseType()) && Objects.equals(getNounMappings(textState), other.getNounMappings(textState))
				&& Objects.equals(getPhraseVector(), other.getPhraseVector());
	}

	@Override
	public IPhraseMapping createCopy() {
		var pm = new PhraseMapping(phrases.toList().toImmutable());
		return pm;
	}
}
