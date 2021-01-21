package com.foreignlanguagereader.api.authentication

import pdi.jwt.JwtClaim
import play.api.mvc.{Request, WrappedRequest}

case class AuthenticatedRequest[A](
    jwt: JwtClaim,
    token: String,
    request: Request[A]
) extends WrappedRequest[A](request)
