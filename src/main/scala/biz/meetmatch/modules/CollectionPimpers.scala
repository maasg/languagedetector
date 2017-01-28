package biz.meetmatch.modules

import java.sql.Timestamp

import biz.meetmatch.pos.Tag
import org.apache.spark.sql.Dataset

import scala.collection.parallel.ParIterable

object CollectionPimpers {

  implicit class MapJoinPimper[K, V](map: Map[K, V]) {
    /**
      * Joins this map with a given one, keeping just entries with keys present at both maps and returning a new map
      * with their combined values.
      *
      * @param that map to join with.
      * @tparam VT the type of the other map values.
      * @return a new map of tuple values with each map value for those keys present at both this map and the other.
      */
    def joinInner[VT](that: Map[K, VT]): Map[K, (V, VT)] =
      for ((k, va) <- map; vb <- that.get(k)) yield k -> (va, vb)

    /**
      * Joins this map with a given one, keeping entries with keys present at both maps or just `this` and returning a
      * new map with the values at `this` and, optionally, their values at the right map.
      *
      * @param that map to join with.
      * @tparam VT the type of the other map values.
      * @return a new map of tuples of values from `this` and `None` or some values from the right map.
      */
    def joinOuterLeft[VT](that: Map[K, VT]): Map[K, (V, Option[VT])] =
      for ((k, va) <- map) yield k -> (va, that.get(k))

    /**
      * Joins this map with a given one, keeping entries with keys present at both or just the joined map and returning a
      * new map with the values at `that` and, optinonally, their values at `this`
      *
      * @param that map to join with.
      * @tparam VT the type of the other map values.
      * @return a new map of tuples of `None` or some values from `this` and values from the right map.
      */
    def joinOuterRight[VT](that: Map[K, VT]): Map[K, (Option[V], VT)] =
      for ((k, vb) <- that) yield k -> (map.get(k), vb)
  }

  implicit class TopPimper[A](iterable: Iterable[A]) {
    def top(top: Int)(orderBy: A => Double): List[A] = {
      val ord = Ordering.by[A, Double](orderBy)
      val queue = collection.mutable.PriorityQueue()(ord)
      val ordRev = ord.reverse

      iterable
        .foldLeft(queue) { (queue, item) =>
          if (queue.size < top) {
            queue.enqueue(item)
          } else if (ordRev.compare(item, queue.head) >= 0) {
            queue.dequeue
            queue.enqueue(item)
          }

          queue
        }
        .toList
        .sorted(ord)
    }
  }

  implicit class ParTopPimper[A](parIterable: ParIterable[A]) {
    def top(top: Int)(orderBy: A => Double): List[A] = {
      val ord = Ordering.by[A, Double](orderBy)
      val queue = collection.mutable.PriorityQueue()(ord)
      val ordRev = ord.reverse

      parIterable
        .foldLeft(queue) { (queue, item) =>
          if (queue.length < top) {
            queue.enqueue(item)
          } else if (ordRev.compare(item, queue.head) >= 0) {
            queue.dequeue
            queue.enqueue(item)
          }
          queue
        }
        .toList
        .sorted(ord)
    }
  }

}

