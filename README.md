# mmap-ipc
Simple IPC via shared memory/memory mapped files

To build and run:

mvn clean install

java Prime
java Randomizer


Implementation Notes:

Randomizer
As per 'spec', Randomizer has the following responsibilities:
add positive integers to a queue implementation
read answers from a queue implementation

Prime
As per 'spec', Prime has the following responsibilities:
read positive integers from a queue implementation
add primality test answers to a queue implementation

Typical IPC options are sockets and files. The shared memory implementation provided seems natural given that both processes run on the same host.

Queue writer and reader are separate for illustration convenience and testability.

Prime lags Randomizer because of its more computational expensive primality test. Nevertheless, I clocked a decent 2+ million answers/second on my dual core MBP which is good enough for the 'spec' given.
Should this be considered a performance bottleneck, there are trivial improvements that could be done such as batching and/or calculating several primes in parallel, e.g.:

Stream.generate(intsQueue::poll)
.filter(Objects::nonNull)
.parallel()
.forEach(i -> {
    Answer answer = primality.apply(i);
    answersQueue.add(answer); // Queue implementation should change to accommodate multiple writers
});
