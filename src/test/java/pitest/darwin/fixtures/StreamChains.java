package pitest.darwin.fixtures;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Chains whose calls darwin should - or pointedly should not - mutate. Every method is static so the
 * harness can invoke it without constructing anything, and holds exactly one call darwin cares about.
 */
public class StreamChains {

  public static long countNonEmpty(final List<String> values) {
    return values.stream().filter(s -> !s.isEmpty()).count();
  }

  /** A removable call inside a branch, so the method carries StackMapFrames of its own. */
  public static long countNonEmptyIf(final List<String> values, final boolean strict) {
    Stream<String> stream = values.stream();
    if (strict) {
      stream = stream.filter(s -> !s.isEmpty());
    }
    return stream.count();
  }

  public static String firstSorted(final List<String> values) {
    return values.stream().sorted().findFirst().orElse(null);
  }

  /**
   * The terminal op has to traverse the pipeline. Pair {@code peek} with {@code count} instead and the
   * JDK computes the count from the sized source without ever running the peek, so removing it is
   * unobservable.
   */
  public static String joinAudited(final List<String> values, final List<String> audit) {
    return values.stream().peek(audit::add).collect(Collectors.joining(","));
  }

  /** {@code limit} takes a long, so removing it exercises POP2 rather than POP. */
  public static List<String> firstTwo(final List<String> values) {
    return values.stream().limit(2).collect(Collectors.toList());
  }

  /** {@code Stream#map} is an excluded removal. */
  public static List<String> trimmed(final List<String> values) {
    return values.stream().map(String::trim).collect(Collectors.toList());
  }

  /** {@code Stream.of} is self-returning but INVOKESTATIC, so removing it would be a VerifyError. */
  public static long countOfThree() {
    return Stream.of("a", "b", "c").count();
  }

  /** {@code IntStream#map} is not excluded: a primitive element type means no downstream checkcast. */
  public static int sumOfDoubles(final int[] values) {
    return IntStream.of(values).map(v -> v * 2).sum();
  }
}
