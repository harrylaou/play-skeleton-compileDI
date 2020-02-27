package commons

import application.logging.AppLogging
import org.scalatest.{BeforeAndAfterAll, EitherValues, TestSuite}
import org.scalatestplus.play.PlaySpec

/**
  * Common spec without fakeAplication
  */
trait CommonSpec extends PlaySpec with EitherValues with BeforeAndAfterAll with AppLogging {
  this: TestSuite =>
}
