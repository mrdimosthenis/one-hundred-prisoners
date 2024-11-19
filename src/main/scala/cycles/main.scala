package cycles

import scala.util.Random

val numOfBoxes = 100
val threshold = 50
val experiments = 10000

def shuffledMapOfNumbers: Map[Int, Int] =
  val boxKeys = LazyList.range(0, numOfBoxes)
  val boxVals = Random.shuffle(boxKeys)
  boxKeys.zip(boxVals).toMap

def nextOpenAndClosedBoxes(openBoxes: Vector[(Int, Int)], closedBoxes: Map[Int, Int]):
(Vector[(Int, Int)], Map[Int, Int]) =
  if openBoxes.isEmpty then
    val (singletonMap, nextClosedBoxes) = closedBoxes.splitAt(1)
    val nextOpenBoxes = openBoxes :+ singletonMap.head
    (nextOpenBoxes, nextClosedBoxes)
  else if openBoxes.head._1 == openBoxes.last._2 then
    (Vector(), closedBoxes)
  else
    val nextBoxKey = openBoxes.last._2
    val nextOpenBoxes = openBoxes :+ nextBoxKey -> closedBoxes(nextBoxKey)
    val nextClosedBoxes = closedBoxes - nextBoxKey
    (nextOpenBoxes, nextClosedBoxes)

def isLargeCycleDetected(openBoxes: Vector[(Int, Int)], closedBoxes: Map[Int, Int]): Option[Boolean] =
  if openBoxes.size > threshold then Some(true)
  else if closedBoxes.isEmpty then Some(false)
  else if openBoxes.isEmpty && closedBoxes.size <= threshold then Some(false)
  else None

def areAllCyclesSmall(openBoxes: Vector[(Int, Int)], closedBoxes: Map[Int, Int]): Boolean =
  Iterator
    .iterate((openBoxes, closedBoxes))(nextOpenAndClosedBoxes)
    .map(isLargeCycleDetected)
    .collectFirst { case Some(result) => !result }
    .get

@main
def main(): Unit =
  val successes =
    Iterator
      .continually(areAllCyclesSmall(Vector(), shuffledMapOfNumbers))
      .take(experiments)
      .count(identity)
      .toDouble
  println(s"Success rate: ${successes / experiments}")
