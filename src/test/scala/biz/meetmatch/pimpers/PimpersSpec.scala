package biz.meetmatch.pimpers

import biz.meetmatch.UnitSpec
import biz.meetmatch.util.CollectionPimpers
import org.slf4j.LoggerFactory

class PimpersSpec extends UnitSpec {
  private val logger = LoggerFactory.getLogger(this.getClass)

  it should "take a top of a collection" in {
    import CollectionPimpers._

    val collection = (1 to 100).map(i => (i, i))
    val reverseTop10 = collection.top(5)(-1 * _._1)

    reverseTop10.length should be(5)
    reverseTop10.head should be((100, 100))

    val reverseTop10Par = collection.par.top(5)(-1 * _._1)

    reverseTop10Par should have length 5
    reverseTop10Par.head should be((100, 100))
  }
}
