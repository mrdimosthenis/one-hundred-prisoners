import scala.annotation.tailrec
import scala.util.Random

val prisoners = 100
val threshold = 50
val experiments = 10000

@tailrec
def canEscape(boxes: Map[Int, Int], prisonerId: Int, revealedNumbers: Vector[Int]): Boolean =
  val nextBoxId = revealedNumbers.lastOption.getOrElse(prisonerId)
  val nextRevealedNum = boxes(nextBoxId)
  val newRevealedNumbers = revealedNumbers :+ nextRevealedNum
  if newRevealedNumbers.size > threshold then
    false
  else if nextRevealedNum == prisonerId then
    true
  else
    canEscape(boxes, prisonerId, newRevealedNumbers)

def canAllEscape: Boolean =
  val ids = 1 to prisoners
  val shuffledIds = Random.shuffle(ids)
  val boxes = ids.zip(shuffledIds).toMap
  ids.forall(prisonerId => canEscape(boxes, prisonerId, Vector()))

@main
def main(): Unit =
  val successes = (1 to experiments).count(_ => canAllEscape).toDouble
  println(s"Success rate: ${successes / experiments}")
