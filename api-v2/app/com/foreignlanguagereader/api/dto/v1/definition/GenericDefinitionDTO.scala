package com.foreignlanguagereader.api.dto.v1.definition

case class GenericDefinitionDTO(subdefinitions: List[String],
                                tag: String,
                                examples: List[String])
    extends DefinitionDTO
