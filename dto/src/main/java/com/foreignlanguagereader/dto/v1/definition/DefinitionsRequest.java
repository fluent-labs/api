package com.foreignlanguagereader.dto.v1.definition;

import java.util.List;

public class DefinitionsRequest {
    private final List<String> words;

    public DefinitionsRequest(List<String> words) {
        this.words = words;
    }

    public List<String> getWords() {
        return words;
    }
}
