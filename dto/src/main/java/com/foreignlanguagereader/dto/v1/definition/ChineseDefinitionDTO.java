package com.foreignlanguagereader.dto.v1.definition;

import com.foreignlanguagereader.dto.v1.definition.chinese.HSKLevel;
import com.foreignlanguagereader.dto.v1.word.PartOfSpeechDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChineseDefinitionDTO extends DefinitionDTO {
    private Optional<String> simplified;
    private Optional<List<String>> traditional;
    private String pronunciation;
    private HSKLevel hsk;

    public ChineseDefinitionDTO(String id, List<String> subdefinitions, PartOfSpeechDTO tag, List<String> examples,
                                Optional<String> simplified, Optional<List<String>> traditional, String pronunciation, HSKLevel hsk) {
        super(id, subdefinitions, tag, examples);
        this.simplified = simplified;
        this.traditional = traditional;
        this.pronunciation = pronunciation;
        this.hsk = hsk;
    }
}
