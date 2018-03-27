package nl.quintor.studybits.indy.wrapper.util;

import java.util.concurrent.CompletionException;
import java.util.function.*;

public class AsyncUtil {
    public static <T, R> Function<T, R> wrapException(ExceptionalFunction<T, R> exceptionalFunction) {
        return (T t) -> {
            try {
                return exceptionalFunction.apply(t);
            }
            catch (Exception e) {
                throw new CompletionException(e);
            }
        };
    }

    public static <T, U, R> BiFunction<T, U, R> wrapBiFunctionException(ExceptionalBiFunction<T, U, R> exceptionalBiFunction) {
        return (T t, U u) -> {
            try {
                return exceptionalBiFunction.apply(t, u);
            }
            catch (Exception e) {
                throw new CompletionException(e);
            }
        };
    }

    public static <T> Predicate<T> wrapPredicateException(ExceptionalPredicate<T> exceptionalPredicate) {
        return (T t) -> {
            try {
                return exceptionalPredicate.test(t);
            }
            catch (Exception e) {
                throw new CompletionException(e);
            }
        };
    }

    public static <T> Supplier<T> wrapException(ExceptionalSupplier<T> exceptionalSupplier) {
        return () -> {
            try {
                return exceptionalSupplier.supply();
            }
            catch (Exception e) {
                throw new CompletionException(e);
            }
        };
    }

    public static <T, U> BiConsumer<T, U> wrapBiConsumerException( ExceptionalBiConsumer<T, U> exceptionalBiConsumer ) {
        return ( T t, U u ) -> {
            try {
                exceptionalBiConsumer.consume(t, u);
            } catch ( Exception e ) {
                throw new CompletionException(e);
            }
        };
    }


    public interface ExceptionalFunction<T, R> {
        public R apply(T t) throws Exception;
    }

    public interface ExceptionalBiFunction<T, U, R> {
        public R apply(T t, U u) throws Exception;
    }

    public interface ExceptionalSupplier<T> {
        public T supply() throws Exception;
    }

    public interface ExceptionalPredicate<T> {
        public boolean test(T t) throws Exception;
    }

    public interface ExceptionalBiConsumer<T, U> {
        public void consume( T t, U u ) throws Exception;
    }
}
