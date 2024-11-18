package cycles

import scala.util.Random

val numOfBoxes = 100
val threshold = 50
val experiments = 10000

def shuffledMapOfNumbers: Map[Int, Int] =
  val boxKeys = LazyList.range(0, numOfBoxes)
  val boxVals = Random.shuffle(boxKeys)
  boxKeys.zip(boxVals).toMap

def nextOpenAndClosedBoxes(openBoxes: Vector[(Int, Int)], closedBoxes: Map[Int, Int]): (Vector[(Int, Int)], Map[Int, Int]) =
  (openBoxes, closedBoxes) match
    case (v, m) if v.isEmpty =>
      (Vector(m.head), m.tail)
    case (v, _) if v.nonEmpty && v.head._1 == v.last._2 =>
      (Vector(), closedBoxes)
    case (v, m) =>
      val nextOpenBoxes = v :+ v.last._2 -> closedBoxes(v.last._2)
      val nextClosedBoxes = closedBoxes - v.last._2
      (nextOpenBoxes, nextClosedBoxes)

def isLargeCycleDetected(openBoxes: Vector[(Int, Int)], closedBoxes: Map[Int, Int]): Option[Boolean] =
  (openBoxes, closedBoxes) match
    case (v, _) if v.size > threshold => Some(true)
    case (_, m) if m.isEmpty => Some(false)
    case (v, m) if v.isEmpty && m.size <= threshold => Some(false)
    case _ => None

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
