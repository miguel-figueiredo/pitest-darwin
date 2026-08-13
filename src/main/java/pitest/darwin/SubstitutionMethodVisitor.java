package pitest.darwin;

import java.util.Map;

import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.reloc.asm.MethodVisitor;

/**
 * Replaces a call with a different method of the same descriptor.
 * <p>
 * No opcode guard here, unlike removal: swapping a static factory for another static factory is
 * perfectly safe, since the descriptor - and therefore the stack - is unchanged.
 */
public class SubstitutionMethodVisitor extends MethodVisitor {

  private final MethodMutatorFactory factory;
  private final MutationContext      context;
  private final Map<String, String>  substitutions;

  public SubstitutionMethodVisitor(final MethodMutatorFactory factory, final MutationContext context,
      final MethodVisitor delegate, final Map<String, String> substitutions) {
    super(ASMVersion.ASM_VERSION, delegate);
    this.factory = factory;
    this.context = context;
    this.substitutions = substitutions;
  }

  @Override
  public void visitMethodInsn(final int opcode, final String owner, final String name,
      final String desc, final boolean itf) {
    final String replacement = this.substitutions.get(owner + "#" + name);
    if (replacement == null) {
      super.visitMethodInsn(opcode, owner, name, desc, itf);
      return;
    }

    final MutationIdentifier id = this.context.registerMutation(this.factory,
        "replaced call to " + Names.simpleName(owner) + "::" + name + " with " + replacement);

    super.visitMethodInsn(opcode, owner,
        this.context.shouldMutate(id) ? replacement : name, desc, itf);
  }
}
