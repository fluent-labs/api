package com.foreignlanguagereader.dto.v1.word;

public class WordDTO {
    private final String token;
    private final String processedToken;
    private final String tag;
    private final String lemma;
    private final Boolean isPunctuation;
    private final Boolean isNumber;

    public WordDTO(String token, String processedToken, String tag, String lemma, Boolean isPunctuation, Boolean isNumber) {
        this.token = token;
        this.processedToken = processedToken;
        this.tag = tag;
        this.lemma = lemma;
        this.isPunctuation = isPunctuation;
        this.isNumber = isNumber;
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

    public Boolean getIsPunctuation() {
        return isPunctuation;
    }

    public Boolean getIsNumber() {
        return isNumber;
    }
}
