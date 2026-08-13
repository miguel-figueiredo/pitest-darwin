package pitest.darwin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.pitest.reloc.asm.Opcodes;
import org.pitest.reloc.asm.Type;

/**
 * Decides whether a call instruction can be removed by dropping the invoke and popping its arguments,
 * leaving the receiver on the stack in place of the return value.
 * <p>
 * This is a purely local decision: only the opcode and descriptor of the call site are consulted, never
 * the class hierarchy. See docs/adr/0003-strict-type-equality-for-removability.md.
 */
public final class RemovableCall {

  /**
   * Calls that would be removable but whose mutants are worthless. See
   * docs/adr/0002-excluded-and-pruned-operators.md before adding to or removing from this list.
   */
  private static final Set<String> EXCLUDED = new HashSet<>(Arrays.asList(
      // Type-changing under erasure: the downstream checkcast fails on every execution, so the mutant
      // dies to any covering test and inflates the score. Note IntStream#map and its siblings are
      // absent from this list on purpose - a primitive element type means no checkcast follows.
      "java/util/stream/Stream#map",
      "java/util/stream/Stream#flatMap",
      "java/util/Optional#map",
      "java/util/Optional#flatMap"));

  /**
   * Unobservable, therefore unkillable. Belt and braces: these are all declared on
   * {@code BaseStream}, so the return type never equals the call-site owner anyway.
   */
  private static final Set<String> UNOBSERVABLE_STREAM_OPS = new HashSet<>(Arrays.asList(
      "sequential", "parallel", "unordered", "onClose"));

  private RemovableCall() {
  }

  public static boolean isRemovable(final int opcode, final String owner, final String name,
      final String desc) {
    return isSelfReturning(opcode, owner, desc) && !isExcluded(owner, name);
  }

  /**
   * True when the call is made on an instance and hands back the type it was called on. The opcode guard
   * is mandatory, not defensive: {@code Optional.of(x)} and {@code Stream.of(...)} are INVOKESTATIC and
   * pass the type test, but removing one leaves the argument where an Optional or Stream is expected,
   * which is a VerifyError rather than a mutant.
   */
  private static boolean isSelfReturning(final int opcode, final String owner, final String desc) {
    if (opcode != Opcodes.INVOKEVIRTUAL && opcode != Opcodes.INVOKEINTERFACE) {
      return false;
    }
    final Type returnType = Type.getReturnType(desc);
    // getInternalName() is only meaningful for OBJECT; on primitives and arrays it is not a class name.
    return returnType.getSort() == Type.OBJECT && returnType.getInternalName().equals(owner);
  }

  private static boolean isExcluded(final String owner, final String name) {
    return EXCLUDED.contains(owner + "#" + name)
        || (owner.startsWith("java/util/stream/") && UNOBSERVABLE_STREAM_OPS.contains(name));
  }
}
