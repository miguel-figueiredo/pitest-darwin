# Some mutable calls are deliberately not mutated

Darwin's generic rule finds far more mutable calls than are worth mutating, so both the removal
exclusion list and the substitution table are curated against three admission criteria: a mutant must
not be **crashing** (dies to every covering test, inflating the score), not **unkillable** (no test can
distinguish it, permanently depressing the score), and not **negation-equivalent** (gregor's
`NEGATE_CONDITIONALS` already produces it).

This is the ADR most likely to be "fixed" by a future reader who sees a mutable call being skipped and
assumes an oversight. It is not an oversight.

## Consequences

- Removal excludes `Stream#map` and `Stream#flatMap` (crashing: erasure makes them indistinguishable
  from `filter` at the bytecode level, but the downstream `checkcast` then fails on every execution) and
  `sequential`, `parallel`, `unordered`, `onClose` (unkillable: unobservable).
- Excluding `Stream#map` also loses the genuinely useful mutants where the mapper happens to preserve
  the element type, such as `map(String::trim)`. Distinguishing those needs the generic signature, which
  the removability rule deliberately does not read. Accepted.
- `IntStream#map` and its siblings are **not** excluded: the element type is a fixed primitive, so no
  `checkcast` follows and the mutant is both valid and meaningful.
- Substitution ships only `anyMatch↔allMatch`, `min↔max`, `limit↔skip` and `Optional.of↔ofNullable`.
  Rejected for cause: `findFirst↔findAny` and `forEach↔forEachOrdered` (unkillable on sequential
  streams), `isPresent↔isEmpty` and `anyMatch→noneMatch` (negation-equivalent), `toList↔toSet`
  (crashing).
