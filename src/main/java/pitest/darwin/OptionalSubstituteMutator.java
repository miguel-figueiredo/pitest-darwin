package pitest.darwin;

import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.reloc.asm.MethodVisitor;

/**
 * Swaps {@code Optional.of} and {@code Optional.ofNullable}, which differ only in whether they reject
 * null. Killing this mutant means having a test that covers the null case.
 */
public enum OptionalSubstituteMutator implements MethodMutatorFactory {

  DARWIN_OPTIONAL_SUBSTITUTE;

  @Override
  public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo,
      final MethodVisitor methodVisitor) {
    return new SubstitutionMethodVisitor(this, context, methodVisitor, CallSubstitutions.OPTIONAL);
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
