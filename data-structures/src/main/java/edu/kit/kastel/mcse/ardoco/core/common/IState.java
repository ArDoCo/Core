package edu.kit.kastel.mcse.ardoco.core.common;

/**
 * The Interface IState defines states for {@link IAgent Agents} and {@link IExtractor Extractors}.
 *
 * @param <S> the generic type of the state
 */
public interface IState<S extends IState<S>> extends ICopyable<S> {

}
