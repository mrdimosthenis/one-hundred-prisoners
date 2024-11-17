package prisoners

import scala.util.Random

val numOfPrisoners = 100
val threshold = 50
val experiments = 10000

def randomBoxConfiguration: Map[Int, Int] =
  val ids = 1 to numOfPrisoners
  val shuffledIds = Random.shuffle(ids)
  ids.zip(shuffledIds).toMap

def isVisitComplete(prisoner: Int)(openNumbers: Vector[Int]): Option[Boolean] =
  openNumbers match
    case vec if vec.size > threshold => Some(false)
    case _ :+ `prisoner` => Some(true)
    case _ => None

def nextOpenNumbers(boxes: Map[Int, Int], prisoner: Int)(openNumbers: Vector[Int]): Vector[Int] =
  val nextBoxId = openNumbers.lastOption.getOrElse(prisoner)
  val nextOpenNum = boxes(nextBoxId)
  openNumbers :+ nextOpenNum

def isPrisonerFree(boxes: Map[Int, Int])(prisoner: Int): Boolean =
  Iterator
    .iterate(Vector.empty[Int])(nextOpenNumbers(boxes, prisoner))
    .map(isVisitComplete(prisoner))
    .collectFirst { case Some(result) => result }
    .get

def areAllPrisonersFree: Boolean =
  val boxes = randomBoxConfiguration
  Iterator
    .from(1)
    .take(numOfPrisoners)
    .forall(isPrisonerFree(boxes))

@main
def main(): Unit =
  val successes =
    Iterator
      .continually(areAllPrisonersFree)
      .take(experiments)
      .count(identity)
      .toDouble
  println(s"Success rate: ${successes / experiments}")
