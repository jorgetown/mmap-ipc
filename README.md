# mmap-ipc
Simple IPC via shared memory/memory mapped files


To build and run:

```
mvn clean install
```

Followed by:
```
java Prime
java Randomizer
```


Implementation Notes:

### Randomizer
As per 'spec', Randomizer has the following responsibilities:
+ add positive integers to a queue implementation
+ read answers from a queue implementation

Their functionality is unit tested separately from that of their dependencies.

### Prime
As per 'spec', Prime has the following responsibilities:
+ read positive integers from a queue implementation
+ add primality test answers to a queue implementation

Typical IPC options are sockets and files. The shared memory implementation provided seems natural given that both processes run on the same host.

Queue writer and reader are separate for illustration convenience and testability.

Prime lags Randomizer because of its more computational expensive primality test. Nevertheless, I clocked a decent 2+ million answers/second on my dual core MBP which is good enough for the 'spec' given.

Should this be considered a performance bottleneck, there are trivial improvements that could be done such as batching and/or calculating several primes in parallel, e.g.:

```
Stream.generate(intsQueue::poll)
.filter(Objects::nonNull)
.parallel()
.forEach(i -> {
    Answer answer = primality.apply(i);
    answersQueue.add(answer); // Queue implementation should change to accommodate multiple writers
});
```


Prime and Randomizer stop when they fill the backing file (~2GBs of data). Support for much larger quantities of data (TBs) can be done by linking chunks of ~MAX_INT bytes but was beyond the goal of this exercise.
A day's worth of Randomizer/Prime would require ~200GBs with the current approach.