package cycles

import scala.annotation.tailrec
import scala.util.Random

val prisoners = 100
val threshold = 50
val experiments = 10000

@tailrec
def canAllEscape(remainingBoxes: Map[Int, Int], revealedNumbers: Vector[Int]): Boolean =
  if revealedNumbers.size > threshold then
    false
  else if remainingBoxes.isEmpty then
    true
  else if revealedNumbers.isEmpty && remainingBoxes.size <= threshold then
    true
  else
    val nextBoxId = revealedNumbers.lastOption.getOrElse(remainingBoxes.keys.head)
    val nextRevealedNum = remainingBoxes(nextBoxId)
    val updatedRemainingBoxes = remainingBoxes - nextBoxId
    val updatedRevealedNumbers =
      revealedNumbers.headOption match
        case Some(i) if i == nextRevealedNum => Vector()
        case _ => revealedNumbers :+ nextRevealedNum
    canAllEscape(updatedRemainingBoxes, updatedRevealedNumbers)

def initBoxes: Map[Int, Int] =
  val ids = 1 to prisoners
  val shuffledIds = Random.shuffle(ids)
  ids.zip(shuffledIds).toMap

@main
def main(): Unit =
  val successes =
    Iterator
      .continually(canAllEscape(initBoxes, Vector()))
      .take(experiments)
      .count(identity)
      .toDouble
  println(s"Success rate: ${successes / experiments}")
