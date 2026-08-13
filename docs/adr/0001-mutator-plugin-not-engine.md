# Darwin is a mutator plugin, not a mutation engine

PIT discovers third-party `MethodMutatorFactory` implementations through `ServiceLoader`, so darwin
contributes operators *into* gregor rather than replacing it. The obvious precedent — pitest-descartes —
implements `MutationEngineFactory` instead, but only because extreme mutation is a deliberately
different methodology; fluent-API mutation is a gap-filler for operators gregor lacks, and users want
both sets at once.

## Consequences

- There is no `mutationEngine=darwin`. Users add the jar to PIT's classpath and select darwin's
  operators through the ordinary mutator list, which **must** still include `DEFAULTS` — setting
  `mutators` replaces the default list rather than extending it.
- The repository name says "darwin", and the project was originally conceived as an engine. The name is
  kept; the word "engine" is not. See `CONTEXT.md`.
