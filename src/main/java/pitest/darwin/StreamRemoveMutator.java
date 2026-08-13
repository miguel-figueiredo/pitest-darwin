package pitest.darwin;

import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.reloc.asm.MethodVisitor;

/**
 * Removes self-returning calls on the Stream API.
 * <p>
 * A single-constant enum on purpose: PIT's own ServiceLoader instantiates enum plugins through
 * {@code getEnumConstants()[0]}, so a second constant would never be discovered.
 */
public enum StreamRemoveMutator implements MethodMutatorFactory {

  DARWIN_STREAM_REMOVE;

  @Override
  public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo,
      final MethodVisitor methodVisitor) {
    return new RemovalMethodVisitor(this, context, methodVisitor,
        owner -> owner.startsWith("java/util/stream/"));
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
