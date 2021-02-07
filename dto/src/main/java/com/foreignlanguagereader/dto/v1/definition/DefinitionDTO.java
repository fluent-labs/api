package com.foreignlanguagereader.dto.v1.definition;

import com.foreignlanguagereader.dto.v1.word.PartOfSpeechDTO;

import java.util.List;

public class DefinitionDTO {
    private final String id;
    private final List<String> subdefinitions;
    private final PartOfSpeechDTO tag;
    private final DefinitionSourceDTO source;
    private final List<String> examples;

    public DefinitionDTO(String id, List<String> subdefinitions, PartOfSpeechDTO tag, DefinitionSourceDTO source, List<String> examples) {
        this.id = id;
        this.subdefinitions = subdefinitions;
        this.tag = tag;
        this.examples = examples;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public List<String> getSubdefinitions() {
        return subdefinitions;
    }

    public PartOfSpeechDTO getTag() {
        return tag;
    }

    public DefinitionSourceDTO getSource() {
        return source;
    }

    public List<String> getExamples() {
        return examples;
    }
}
