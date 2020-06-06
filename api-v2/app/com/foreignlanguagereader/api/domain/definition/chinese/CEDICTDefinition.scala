package com.foreignlanguagereader.api.domain.definition.chinese

case class CEDICTDefinition(subdefinitions: List[String],
                            pinyin: String,
                            simplified: String,
                            traditional: String)
