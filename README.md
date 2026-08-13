# pitest-darwin

Mutation operators for Java's fluent APIs, for [PIT](https://pitest.org).

PIT's default engine (gregor) does not mutate fluent call chains at all. Given

```java
return people.stream().filter(Person::isActive).count();
```

gregor produces no mutant that removes the `filter`, so a suite that never exercises an inactive
`Person` still scores 100% on that line. The same holds for a `sorted()` before a `findFirst()`, and for
a builder call site that never sets `.age(...)`. Darwin fills that gap.

Darwin is a **mutator plugin**, not a replacement engine: its operators run *alongside* gregor's. There
is no `mutationEngine=darwin` — see [ADR 0001](docs/adr/0001-mutator-plugin-not-engine.md).

## Operators

| Mutator | Does |
|---------|------|
| `DARWIN_STREAM_REMOVE` | removes `filter`, `sorted`, `distinct`, `peek`, `limit`, `skip`, `takeWhile`, `dropWhile`, `IntStream.map` … on `java.util.stream.*` |
| `DARWIN_STREAM_SUBSTITUTE` | swaps `anyMatch`↔`allMatch`, `min`↔`max`, `limit`↔`skip` |
| `DARWIN_OPTIONAL_REMOVE` | removes `Optional.filter` and `Optional.or` |
| `DARWIN_OPTIONAL_SUBSTITUTE` | swaps `Optional.of`↔`Optional.ofNullable` |
| `DARWIN_FLUENT_REMOVE` | removes self-returning calls on every other type: builders (Lombok's included), `StringBuilder.append`, hand-rolled DSLs |

All five are selectable as one group under the name **`FLUENT`**, or individually by the names above.

Some mutable calls are deliberately left alone — `Stream.map` and `Stream.flatMap` among them. That is a
curated decision, not an omission: see [ADR 0002](docs/adr/0002-excluded-and-pruned-operators.md).

## Install

Darwin is not published to Maven Central. Build and install it locally first:

```bash
mvn clean install
```

## Activation — Maven

```xml
<plugin>
  <groupId>org.pitest</groupId>
  <artifactId>pitest-maven</artifactId>
  <version>1.25.9</version>
  <configuration>
    <mutators>
      <mutator>DEFAULTS</mutator>
      <mutator>FLUENT</mutator>
    </mutators>
  </configuration>
  <dependencies>
    <dependency>
      <groupId>pitest.darwin</groupId>
      <artifactId>pitest-darwin</artifactId>
      <version>0.1.0-SNAPSHOT</version>
    </dependency>
  </dependencies>
</plugin>
```

Then `mvn test-compile org.pitest:pitest-maven:mutationCoverage`.

## Activation — Gradle

Via [gradle-pitest-plugin](https://gradle-pitest-plugin.solidsoft.info/). Its `pitest` configuration is
how PIT plugins get onto PIT's own classpath — the same mechanism `pitest-junit5-plugin` uses.

```groovy
plugins {
    id 'java'
    id 'info.solidsoft.pitest' version '1.19.0'
}

repositories {
    mavenCentral()
    mavenLocal()          // darwin is installed locally, not published
}

dependencies {
    pitest 'pitest.darwin:pitest-darwin:0.1.0-SNAPSHOT'
}

pitest {
    pitestVersion = '1.25.9'
    mutators = ['DEFAULTS', 'FLUENT']
}
```

Then `./gradlew pitest`.

## Three things that silently go wrong

- **`DEFAULTS` must be listed explicitly.** In both build tools, setting `mutators` *replaces* the
  default list rather than extending it. Omit `DEFAULTS` and you turn off the whole of gregor while
  believing you added to it.
- **`pitestVersion` is not optional under Gradle.** The plugin ships its own default PIT version, which
  may not be the one darwin was built against. Darwin compiles against gregor internals rather than
  sitting behind the engine SPI, so a mismatch surfaces as `NoSuchMethodError` at mutation time. Match
  the `<pitest.version>` in darwin's `pom.xml` (currently `1.25.9`).
- **`mavenLocal()` plus `mvn install` first.** Skip either and Gradle simply fails to resolve the jar —
  the one failure mode here that is loud rather than silent.

Both snippets are documentation rather than tests: there is no fixture project in this repo exercising
them, so treat them as unverified until first use.

## Known limitations

- Lombok `@Builder` setters are mutated; with `@SuperBuilder`, only the setters declared on the builder
  you call are — inherited ones are skipped. See
  [ADR 0003](docs/adr/0003-strict-type-equality-for-removability.md).
- Removing a `peek` yields an unkillable mutant when the `peek` is debug-only, and also when the terminal
  op is a `count()` the JDK can answer from the source's size without traversing the pipeline (the `peek`
  never runs even unmutated).
- On classes compiled for Java 8, `DARWIN_FLUENT_REMOVE` also mutates the `StringBuilder.append` chains
  javac emits for plain string concatenation. Java 9+ compiles concatenation to `invokedynamic` instead,
  so this does not arise there.
- Darwin never mutates call *arguments* — predicate and comparator faults stay gregor's job.

## Development

```bash
mvn clean verify
```

The suite mutates fixture classes, loads each mutant and runs it, so a mutant that fails verification
fails the build. Tested against JDK 11, 21 and 25.

## Licence

Apache-2.0, matching PIT.
