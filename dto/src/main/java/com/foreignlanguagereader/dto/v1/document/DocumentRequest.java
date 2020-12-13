package com.foreignlanguagereader.dto.v1.document;

public class DocumentRequest {
    private final String text;

    public DocumentRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
