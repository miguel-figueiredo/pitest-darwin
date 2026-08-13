package pitest.darwin;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.pitest.mutationtest.engine.MutationDetails;

import pitest.darwin.fixtures.Substitutions;

class SubstitutionMutatorTest {

  private final MutatorHarness streams =
      new MutatorHarness(StreamSubstituteMutator.DARWIN_STREAM_SUBSTITUTE);

  private final MutatorHarness optionals =
      new MutatorHarness(OptionalSubstituteMutator.DARWIN_OPTIONAL_SUBSTITUTE);

  @Test
  void swapsAnyMatchForAllMatch() {
    final List<String> values = asList("a", "");
    assertEquals(true, Substitutions.anyEmpty(values));

    final MutationDetails mutation = this.streams.soleMutationIn(Substitutions.class, "anyEmpty");
    assertEquals("replaced call to Stream::anyMatch with allMatch", mutation.getDescription());
    assertEquals(false, this.streams.apply(mutation, "anyEmpty", values));
  }

  @Test
  void swapsMinForMax() {
    final List<String> values = asList("b", "a");
    assertEquals(Optional.of("a"), Substitutions.smallest(values));

    final MutationDetails mutation = this.streams.soleMutationIn(Substitutions.class, "smallest");
    assertEquals("replaced call to Stream::min with max", mutation.getDescription());
    assertEquals(Optional.of("b"), this.streams.apply(mutation, "smallest", values));
  }

  @Test
  void swapsLimitForSkip() {
    final List<String> values = asList("a", "b");
    assertEquals(List.of("a"), Substitutions.firstOne(values));

    final MutationDetails mutation = this.streams.soleMutationIn(Substitutions.class, "firstOne");
    assertEquals("replaced call to Stream::limit with skip", mutation.getDescription());
    assertEquals(List.of("b"), this.streams.apply(mutation, "firstOne", values));
  }

  @Test
  void swapsMinForMaxOnPrimitiveStreams() {
    final int[] values = {3, 1, 2};
    assertEquals(1, Substitutions.smallestInt(values));

    final MutationDetails mutation = this.streams.soleMutationIn(Substitutions.class, "smallestInt");
    assertEquals("replaced call to IntStream::min with max", mutation.getDescription());
    assertEquals(3, this.streams.apply(mutation, "smallestInt", values));
  }

  @Test
  void swapsOptionalOfForOfNullable() {
    assertThrows(NullPointerException.class, () -> Substitutions.wrap(null));

    final MutationDetails mutation = this.optionals.soleMutationIn(Substitutions.class, "wrap");
    assertEquals("replaced call to Optional::of with ofNullable", mutation.getDescription());
    assertEquals(Optional.empty(), this.optionals.apply(mutation, "wrap", (Object) null));
  }

  @Test
  void streamAndOptionalTablesDoNotOverlap() {
    assertEquals(List.of(), this.optionals.mutationsIn(Substitutions.class, "anyEmpty"));
    assertEquals(List.of(), this.streams.mutationsIn(Substitutions.class, "wrap"));
  }
}
