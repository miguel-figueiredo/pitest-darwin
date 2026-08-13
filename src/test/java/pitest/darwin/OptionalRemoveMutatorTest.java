package pitest.darwin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.pitest.mutationtest.engine.MutationDetails;

import pitest.darwin.fixtures.OptionalChains;

class OptionalRemoveMutatorTest {

  private final MutatorHarness harness =
      new MutatorHarness(OptionalRemoveMutator.DARWIN_OPTIONAL_REMOVE);

  @Test
  void removesFilter() {
    final Optional<Integer> value = Optional.of(-5);
    assertEquals(0, OptionalChains.positiveOrZero(value));

    final MutationDetails mutation = this.harness.soleMutationIn(OptionalChains.class, "positiveOrZero");
    assertEquals("removed call to Optional::filter", mutation.getDescription());
    assertEquals(-5, this.harness.apply(mutation, "positiveOrZero", value));
  }

  @Test
  void removesOr() {
    final Optional<String> value = Optional.empty();
    assertEquals("fallback", OptionalChains.orFallback(value));

    final MutationDetails mutation = this.harness.soleMutationIn(OptionalChains.class, "orFallback");
    assertEquals("removed call to Optional::or", mutation.getDescription());
    assertEquals("none", this.harness.apply(mutation, "orFallback", value));
  }

  @Test
  void leavesOptionalMapAlone() {
    assertEquals(List.of(), this.harness.mutationsIn(OptionalChains.class, "lengthOrZero"));
  }

  @Test
  void leavesStaticFactoriesAlone() {
    assertEquals(List.of(), this.harness.mutationsIn(OptionalChains.class, "unwrapped"));
  }
}
