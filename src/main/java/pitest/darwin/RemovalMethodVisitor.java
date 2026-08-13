package pitest.darwin;

import java.util.function.Predicate;

import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.reloc.asm.MethodVisitor;
import org.pitest.reloc.asm.Opcodes;
import org.pitest.reloc.asm.Type;

/**
 * Deletes removable calls whose owner matches a filter, popping each argument and leaving the receiver
 * on the stack.
 * <p>
 * Stack depth is preserved by construction (one receiver in, one value out) and, because removability
 * demands strict type equality, so is the stack type - the mutated method is type-correct without any
 * frame juggling on our part.
 */
public class RemovalMethodVisitor extends MethodVisitor {

  private final MethodMutatorFactory factory;
  private final MutationContext      context;
  private final Predicate<String>    owners;

  public RemovalMethodVisitor(final MethodMutatorFactory factory, final MutationContext context,
      final MethodVisitor delegate, final Predicate<String> owners) {
    super(ASMVersion.ASM_VERSION, delegate);
    this.factory = factory;
    this.context = context;
    this.owners = owners;
  }

  @Override
  public void visitMethodInsn(final int opcode, final String owner, final String name,
      final String desc, final boolean itf) {
    if (!this.owners.test(owner) || !RemovableCall.isRemovable(opcode, owner, name, desc)) {
      super.visitMethodInsn(opcode, owner, name, desc, itf);
      return;
    }

    final MutationIdentifier id = this.context.registerMutation(this.factory,
        "removed call to " + Names.simpleName(owner) + "::" + name);

    if (this.context.shouldMutate(id)) {
      popArguments(desc);
    } else {
      super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
  }

  private void popArguments(final String desc) {
    final Type[] argTypes = Type.getArgumentTypes(desc);
    for (int i = argTypes.length - 1; i >= 0; i--) {
      super.visitInsn(argTypes[i].getSize() == 1 ? Opcodes.POP : Opcodes.POP2);
    }
  }
}
