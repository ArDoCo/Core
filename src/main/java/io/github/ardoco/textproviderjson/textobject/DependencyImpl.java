package io.github.ardoco.textproviderjson.textobject;

import io.github.ardoco.textproviderjson.DependencyType;

import java.util.Objects;

public class DependencyImpl {

    private final DependencyType dependencyType;
    private final long wordId;

    public DependencyImpl(DependencyType type, long wordId) {
        this.dependencyType = type;
        this.wordId = wordId;
    }

    public long getWordId() {
        return wordId;
    }

    public DependencyType getDependencyType() {
        return dependencyType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DependencyImpl that = (DependencyImpl) o;
        return wordId == that.wordId && dependencyType == that.dependencyType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencyType, wordId);
    }
}
