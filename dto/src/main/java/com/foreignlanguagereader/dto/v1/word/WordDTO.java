package com.foreignlanguagereader.dto.v1.word;

import com.foreignlanguagereader.dto.v1.definition.DefinitionDTO;
import lombok.Data;

import java.util.List;

@Data
public class WordDTO {
    private String token;
    private String tag;
    private String lemma;
    private List<DefinitionDTO> definitions;

    public WordDTO(String token, String tag, String lemma, List<DefinitionDTO> definitions) {
        this.token = token;
        this.tag = tag;
        this.lemma = lemma;
        this.definitions = definitions;
    }
}
