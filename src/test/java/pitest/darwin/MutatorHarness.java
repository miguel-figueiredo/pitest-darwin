package pitest.darwin;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

/**
 * Drives gregor directly: find the mutations darwin reports in a fixture, generate one, load the mutated
 * class in isolation and run it. PIT's own {@code MutatorVerifier} would do this job, but it lives in
 * pitest's test sources and is not published.
 * <p>
 * Loading and running the mutant is the point - it proves the bytecode actually verifies, which a
 * bytecode-level assertion would not.
 */
final class MutatorHarness {

  private final Mutater mutater;

  MutatorHarness(final MethodMutatorFactory... factories) {
    this.mutater = new GregorMutater(ClassloaderByteArraySource.fromContext(), m -> true,
        Arrays.asList(factories));
  }

  List<MutationDetails> mutationsIn(final Class<?> target, final String method) {
    return this.mutater.findMutations(ClassName.fromClass(target)).stream()
        .filter(d -> d.getMethod().equals(method))
        .collect(toList());
  }

  List<String> descriptionsIn(final Class<?> target, final String method) {
    return mutationsIn(target, method).stream().map(MutationDetails::getDescription).collect(toList());
  }

  /** The one mutation in {@code method} with this exact description; for methods holding several. */
  MutationDetails mutationIn(final Class<?> target, final String method, final String description) {
    return mutationsIn(target, method).stream()
        .filter(d -> d.getDescription().equals(description))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no mutation \"" + description + "\" in " + method
            + ", found " + descriptionsIn(target, method)));
  }

  MutationDetails soleMutationIn(final Class<?> target, final String method) {
    final List<MutationDetails> found = mutationsIn(target, method);
    assertEquals(1, found.size(),
        () -> "expected exactly one mutation in " + method + " but found "
            + found.stream().map(MutationDetails::getDescription).collect(toList()));
    return found.get(0);
  }

  /** Applies one mutation, then invokes a static method of the mutated class. */
  Object apply(final MutationDetails mutation, final String method, final Object... args) {
    final Mutant mutant = this.mutater.getMutation(mutation.getId());
    final String className = mutation.getClassName().asJavaName();
    try {
      final Class<?> mutated = new MutantLoader(className, mutant.getBytes()).loadClass(className);
      final Method target = Arrays.stream(mutated.getDeclaredMethods())
          .filter(m -> m.getName().equals(method))
          .findFirst()
          .orElseThrow(() -> new AssertionError("no method " + method + " on " + className));
      target.setAccessible(true);
      return target.invoke(null, args);
    } catch (final ReflectiveOperationException ex) {
      throw new AssertionError("could not run mutant: " + mutation.getDescription(), ex);
    }
  }

  /**
   * Defines the mutated class itself and delegates everything else upwards. Parent-first delegation
   * would hand back the unmutated original, since the fixture is already on the test classpath.
   */
  private static final class MutantLoader extends ClassLoader {

    private final String name;
    private final byte[] bytes;

    MutantLoader(final String name, final byte[] bytes) {
      super(MutatorHarness.class.getClassLoader());
      this.name = name;
      this.bytes = bytes;
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve)
        throws ClassNotFoundException {
      if (!name.equals(this.name)) {
        return super.loadClass(name, resolve);
      }
      Class<?> mutated = findLoadedClass(name);
      if (mutated == null) {
        mutated = defineClass(name, this.bytes, 0, this.bytes.length);
      }
      if (resolve) {
        resolveClass(mutated);
      }
      return mutated;
    }
  }
}
