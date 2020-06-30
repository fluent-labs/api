package com.foreignlanguagereader.api.domain.definition.entry.webster.common

import play.api.libs.json._

/**
  * This file is for all the common parts of the webster schema
  * Anything that would be used by multiple dictionaries should be here
  */
// TODO big time this is not really implemented
case class WebsterSense(dt: WebsterDefiningText,
                        et: Option[Seq[Seq[String]]],
                        ins: Option[Seq[WebsterInflection]],
                        prs: Option[Seq[WebsterPronunciation]],
                        sdsense: Option[WebsterSense],
                        sgram: Option[String],
                        sls: Option[Seq[String]],
                        // Sense number - basically an id - probably don't need this.
                        sn: Option[String],
                        variations: Option[Seq[WebsterVariant]],
                        senseDivider: Option[String]) {}
object WebsterSense {
//  implicit val reads =
//  implicit val format: Format[WebsterSense] = Json.format[WebsterSense]
}
