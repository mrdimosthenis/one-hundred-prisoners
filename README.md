# 100 Prisoners Problem

## The Riddle

The riddle involves 100 prisoners numbered from 0 to 99, and a room with 100 boxes numbered
in the same range. Each box contains a slip of paper with a distinct random number from 0 to 99.
Every prisoner is allowed to enter the room, open up to 50 boxes, and try to find the slip of
paper with their corresponding number.

If all prisoners find their numbers, they are set free.

The prisoners are allowed to decide on a strategy before the process begins, but they are
forbidden from communicating with each other once the first inmate enters the room.
They should also leave the room the exact same way they found it, with all boxes closed.

### The Lack of Strategy

If the prisoners decide to open boxes without any strategy, the probability of all of them
finding their numbers is `(50/100) * (50/100) * ... * (50/100)` (100 times),
which is `(1/2)^100`, or `0.0000000000000000000000000000008`. Their success is practically
impossible.

### The Strategy






```scala 3
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
```

```scala 3
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
```


