package com.foreignlanguagereader.api.controller.v1

import io.fluentlabs.content.types.Language.{Language, fromString}
import play.api.mvc.PathBindable

class PathBinders {
  implicit def languagePathBinder: PathBindable[Language] =
    new PathBindable[Language] {
      override def bind(key: String, value: String): Either[String, Language] =
        fromString(value) match {
          case Some(language) =>
            Right(language)
          case _ => Left(value)
        }
      override def unbind(key: String, value: Language): String = value.toString
    }
}
