package pitest.darwin;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Which calls may be swapped for which others, keyed by {@code owner#name}.
 * <p>
 * Every pair here shares a single descriptor, so the swap changes nothing but the name - stack and frames
 * are untouched. That is what makes a curated table the right shape for this: there is no general rule
 * that finds interchangeable methods, and the pairs that produce worthwhile mutants are few. See
 * docs/adr/0002-excluded-and-pruned-operators.md for the ones deliberately left out.
 */
public final class CallSubstitutions {

  private static final List<String> STREAM_OWNERS = Arrays.asList(
      "java/util/stream/Stream",
      "java/util/stream/IntStream",
      "java/util/stream/LongStream",
      "java/util/stream/DoubleStream");

  private static final String[][] STREAM_PAIRS = {
      {"anyMatch", "allMatch"},
      {"min", "max"},
      {"limit", "skip"}};

  public static final Map<String, String> STREAM = streamTable();

  public static final Map<String, String> OPTIONAL = pairs(
      Collections.singletonList(OptionalRemoveMutator.OWNER),
      new String[][] {{"of", "ofNullable"}});

  private CallSubstitutions() {
  }

  private static Map<String, String> streamTable() {
    return pairs(STREAM_OWNERS, STREAM_PAIRS);
  }

  private static Map<String, String> pairs(final List<String> owners, final String[][] pairs) {
    final Map<String, String> table = new HashMap<>();
    for (final String owner : owners) {
      for (final String[] pair : pairs) {
        table.put(owner + "#" + pair[0], pair[1]);
        table.put(owner + "#" + pair[1], pair[0]);
      }
    }
    return Collections.unmodifiableMap(table);
  }
}
