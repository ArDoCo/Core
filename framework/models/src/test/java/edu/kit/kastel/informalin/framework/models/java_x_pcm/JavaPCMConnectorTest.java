/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.informalin.framework.models.java_x_pcm;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import edu.kit.kastel.informalin.framework.models.java.JavaClassOrInterface;
import edu.kit.kastel.informalin.framework.models.java.JavaProject;
import edu.kit.kastel.informalin.framework.models.pcm.PCMComponent;
import edu.kit.kastel.informalin.framework.models.pcm.PCMInterface;
import edu.kit.kastel.informalin.framework.models.pcm.PCMModel;
import edu.kit.kastel.informalin.framework.models.pcm.PCMRepository;

class JavaPCMConnectorTest {

    private JavaPCMConnectorImpl javaPCMConnector;
    private JavaClassOrInterface class1;
    private JavaClassOrInterface class2;
    private JavaClassOrInterface interface1;
    private JavaClassOrInterface interface2;
    private PCMComponent component1;
    private PCMComponent component2;

    @BeforeEach
    void setup() {
        JavaProject javaProject = Mockito.mock(JavaProject.class);
        PCMModel pcmModel = Mockito.mock(PCMModel.class);
        PCMRepository pcmRepository = Mockito.mock(PCMRepository.class);
        Mockito.when(pcmModel.getRepository()).thenReturn(pcmRepository);

        class1 = mockClass("test.JavaClass1", true);
        class2 = mockClass("test.JavaClass2", true);
        Mockito.when(javaProject.getClasses()).thenReturn(List.of(class1, class2));
        interface1 = mockClass("test.JavaInterface1", false);
        interface2 = mockClass("test.JavaInterface2", false);
        Mockito.when(javaProject.getClassesAndInterfaces()).thenReturn(List.of(class1, class2, interface1, interface2));

        component1 = mockComponent("id1");
        component2 = mockComponent("id2");
        Mockito.when(pcmRepository.getComponents()).thenReturn(List.of(component1, component2));

        this.javaPCMConnector = new JavaPCMConnectorImpl(javaProject, pcmModel);
    }

    @Test
    void testGetClassesThatBelongToComponent() {
        Assertions.assertEquals(0, javaPCMConnector.getClassesThatBelongToComponent(component1).size());
        javaPCMConnector.addClassToComponent(class1, component1);
        Assertions.assertEquals(1, javaPCMConnector.getClassesThatBelongToComponent(component1).size());

        Assertions.assertThrows(IllegalStateException.class, () -> javaPCMConnector.addClassToComponent(class1, component2));
        Assertions.assertThrows(IllegalArgumentException.class, () -> javaPCMConnector.addClassToComponent(mockClass("test", true), component2));
        Assertions.assertThrows(IllegalArgumentException.class, () -> javaPCMConnector.addClassToComponent(class2, mockComponent("idTest")));

        Assertions.assertEquals(1, javaPCMConnector.getClassesThatBelongToComponent(component1).size());

        javaPCMConnector.addClassToComponent(class2, mockComponent("id1"));

        Assertions.assertEquals(2, javaPCMConnector.getClassesThatBelongToComponent(component1).size());
        Assertions.assertEquals(0, javaPCMConnector.getClassesThatBelongToComponent(component2).size());
    }

    @Test
    void testGetInterfacesThatBelongToComponent() {
        Assertions.assertEquals(0, javaPCMConnector.getClassesThatBelongToComponent(component1).size());
        javaPCMConnector.addInterfaceToComponent(interface1, component1);
        Assertions.assertEquals(1, javaPCMConnector.getInterfacesThatBelongToComponent(component1).size());

        Assertions.assertThrows(IllegalStateException.class, () -> javaPCMConnector.addInterfaceToComponent(interface1, component2));
        Assertions.assertThrows(IllegalArgumentException.class, () -> javaPCMConnector.addInterfaceToComponent(mockClass("test", false), component2));
        Assertions.assertThrows(IllegalArgumentException.class, () -> javaPCMConnector.addInterfaceToComponent(interface2, mockComponent("idTest")));

        Assertions.assertEquals(0, javaPCMConnector.getClassesThatBelongToComponent(component1).size());
        Assertions.assertEquals(1, javaPCMConnector.getInterfacesThatBelongToComponent(component1).size());

        javaPCMConnector.addInterfaceToComponent(interface2, mockComponent("id1"));

        Assertions.assertEquals(2, javaPCMConnector.getInterfacesThatBelongToComponent(component1).size());
        Assertions.assertEquals(0, javaPCMConnector.getInterfacesThatBelongToComponent(component2).size());
    }

    @Test
    void testGetComponentOfClassOrInterface() {
        Assertions.assertNull(javaPCMConnector.getComponentOfClassOrInterface(interface1));
        Assertions.assertNull(javaPCMConnector.getComponentOfClassOrInterface(interface2));
        Assertions.assertNull(javaPCMConnector.getComponentOfClassOrInterface(class1));
        Assertions.assertNull(javaPCMConnector.getComponentOfClassOrInterface(class2));

        javaPCMConnector.addInterfaceToComponent(interface1, component1);
        javaPCMConnector.addInterfaceToComponent(interface2, mockComponent("id1"));
        javaPCMConnector.addClassToComponent(class1, component1);
        javaPCMConnector.addClassToComponent(class2, mockComponent("id1"));

        Assertions.assertEquals(component1, javaPCMConnector.getComponentOfClassOrInterface(interface1));
        Assertions.assertEquals(component1, javaPCMConnector.getComponentOfClassOrInterface(interface2));
        Assertions.assertEquals(component1, javaPCMConnector.getComponentOfClassOrInterface(class1));
        Assertions.assertEquals(component1, javaPCMConnector.getComponentOfClassOrInterface(class2));
    }

    private JavaClassOrInterface mockClass(String fqn, boolean isClass) {
        var result = Mockito.mock(JavaClassOrInterface.class);
        Mockito.when(result.getFullyQualifiedName()).thenReturn(fqn);
        Mockito.when(result.isInterface()).thenReturn(!isClass);
        return result;
    }

    private PCMComponent mockComponent(String id) {
        var result = Mockito.mock(PCMComponent.class);
        Mockito.when(result.getId()).thenReturn(id);
        return result;
    }

    private PCMInterface mockInterface(String id) {
        var result = Mockito.mock(PCMInterface.class);
        Mockito.when(result.getId()).thenReturn(id);
        return result;
    }
}
