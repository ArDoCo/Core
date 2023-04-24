/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.old.pcm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.fuchss.xmlobjectmapper.XML2Object;
import org.fuchss.xmlobjectmapper.XMLException;

public class PCMModel {
    private PCMRepository repository;

    public PCMModel(File repository) throws IllegalArgumentException {
        try (var inputStream = new FileInputStream(repository)) {
            this.load(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public PCMModel(InputStream repositoryStream) throws IllegalArgumentException {
        this.load(repositoryStream);
    }

    public PCMRepository getRepository() {
        return repository;
    }

    private void load(InputStream repositoryStream) throws IllegalArgumentException {
        try (repositoryStream) {
            XML2Object xml2Object = new XML2Object();
            xml2Object.registerClasses(PCMRepository.class, PCMComponent.class, PCMInterface.class, PCMSignature.class, PCMComponent.InterfaceId.class);
            repository = xml2Object.parseXML(repositoryStream, PCMRepository.class);
            repository.init();
        } catch (ReflectiveOperationException | IOException | XMLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
