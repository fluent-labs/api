package com.foreignlanguagereader.api.client

import com.foreignlanguagereader.api.Language.Language
import com.foreignlanguagereader.api.ReadinessStatus.ReadinessStatus
import com.foreignlanguagereader.api.domain.definition.combined.Definition

class ElasticsearchClient {
  def checkConnection: ReadinessStatus = ???

  def getDefinition(wordLanguage: Language,
                    definitionLanguage: Language,
                    word: String): Option[Seq[Definition]] = ???
}
