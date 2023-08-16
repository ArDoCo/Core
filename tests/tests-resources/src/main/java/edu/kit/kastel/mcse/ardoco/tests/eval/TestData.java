package edu.kit.kastel.mcse.ardoco.tests.eval;

import java.io.Serializable;

public record TestData<T extends Serializable>(T data) implements Serializable {
}
