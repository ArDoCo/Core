/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Connector implements Serializable {
    private String uuid = UUID.randomUUID().toString();
    private List<String> connectedBoxes;
    private List<TextBox> texts = new ArrayList<>();

    private Connector() {
        // Jackson JSON
    }

    public Connector(String uuid, List<String> connectedBoxes, List<TextBox> texts) {
        this.uuid = uuid;
        this.connectedBoxes = connectedBoxes;
        this.texts = texts;
    }

    public String getUUID() {
        return uuid;
    }

    public List<String> getConnectedBoxes() {
        return new ArrayList<>(connectedBoxes);
    }

    public List<TextBox> getTexts() {
        return new ArrayList<>(texts);
    }
}
