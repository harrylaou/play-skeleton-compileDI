package com.harrylaou.play.commons

import org.scalatest.{BeforeAndAfterAll, EitherValues, TestSuite}
import org.scalatestplus.play.PlaySpec

/** Common spec without fakeApplication
  */
trait CommonSpec extends PlaySpec with EitherValues with BeforeAndAfterAll {
  this: TestSuite =>
}
