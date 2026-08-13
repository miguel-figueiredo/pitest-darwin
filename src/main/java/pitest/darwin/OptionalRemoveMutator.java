package pitest.darwin;

import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.reloc.asm.MethodVisitor;

/**
 * Removes self-returning calls on {@link java.util.Optional} - in practice {@code filter} and
 * {@code or}, since {@code map} and {@code flatMap} are excluded removals.
 */
public enum OptionalRemoveMutator implements MethodMutatorFactory {

  DARWIN_OPTIONAL_REMOVE;

  static final String OWNER = "java/util/Optional";

  @Override
  public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo,
      final MethodVisitor methodVisitor) {
    return new RemovalMethodVisitor(this, context, methodVisitor, OWNER::equals);
  }

  @Override
  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

  @Override
  public String getName() {
    return name();
  }
}
