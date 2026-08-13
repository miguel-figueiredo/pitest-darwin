# Removability uses strict type equality, not assignability

A call is treated as removable only when its erased return type *equals* its receiver type. The general
safety condition is weaker — removal is safe whenever the owner is assignable to the return type — but
`MethodMutatorFactory.create(MutationContext, MethodInfo, MethodVisitor)` hands a mutator no
`ClassByteArraySource`, so resolving a class hierarchy from inside a mutator means inventing a way to
load supertypes that PIT has not offered.

Strict equality is never *unsafe*; it only under-fires. It also keeps the mutated method trivially
type-correct: the value left on the stack has exactly the type the following instructions expect, and the
depth is unchanged, so the mutation can never present ASM with a type merge the original did not already
contain. (gregor writes mutants with `COMPUTE_FRAMES` for any Java 7+ input, so `StackMapFrame`s are
recalculated either way — the point is that this operator never makes that job harder.)

## Consequences

- Lombok `@SuperBuilder` setters are covered unevenly. javac emits the call-site owner as the builder you
  called, while the erased return type is the builder that *declares* the setter, so setters inherited
  from a superclass builder fail the equality test even though removing them would verify. Setters
  declared on the builder itself are mutated as normal, as is every setter of a plain `@Builder`.
  `FluentRemoveMutatorTest.skipsInheritedSuperBuilderSetters` pins this behaviour down.
- Any fluent API that returns a supertype — self-typed generic builders, some DSLs — is likewise
  skipped rather than mutated incorrectly.
