package com.foreignlanguagereader.domain.user

import com.mohiva.play.silhouette.api.Identity

case class User(
    email: String,
    password: String,
    name: String
) extends Identity
