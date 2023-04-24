/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.old.java_x_pcm;

import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.models.old.java.JavaClassOrInterface;
import edu.kit.kastel.mcse.ardoco.core.models.old.pcm.PCMComponent;

/**
 * This interface provides necessary methods to identify links between Java models and PCM models.
 */
public interface JavaPCMConnector {
    Set<JavaClassOrInterface> getClassesThatBelongToComponent(PCMComponent component);

    Set<JavaClassOrInterface> getInterfacesThatBelongToComponent(PCMComponent component);

    PCMComponent getComponentOfClassOrInterface(JavaClassOrInterface classOrInterface);
}
