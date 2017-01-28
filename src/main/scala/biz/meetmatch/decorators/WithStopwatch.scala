package biz.meetmatch.decorators

import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration

object WithStopwatch {
  def apply[B](f: => B): B = {
    apply("")(f)
  }

  def apply[B](label: String)(f: => B): B = {
    val start = System.nanoTime

    val result = f

    val stop = System.nanoTime
    val elapsed = Duration.fromNanos(stop - start)
    val elapsedInHours = elapsed.toHours
    val elapsedInMinutes = elapsed.toMinutes % 60
    val elapsedInSeconds = elapsed.toSeconds % 60

    logger.info(s"Stopwatch: $label ${"%02d".format(elapsedInHours)}h${"%02d".format(elapsedInMinutes)}m${"%02d".format(elapsedInSeconds)}s")

    result
  }

  private val logger = LoggerFactory.getLogger(this.getClass)
}
