package io.fluentlabs.api.controller.v1

import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.WsScalaTestClient

// Shim because scalatestplus is behind scalatest, so dependencies collide
// More: https://github.com/scalatest/scalatest/issues/1734
abstract class PlaySpec
    extends AnyWordSpec
    with Matchers
    with OptionValues
    with WsScalaTestClient
