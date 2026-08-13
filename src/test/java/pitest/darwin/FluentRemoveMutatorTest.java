package pitest.darwin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.pitest.mutationtest.engine.MutationDetails;

import pitest.darwin.fixtures.Builders;
import pitest.darwin.fixtures.OptionalChains;
import pitest.darwin.fixtures.Person;
import pitest.darwin.fixtures.StreamChains;

class FluentRemoveMutatorTest {

  private final MutatorHarness harness =
      new MutatorHarness(FluentRemoveMutator.DARWIN_FLUENT_REMOVE);

  @Test
  void removesLombokBuilderSetters() {
    assertEquals(List.of(
        "removed call to Person$PersonBuilder::name",
        "removed call to Person$PersonBuilder::age"),
        this.harness.descriptionsIn(Builders.class, "adult"));

    final Person unnamed = (Person) this.harness.apply(
        this.harness.mutationIn(Builders.class, "adult", "removed call to Person$PersonBuilder::name"),
        "adult", "Ada");
    assertNull(unnamed.getName());
    assertEquals(18, unnamed.getAge());

    final Person ageless = (Person) this.harness.apply(
        this.harness.mutationIn(Builders.class, "adult", "removed call to Person$PersonBuilder::age"),
        "adult", "Ada");
    assertEquals("Ada", ageless.getName());
    assertEquals(0, ageless.getAge());
  }

  @Test
  void removesStringBuilderAppends() {
    assertEquals(3, this.harness.mutationsIn(Builders.class, "describe").size());
    assertEquals("Ada is 42", Builders.describe("Ada", 42));

    // Three mutations share this description; mutationIn returns the first, which is the first append.
    final MutationDetails mutation =
        this.harness.mutationIn(Builders.class, "describe", "removed call to StringBuilder::append");
    assertEquals(" is 42", this.harness.apply(mutation, "describe", "Ada", 42));
  }

  /**
   * Documents the {@code @SuperBuilder} limitation from ADR 0003: the setter declared on the subclass
   * builder is removable, the one inherited from the superclass builder is not, because its erased
   * return type is the superclass builder rather than the call-site owner.
   */
  @Test
  void skipsInheritedSuperBuilderSetters() {
    assertEquals(List.of("removed call to Dog$DogBuilder::breed"),
        this.harness.descriptionsIn(Builders.class, "dog"));
  }

  @Test
  void leavesStreamAndOptionalToTheirOwnMutators() {
    assertEquals(List.of(), this.harness.mutationsIn(StreamChains.class, "countNonEmpty"));
    assertEquals(List.of(), this.harness.mutationsIn(OptionalChains.class, "positiveOrZero"));
  }
}
