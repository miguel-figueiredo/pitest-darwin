package pitest.darwin;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.pitest.mutationtest.engine.MutationDetails;

import pitest.darwin.fixtures.StreamChains;

class StreamRemoveMutatorTest {

  private final MutatorHarness harness =
      new MutatorHarness(StreamRemoveMutator.DARWIN_STREAM_REMOVE);

  @Test
  void removesFilter() {
    final List<String> values = asList("a", "", "");
    assertEquals(1L, StreamChains.countNonEmpty(values));

    final MutationDetails mutation = this.harness.soleMutationIn(StreamChains.class, "countNonEmpty");
    assertEquals("removed call to Stream::filter", mutation.getDescription());
    assertEquals(3L, this.harness.apply(mutation, "countNonEmpty", values));
  }

  @Test
  void removesFilterInsideControlFlow() {
    final List<String> values = asList("a", "");
    assertEquals(1L, StreamChains.countNonEmptyIf(values, true));

    final MutationDetails mutation =
        this.harness.soleMutationIn(StreamChains.class, "countNonEmptyIf");
    assertEquals("removed call to Stream::filter", mutation.getDescription());
    assertEquals(2L, this.harness.apply(mutation, "countNonEmptyIf", values, true));
  }

  @Test
  void removesSorted() {
    final List<String> values = asList("b", "a");
    assertEquals("a", StreamChains.firstSorted(values));

    final MutationDetails mutation = this.harness.soleMutationIn(StreamChains.class, "firstSorted");
    assertEquals("removed call to Stream::sorted", mutation.getDescription());
    assertEquals("b", this.harness.apply(mutation, "firstSorted", values));
  }

  @Test
  void removesPeek() {
    final List<String> values = asList("a", "b");
    final List<String> audit = new ArrayList<>();
    assertEquals("a,b", StreamChains.joinAudited(values, audit));
    assertEquals(values, audit);

    final MutationDetails mutation = this.harness.soleMutationIn(StreamChains.class, "joinAudited");
    assertEquals("removed call to Stream::peek", mutation.getDescription());

    audit.clear();
    assertEquals("a,b", this.harness.apply(mutation, "joinAudited", values, audit));
    assertTrue(audit.isEmpty(), "only the side effect should be gone - the result is unchanged");
  }

  @Test
  void removesLimitPoppingBothSlotsOfItsLongArgument() {
    final List<String> values = asList("a", "b", "c");
    assertEquals(asList("a", "b"), StreamChains.firstTwo(values));

    final MutationDetails mutation = this.harness.soleMutationIn(StreamChains.class, "firstTwo");
    assertEquals("removed call to Stream::limit", mutation.getDescription());
    assertEquals(values, this.harness.apply(mutation, "firstTwo", values));
  }

  @Test
  void leavesStreamMapAlone() {
    assertEquals(List.of(), this.harness.mutationsIn(StreamChains.class, "trimmed"));
  }

  @Test
  void leavesStaticFactoriesAlone() {
    assertEquals(List.of(), this.harness.mutationsIn(StreamChains.class, "countOfThree"));
  }

  @Test
  void removesPrimitiveStreamMap() {
    final int[] values = {1, 2, 3};
    assertEquals(12, StreamChains.sumOfDoubles(values));

    final MutationDetails mutation = this.harness.soleMutationIn(StreamChains.class, "sumOfDoubles");
    assertEquals("removed call to IntStream::map", mutation.getDescription());
    assertEquals(6, this.harness.apply(mutation, "sumOfDoubles", values));
  }
}
