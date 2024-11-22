# 100 Prisoners Problem

## The Riddle

The riddle involves 100 prisoners numbered from 0 to 99 and a room with 100 boxes numbered in the same range. Each box contains a slip of paper with a distinct random number from 0 to 99. Every prisoner is allowed to enter the room, open up to 50 boxes, and try to find the slip of paper with his corresponding number.

If all prisoners find their numbers, they are set free.

The prisoners are allowed to decide on a strategy before the process begins, but they are forbidden from communicating with each other once the first inmate enters the room. They should also leave the room exactly as they found it, with all boxes closed.

### The Lack of Strategy

If the prisoners decide to open boxes without any strategy, the probability of all of them finding their numbers is `(50/100) * (50/100) * ... * (50/100)` (100 times), which is `(1/2)^100`, or `0.0000000000000000000000000000008`. Their success is practically impossible.

### The Strategy

Before the process begins, the inmates decide to follow this plan:
- Each prisoner will first open the box numbered with his own ID.
- If he finds his number inside, he will stop.
- If not, he will next open the box numbered with the number he found in the slip of paper inside the previous box.
- He will continue this process until he finds his own ID or open 50 boxes.

An example of this strategy is as follows:
- The prisoner with ID 0 will first open box 0. He will find a number, say 42, inside. He will then open box 42 and find the number 0. He will stop and leave the room.
- The prisoner with ID 1 will first open box 1. He will find a number, say 1, inside. He will stop and leave the room.
- The prisoner with ID 2 will first open box 2. He will find a number, say 99, inside. He will then open box 99 and find the number 55. He will then open box 55 and find the number 2. He will stop and leave the room.

Up to this point, these prisoners have found their numbers. But they will all be set free only if all prisoners find their numbers before opening 50 boxes each.

### The Representation in Code

We will code the riddle and the strategy in Scala and run it multiple times to estimate the probability of the prisoners being set free. We need two main data structures to track what's happening:
1. The `boxes` is a vector of 100 shuffled integers. The index of each element will represent the box number, and the value of each element will represent the number that is written on the slip of paper inside that box. Throughout a single experiment, the size and the content of this vector will not change.
2. The `openNumbers` will represent the numbers that the prisoner has found so far. It will be a vector of integers that starts empty and grows as the prisoner opens boxes. The last element of this vector is the number of the box that the prisoner has to open next.

### The Flow of the Process

We will define a couple of functions. The first one will make the state evolve, and the second one will check if the state is terminal.

1. The `nextOpenNumbers` function will return the `openNumbers` vector with the number found in the last box appended to it. If the `openNumbers` vector is empty, it means that the prisoner has just entered the room. In this case, the prisoner will open the box numbered with his own ID. Otherwise, the prisoner will open the box numbered with the last element of the `openNumbers` vector.
2. The `isVisitSuccessful` function will check the termination of a prisoner's visit and its end result. If the size of the `openNumbers` vector is greater than 50, the visit is considered complete and failed. If the last element of the `openNumbers` vector is the prisoner's ID, the visit is considered complete and successful. Otherwise, the visit is considered incomplete.

### The Code Itself

If we glue the above-mentioned data structures and functions together, we may come up with something like this:

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

### The Result

Running this code will give us an estimation of the probability of the prisoners being set free, which is around `0.31`.

[This](https://www.youtube.com/watch?v=iSNsgj1OCLA) video by Veritasium gives a nice description of the riddle and an excellent explanation of how the strategy increases the probability from almost zero to `31%`.

In the next sections, we will see some aspects of the strategy's behavior to better understand what's happening.

### Tracing the Behavior of the Strategy

Let's assume that we have only 6 prisoners, 6 boxes, a threshold of 3, and a single experiment.

```scala 3
val numOfPrisoners = 6
val threshold = 3
val experiments = 1
```

Let's also assume that we add a few print statements or breakpoints at the right places to track the state of the system at specific points in time:

```
Boxes: Vector(5, 4, 0, 1, 3, 2)
Prisoner 0: Vector(5, 2, 0)
Prisoner 0 succeed: true
Prisoner 1: Vector(4, 3, 1)
Prisoner 1 succeed: true
Prisoner 2: Vector(0, 5, 2)
Prisoner 2 succeed: true
Prisoner 3: Vector(1, 4, 3)
Prisoner 3 succeed: true
Prisoner 4: Vector(3, 1, 4)
Prisoner 4 succeed: true
Prisoner 5: Vector(2, 0, 5)
Prisoner 5 succeed: true
```

Examining the `Boxes` vector can help us understand the outcome of the experiment. Let's represent the box `Vector(5, 4, 0, 1, 3, 2)` as a collection of key-value pairs, where the key is the box number and the value is what's written on the slip of paper inside that box.

```
0 -> 5
1 -> 4
2 -> 0
3 -> 1
4 -> 3
5 -> 2
```

Now, let's treat it as a directed graph, where the key is the source node and the value is the destination. If we pay attention to this graph, we will see that it forms two cycles. The first cycle consists of the nodes `0, 5, 2`, and the second cycle consists of the nodes `1, 4, 3`.

It doesn't matter which node the prisoner starts from. The prisoner ends up in his starting node after traversing a 3-sized cycle.

The prisoner `0` followed the path `0 -> 5 -> 2 -> 0`. It succeeded in finding its own number without exceeding the threshold of 3. At this point, we know that the prisoners `5` and `2` will also succeed because they belong to the same cycle.

## The Riddle in a New Form

Instead of talking about jail rooms and prisoner visits, we can represent the riddle in a more abstract way: If we shuffle the numbers from 0 to 99 and place them inside 100 numbered boxes, what is the probability of the largest formed cycle being at most 50?

In the following last sections, we will see how we can code this new form of the riddle.

### The Representation in the New Code

We need two main data structures to track what's happening:

1. The `openBoxes` is a vector of key-value pairs. The first element of each pair will represent the box number that we have opened, and the second element will represent the number we found inside that box. This vector will start empty and grow as we open more boxes.
2. The `closedBoxes` is a map of key-value pairs. The key will represent the box number, and the value will represent the number that is written inside that box. At the beginning, this map will contain 100 keys that point to 100 shuffled values. As the experiment proceeds, the size of this map will decrease.

### The Flow of the New Process

As we did before, we will define a couple of functions. The first one will make the state evolve, and the second one will check if the state is terminal.

1. The `nextOpenAndClosedBoxes` function will return the tuple of our two main data structures. The first element of the tuple is the `openBoxes` vector with the key-value pair of the most recently opened box appended to it. The second element of the tuple is the `closedBoxes` map with the most recently opened box removed from it. There are two special cases we need to pay attention to:
  a. If `openBoxes` is empty, it means that we have not opened any boxes yet. We will open any box, and we will move its key-value pair from the `closedBoxes` map to the `openBoxes` vector.
  b. If the first element of the first pair is equal to the second element of the last pair in the `openBoxes` vector, it means that we have formed a cycle.
2. The `isLargeCycleDetected` function will check the termination of the experiment and its end result. If the size of the `openBoxes` vector is greater than 50, the experiment will be considered complete and failed. If the `closedBoxes` map is empty, the experiment will be considered complete and successful. There is also a special case where we look for a shortcut:
  a. If the `openBoxes` vector is empty we know that a search for a new cycle has just started. In this case, if the size of the `closedBoxes` map is not greater than 50, we don't need to continue the process. We know for sure that all remaining cycles will be small.

### The New Code

Again, if we glue the above-mentioned data structures and functions together, we may come up with something like this:

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

Running this code will print something like `Success rate: 0.31`.

* In this version of the problem, the range of the prisoner and box numbers is from 0 to 99. That way it's easier to use as indices in array-like structures.
* All prisoners are male. If they were gender-agnostic, the text would be difficult to follow. So, I decided to use the "he" pronoun when I refer to a single prisoner, and "they" when I refer to multiple prisoners.
* I grouped the parameters of the functions to perform currying, partially apply them, and make the code more readable.
* Iterators are memory-efficient because they lazily evaluate elements one at a time. They don't store all elements in memory at once.
