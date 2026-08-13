package pitest.darwin.fixtures;

import java.util.Optional;

/** Optional chains, one darwin-relevant call each. */
public class OptionalChains {

  public static int positiveOrZero(final Optional<Integer> value) {
    return value.filter(v -> v > 0).orElse(0);
  }

  public static String orFallback(final Optional<String> value) {
    return value.or(() -> Optional.of("fallback")).orElse("none");
  }

  /** {@code Optional#map} is an excluded removal. */
  public static int lengthOrZero(final Optional<String> value) {
    return value.map(String::length).orElse(0);
  }

  /** {@code Optional.of} is self-returning but INVOKESTATIC. */
  public static String unwrapped(final String value) {
    return Optional.of(value).get();
  }
}
