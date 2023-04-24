/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.old.uml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.fuchss.xmlobjectmapper.XML2Object;
import org.fuchss.xmlobjectmapper.XMLException;

import edu.kit.kastel.mcse.ardoco.core.models.old.uml.xml_elements.OwnedOperation;
import edu.kit.kastel.mcse.ardoco.core.models.old.uml.xml_elements.PackagedElement;
import edu.kit.kastel.mcse.ardoco.core.models.old.uml.xml_elements.Reference;

public class UMLModel {
    private UMLModelRoot model;

    public UMLModel(File umlModel) throws IOException {
        try (var inputStream = new FileInputStream(umlModel)) {
            this.load(inputStream);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public UMLModel(InputStream umlModelStream) throws IllegalArgumentException {
        this.load(umlModelStream);
    }

    public UMLModelRoot getModel() {
        return model;
    }

    private void load(InputStream repositoryStream) throws IllegalArgumentException {
        try (repositoryStream) {
            XML2Object xml2Object = new XML2Object();
            xml2Object.registerClasses(UMLModelRoot.class, PackagedElement.class, OwnedOperation.class, Reference.class);
            model = xml2Object.parseXML(repositoryStream, UMLModelRoot.class);
            model.init();
        } catch (ReflectiveOperationException | IOException | XMLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
