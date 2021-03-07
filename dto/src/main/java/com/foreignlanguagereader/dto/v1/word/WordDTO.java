package com.foreignlanguagereader.dto.v1.word;

import com.foreignlanguagereader.dto.v1.definition.DefinitionDTO;

import java.util.List;

public class WordDTO {
    private final String token;
    private final String processedToken;
    private final String tag;
    private final String lemma;

    public WordDTO(String token, String processedToken, String tag, String lemma) {
        this.token = token;
        this.processedToken = processedToken;
        this.tag = tag;
        this.lemma = lemma;
    }

    public String getTag() {
        return tag;
    }

    public String getLemma() {
        return lemma;
    }

    public String getToken() {
        return token;
    }

    public String getProcessedToken() {
        return processedToken;
    }
}
