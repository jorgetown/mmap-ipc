# mmap-ipc
IPC programming exercise using memory mapped file(s).


### Spec
Create two small applications: one called Randomizer; the other, Prime.

Randomizer‘s job is to generate a series of positive random integers and send those to Prime via a distributed queue of integers.

Prime's job is to receive the integers, calculate whether the integer is a prime or not, and return the answer to Randomizer via a distributed queue that contains the original number and a Boolean -which Randomizer will print to the console.

1. Use only the standard java library
2. Both applications will run on the same host
3. The system should be as fast as possible
4. The results do not have to be returned in the same order as received


### Implementation Notes:
IPC via shared memory is a sensible choice because both Randomizer and Prime processes run on the same host.

To build and run:

```
mvn clean install
cd classes/
java jdc.Prime
java jdc.Randomizer
```


##### Randomizer
As per 'spec', Randomizer has the following responsibilities:
+ add positive integers to a queue
+ read answers from a queue

These responsibilities are unit tested separately from those of its dependencies.


##### Prime
As per 'spec', Prime has the following responsibilities:
+ read positive integers from a queue
+ add primality-tested answers to a queue

Like Randomizer, Prime's responsibilities are unit tested separately from those of its dependencies.

Queue reader and writer are separate for illustration convenience and testability.


### Performance Notes
Prime lags Randomizer due to its more computationally expensive primality test.

If Prime is considered a performance bottleneck, after improving the low hanging fruit (e.g.: calculate MAX_INT primes offline and load upfront, etc) and after careful performance analysis, techniques such as batching and/or calculating several primes in parallel can be implemented and tested, e.g.:

```
Stream.generate(intsQueue::poll)
.filter(Objects::nonNull)
.parallel()
.forEach(i -> {
    Answer answer = primality.apply(i);
    answersQueue.add(answer); // Queue implementation should change to accommodate multiple writers
});
```


Prime and Randomizer stop when their backing file is full -currently after ~2GBs of data. Support for much larger quantities of data (TBs) is possible but beyond the scope of this simple exercise.

