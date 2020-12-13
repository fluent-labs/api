package com.foreignlanguagereader.dto.v1.definition;

import com.foreignlanguagereader.dto.v1.word.PartOfSpeechDTO;
import lombok.Data;

import java.util.List;

@Data
public class DefinitionDTO {
    private String id;
    private List<String> subdefinitions;
    private PartOfSpeechDTO tag;
    private List<String> examples;

    public DefinitionDTO(String id, List<String> subdefinitions, PartOfSpeechDTO tag, List<String> examples) {
        this.id = id;
        this.subdefinitions = subdefinitions;
        this.tag = tag;
        this.examples = examples;
    }
}
