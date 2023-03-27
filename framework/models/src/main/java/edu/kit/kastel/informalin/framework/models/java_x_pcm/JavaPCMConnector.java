/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.informalin.framework.models.java_x_pcm;

import java.util.Set;

import edu.kit.kastel.informalin.framework.models.java.JavaClassOrInterface;
import edu.kit.kastel.informalin.framework.models.pcm.PCMComponent;

/**
 * This interface provides necessary methods to identify links between Java models and PCM models.
 */
public interface JavaPCMConnector {
    Set<JavaClassOrInterface> getClassesThatBelongToComponent(PCMComponent component);

    Set<JavaClassOrInterface> getInterfacesThatBelongToComponent(PCMComponent component);

    PCMComponent getComponentOfClassOrInterface(JavaClassOrInterface classOrInterface);
}
