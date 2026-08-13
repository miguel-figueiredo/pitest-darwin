package pitest.darwin;

import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.reloc.asm.MethodVisitor;

/** Swaps {@code anyMatch}/{@code allMatch}, {@code min}/{@code max} and {@code limit}/{@code skip}. */
public enum StreamSubstituteMutator implements MethodMutatorFactory {

  DARWIN_STREAM_SUBSTITUTE;

  @Override
  public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo,
      final MethodVisitor methodVisitor) {
    return new SubstitutionMethodVisitor(this, context, methodVisitor, CallSubstitutions.STREAM);
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
