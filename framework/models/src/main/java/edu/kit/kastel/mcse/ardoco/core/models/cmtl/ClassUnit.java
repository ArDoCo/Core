package edu.kit.kastel.mcse.ardoco.core.models.cmtl;

import java.util.HashSet;
import java.util.Set;

public class ClassUnit extends Datatype {

    private Set<CodeItem> content;

    public ClassUnit(String name, Set<? extends CodeItem> content) {
        super(name);
        this.content = new HashSet<>(content);
    }

    @Override
    public Set<CodeItem> getContent() {
        return new HashSet<>(content);
    }

    @Override
    public Set<Datatype> getAllDatatypes() {
        Set<Datatype> result = new HashSet<>();
        result.add(this);
        getContent().forEach(c -> result.addAll(c.getAllDatatypes()));
        return result;
    }
}
