# pitest-darwin

Mutation operators for Java's fluent APIs — the Stream API, `Optional`, and fluent builders — added to
PIT's default gregor engine. This is the vocabulary for describing which calls darwin can mutate, and
which of the resulting mutants are worth generating at all.

## Plugin shape

**Mutator Plugin**:
A jar contributing `MethodMutatorFactory` implementations that gregor discovers and runs alongside its
own operators. Darwin is one of these.
_Avoid_: engine, mutation engine

**Mutation Engine**:
A wholesale replacement for gregor, selected through PIT's `mutationEngine` option, which disables
gregor's own operators. Darwin is deliberately not one.
_Avoid_: using "engine" as a loose synonym for plugin

## What can be mutated

**Self-Returning Method**:
An instance method whose erased return type is the same type it was invoked on — `Stream.filter`,
`Optional.filter`, a builder setter, `StringBuilder.append`.
_Avoid_: fluent method, chainable method, chained method

**Removable Call**:
A call to a self-returning method through `INVOKEVIRTUAL` or `INVOKEINTERFACE`, which can therefore be
deleted by popping its arguments and leaving the receiver on the stack.
_Avoid_: chain link, stage, step

**Chain**:
Not part of this model. `a.b().c()` and `var x = a.b(); x.c();` compile to the same instructions, so
darwin reasons about individual calls and never about chains.
_Avoid_: fluent chain, call chain, pipeline

## What is worth mutating

**Removal Operator**:
An operator that deletes a removable call.
_Avoid_: deletion mutator, skip mutator

**Substitution Operator**:
An operator that replaces a call with a different method of identical descriptor — `allMatch` in place
of `anyMatch`.
_Avoid_: swap, replacement mutator

**Excluded Removal**:
A removable call darwin refuses to mutate because the resulting mutant would be worthless. The
exclusion list is a deliberate feature, not an unfinished filter.
_Avoid_: skipped call, blacklist

**Unkillable Mutant**:
A mutant no test can distinguish from the original because the change is unobservable. It depresses the
mutation score permanently, with no fix available to the user. (The literature calls this an
*equivalent mutant*; we name it for its consequence.)
_Avoid_: false positive

**Negation-Equivalent Mutant**:
A mutant whose behaviour is identical to negating a boolean, which gregor's `NEGATE_CONDITIONALS`
already produces. Darwin does not generate these.
_Avoid_: duplicate mutant

**Crashing Mutant**:
A mutant that throws on every execution — typically a failed `checkcast` following a type-changing
removal — so any covering test kills it and the mutation score is inflated.
_Avoid_: invalid mutant, broken mutant
