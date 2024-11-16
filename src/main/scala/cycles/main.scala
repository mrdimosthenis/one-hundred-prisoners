package cycles

import scala.annotation.tailrec
import scala.util.Random

val numOfBoxes = 100
val threshold = 50
val experiments = 10000

@tailrec
def largeCycleExists(closedBoxes: Map[Int, Int], openBoxes: Vector[(Int, Int)]): Boolean =
  val isCycleComplete = openBoxes.nonEmpty && openBoxes.head._1 == openBoxes.last._2
  if openBoxes.size > threshold then
    true
  else if closedBoxes.isEmpty then
    false
  else if isCycleComplete && closedBoxes.size <= threshold then
    false
  else if isCycleComplete then
    largeCycleExists(closedBoxes, Vector())
  else
    val nextBoxKey = openBoxes.lastOption.map(_._2).getOrElse(closedBoxes.keys.head)
    val nextBoxVal = closedBoxes(nextBoxKey)
    val updatedClosedBoxes = closedBoxes - nextBoxKey
    val updatedOpenBoxes = openBoxes :+ nextBoxKey -> nextBoxVal
    largeCycleExists(updatedClosedBoxes, updatedOpenBoxes)

def initBoxes: Map[Int, Int] =
  val ids = 1 to numOfBoxes
  val shuffledIds = Random.shuffle(ids)
  ids.zip(shuffledIds).toMap

@main
def main(): Unit =
  val successes =
    Iterator
      .continually(largeCycleExists(initBoxes, Vector()))
      .take(experiments)
      .count(identity)
      .toDouble
  println(s"Success rate: ${successes / experiments}")
