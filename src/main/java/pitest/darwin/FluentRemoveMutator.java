package pitest.darwin;

import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.reloc.asm.MethodVisitor;

/**
 * Removes self-returning calls on every other type: fluent builders (including Lombok's),
 * {@code StringBuilder}, and hand-rolled DSLs.
 */
public enum FluentRemoveMutator implements MethodMutatorFactory {

  DARWIN_FLUENT_REMOVE;

  @Override
  public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo,
      final MethodVisitor methodVisitor) {
    return new RemovalMethodVisitor(this, context, methodVisitor, FluentRemoveMutator::isOtherOwner);
  }

  /**
   * The two carve-outs are load bearing: without them this mutator produces a duplicate of every mutant
   * the family-specific removers already produce, and enabling the FLUENT group would report each
   * removal twice.
   */
  private static boolean isOtherOwner(final String owner) {
    return !owner.startsWith("java/util/stream/") && !owner.equals(OptionalRemoveMutator.OWNER);
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
