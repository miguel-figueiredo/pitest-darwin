package pitest.darwin.fixtures;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Call sites for the substitution table, one substitutable call each. */
public class Substitutions {

  public static boolean anyEmpty(final List<String> values) {
    return values.stream().anyMatch(String::isEmpty);
  }

  public static Optional<String> smallest(final List<String> values) {
    return values.stream().min(Comparator.naturalOrder());
  }

  public static List<String> firstOne(final List<String> values) {
    return values.stream().limit(1).collect(Collectors.toList());
  }

  public static int smallestInt(final int[] values) {
    return IntStream.of(values).min().orElse(0);
  }

  public static Optional<String> wrap(final String value) {
    return Optional.of(value);
  }
}
