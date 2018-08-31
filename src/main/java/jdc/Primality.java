package jdc;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author Jorge De Castro
 */
public final class Primality implements Function<Integer, Answer> {

    @Override
    public Answer apply(@NotNull final Integer i) {
        return isPrime(i) ? new Answer(i, true) : new Answer(i, false);
    }

    // {@see https://en.wikipedia.org/wiki/Primality_test}
    private boolean isPrime(final int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;

        if (n % 2 == 0 || n % 3 == 0) return false;

        for (int i = 5; i * i <= n; i = i + 6)
            if (n % i == 0 || n % (i + 2) == 0)
                return false;

        return true;
    }
}