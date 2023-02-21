package io.github.ardoco.textproviderjson.textobject;

import io.github.ardoco.textproviderjson.DependencyType;

public class DependencyImpl {

    private DependencyType dependencyType;
    private long wordId;

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
}
