package prisoners

import scala.util.Random

val numOfPrisoners = 100
val threshold = 50
val experiments = 10000

def shuffledNumbers: Vector[Int] =
  val vec = Vector.range(0, numOfPrisoners)
  Random.shuffle(vec)

def nextOpenNumbers(boxes: Vector[Int], prisonerId: Int)(openNumbers: Vector[Int]): Vector[Int] =
  val nextBoxIndex = openNumbers.lastOption.getOrElse(prisonerId)
  openNumbers :+ boxes(nextBoxIndex)

def isVisitSuccessful(prisonerId: Int)(openNumbers: Vector[Int]): Option[Boolean] =
  openNumbers match
    case vec if vec.size > threshold => Some(false)
    case _ :+ `prisonerId` => Some(true)
    case _ => None

def isPrisonerFree(boxes: Vector[Int])(prisonerId: Int): Boolean =
  Iterator
    .iterate(Vector.empty[Int])(nextOpenNumbers(boxes, prisonerId))
    .map(isVisitSuccessful(prisonerId))
    .collectFirst { case Some(result) => result }
    .get

def areAllPrisonersFree: Boolean =
  val boxes = shuffledNumbers
  Iterator
    .range(0, numOfPrisoners)
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
