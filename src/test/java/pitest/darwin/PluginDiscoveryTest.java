package pitest.darwin;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.config.Mutator;

/**
 * Proves PIT can find darwin the way a real run would: through its own ServiceLoader, by name. Without
 * this, every unit test in the suite can pass while the plugin is invisible to PIT.
 */
class PluginDiscoveryTest {

  private static final List<MethodMutatorFactory> ALL = List.of(
      StreamRemoveMutator.DARWIN_STREAM_REMOVE,
      StreamSubstituteMutator.DARWIN_STREAM_SUBSTITUTE,
      OptionalRemoveMutator.DARWIN_OPTIONAL_REMOVE,
      OptionalSubstituteMutator.DARWIN_OPTIONAL_SUBSTITUTE,
      FluentRemoveMutator.DARWIN_FLUENT_REMOVE);

  @Test
  void everyMutatorAndTheGroupAreDiscoverableByName() {
    for (final MethodMutatorFactory factory : ALL) {
      assertTrue(Mutator.allMutatorIds().contains(factory.getName()),
          () -> factory.getName() + " missing from " + Mutator.allMutatorIds());
    }
    assertTrue(Mutator.allMutatorIds().contains("FLUENT"),
        () -> "FLUENT group missing from " + Mutator.allMutatorIds());
  }

  @Test
  void groupResolvesToEveryDarwinOperator() {
    assertEquals(ALL.stream().map(f -> f.getClass().getName()).collect(toSet()),
        classNames("FLUENT"));
  }

  @Test
  void eachMutatorResolvesByItsOwnName() {
    for (final MethodMutatorFactory factory : ALL) {
      assertEquals(Set.of(factory.getClass().getName()), classNames(factory.getName()));
    }
  }

  @Test
  void globallyUniqueIdsAreUnique() {
    assertEquals(ALL.size(), ALL.stream().map(MethodMutatorFactory::getGloballyUniqueId).count());
    assertEquals(ALL.size(),
        ALL.stream().map(MethodMutatorFactory::getGloballyUniqueId).collect(toSet()).size());
  }

  @Test
  void serviceFilesArePackaged() throws IOException {
    for (final MethodMutatorFactory factory : ALL) {
      assertServiceFileNames("org.pitest.mutationtest.engine.gregor.MethodMutatorFactory",
          factory.getClass().getName());
    }
    assertServiceFileNames("org.pitest.mutationtest.engine.gregor.config.MutatorGroup",
        FluentMutatorGroup.class.getName());
  }

  private static Set<String> classNames(final String mutatorName) {
    return Mutator.fromStrings(singletonList(mutatorName)).stream()
        .map(MethodMutatorFactory::getClass)
        .map(Class::getName)
        .collect(toSet());
  }

  /**
   * PIT ships service files under the same names, so this walks every copy on the classpath rather than
   * trusting {@code getResource} to return darwin's.
   */
  private static void assertServiceFileNames(final String spi, final String expected)
      throws IOException {
    boolean found = false;
    for (final URL url : Collections.list(
        PluginDiscoveryTest.class.getClassLoader().getResources("META-INF/services/" + spi))) {
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
        found = found || reader.lines().anyMatch(line -> line.trim().equals(expected));
      }
    }
    assertTrue(found, () -> expected + " is not registered in any META-INF/services/" + spi);
  }
}
