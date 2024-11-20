# 100 Prisoners Problem

## The Riddle

The riddle involves 100 prisoners numbered from 0 to 99, and a room with 100 boxes numbered
in the same range. Each box contains a slip of paper with a distinct random number from 0 to 99.
Every prisoner is allowed to enter the room, open up to 50 boxes, and try to find the slip of
paper with his corresponding number.

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

Before the process begins, the inmates decide to follow this plan: Each prisoner will first
open the box numbered with his own id. If he finds his number inside, he will stop.
If not, he will next open the box numbered with what he found in the slip of paper
inside the previous box. He will continue this process until he finds his own id or
open 50 boxes.

An example of this strategy is as follows:

* The prisoner with id 0 will first open box 0. He will find a number, say 42, inside.
  He will then open box 42 and find the number 0. He will stop and leave the room.
* The prisoner with id 1 will first open box 1. He will find a number, say 1, inside.
  He will stop and leave the room.
* The prisoner with id 2 will first open box 2. He will find a number, say 99, inside.
  He will then open box 99 and find the number 55. He will then open box 55 and find
  the number 2. He will stop and leave the room.
  Up to this point, these prisoners have found their numbers. But they will all set free
  only if all prisoners find their numbers before opening 50 boxes each.

### Probability Estimation

We will code the riddle and the strategy in Scala, and run it multiple times to estimate the
probability of the prisoners setting free.

#### The Representation in Code

We need two main data structures to track what's happening:

1. The `boxes` will be a vector of 100 shuffled integers. The index of each element will
   represent the box number, and the value of each element will represent the number that
   is written on the slip of paper inside that box. Throughout a single experiment, the
   size and the content of this vector will not change.
2. The `openNumbers` will represent the numbers that the prisoner has found so far. It will
   be a vector of integers that starts empty and grows as the prisoner opens more boxes.
   The last element of this vector will be the number of the box that the prisoner has
   to open next.

#### The Flow of the Process

We will define a couple of functions. The first one will make the state evolve, and the
second one will check if the state is terminal.

1. The `nextOpenNumbers` function will return the `openNumbers` vector with the number
   found in the last box appended to it. If the `openNumbers` vector is empty, it means
   that the prisoner has just entered the room. In this case, the prisoner will open
   the box numbered with his own id. Otherwise, the prisoner will open the box numbered
   with the last element of the `openNumbers` vector.
2. The `isVisitSuccessful` function will check the termination of a prisoner's visit and
   its end result. If the size of the `openNumbers` vector is greater than 50 the visit
   will be considered complete and failed. If the last element of the `openNumbers` vector
   is the prisoner's id, the visit will be considered complete and successful. Otherwise,
   the visit will be considered incomplete.

#### The Code Itself

If we glue the above-mentioned data structures and functions together, we may come up with
something like this:

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

Running this code will give us an estimation of the probability of the prisoners setting free,
which is around `0.31`.

In the next sections we will see why the strategy increased the probability from (almost) zero
to `31%`.

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

* In this version of the problem the range of the prisoner and box numbers is from 0 to 99.
  That way it's easier to be used as indices in array-like structures.
* Also, all prisoners are male. If they were gender-agnostic, the text would be difficult
  to follow. So, I decided to use the "he" pronoun when I refer to a single prisoner, and
  "they" when I refer to multiple prisoners.
* I grouped the parameters of the functions to perform currying, partially apply them, and
  make the code more readable.