package com.foreignlanguagereader.dto.v1.word;

import com.foreignlanguagereader.dto.v1.definition.DefinitionDTO;

import java.util.List;

public class WordDTO {
    private final String token;
    private final String processedToken;
    private final String tag;
    private final String lemma;
    private final List<DefinitionDTO> definitions;

    public WordDTO(String token, String processedToken, String tag, String lemma, List<DefinitionDTO> definitions) {
        this.token = token;
        this.processedToken = processedToken;
        this.tag = tag;
        this.lemma = lemma;
        this.definitions = definitions;
    }

    public String getTag() {
        return tag;
    }

    public String getLemma() {
        return lemma;
    }

    public List<DefinitionDTO> getDefinitions() {
        return definitions;
    }

    public String getToken() {
        return token;
    }

    public String getProcessedToken() {
        return processedToken;
    }
}
