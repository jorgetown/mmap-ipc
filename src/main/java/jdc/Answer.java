package jdc;

import java.util.Objects;

/**
 * @author Jorge De Castro
 */
public final class Answer {
    public final int number;
    public final boolean isPrime;

    public Answer(final int number, final boolean isPrime) {
        if (number < 0) {
            throw new IllegalArgumentException("Number must be a positive integer");
        }

        this.number = number;
        this.isPrime = isPrime;
    }

    public Answer(final int number, final int isPrime) {
        this(number, isPrime == 1 ? true : false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Answer answer = (Answer) o;
        return Objects.equals(number, answer.number) &&
                Objects.equals(isPrime, answer.isPrime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, isPrime);
    }

    @Override
    public String toString() {
        return "Answer{" +
                "number=" + number +
                ", isPrime=" + isPrime +
                '}';
    }
}
