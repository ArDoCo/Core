package edu.kit.kastel.mcse.ardoco.core.datastructures.modules;

public interface IState<S extends IState> {
    S createCopy();
}
