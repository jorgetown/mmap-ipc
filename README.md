# mmap-ipc
Simple IPC via shared memory/memory mapped files


Typical IPC options are sockets and files. This shared memory implementation seems natural given that both Prime and Randomizer processes are expected to run on the same host.
Additionally, we get journaling for free.

To build and run:

```
mvn clean install
cd classes/
java jdc.Prime
java jdc.Randomizer
```

### Implementation Notes:

##### Randomizer
As per 'spec', Randomizer has the following responsibilities:
+ add positive integers to a queue implementation
+ read answers from a queue implementation

These responsibilities are unit tested separately from those of its dependencies.

##### Prime
As per 'spec', Prime has the following responsibilities:
+ read positive integers from a queue implementation
+ add primality-tested answers to a queue implementation

Like Randomizer, Prime's' responsibilities are unit tested separately from those of its dependencies.

Queue reader and writer are separate for illustration convenience and testability.


##### Performance Notes

Prime lags Randomizer due to its more computationally expensive primality test. Nevertheless, I clocked a decent 2+ million answers/second on my dual core MBP which is good enough for the 'spec' given.

If, after improving the low hanging fruit (e.g.: calculate MAX_INT primes offline and load upfront, etc) and after careful performance analysis, Prime is still considered a performance bottleneck, there are additional improvements that could be done such as batching and/or calculating several primes in parallel, e.g.:

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
A day's worth of Randomizer/Prime data would require ~200GBs with the current approach.