package de.iu.bniebes.model.result;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class Result<T> {

    private final T value;

    @Getter
    private final boolean empty;

    @Getter
    private final boolean error;

    private Result(final T value, boolean empty, boolean error) {
        this.value = value;
        this.empty = empty;
        this.error = error;
    }

    public static <T> Result<T> of(final T value) {
        return new Result<>(value, false, false);
    }

    public static <T> Result<T> empty() {
        return new Result<>(null, true, false);
    }

    public static <T> Result<T> error() {
        return new Result<>(null, false, true);
    }

    public boolean isPresent() {
        return !empty && !error;
    }

    public boolean notPresent() {
        return !isPresent();
    }

    public T get() throws IllegalStateException {
        if (notPresent()) throw new IllegalStateException();
        return value;
    }

    @Override
    public String toString() {
        if (isPresent()) return "Result[%s]".formatted(value);
        if (isEmpty()) return "Result.empty";
        return "Result.error";
    }
}
