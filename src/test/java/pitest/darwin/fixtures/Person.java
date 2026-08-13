package pitest.darwin.fixtures;

import lombok.Builder;
import lombok.Getter;

/** A plain Lombok {@code @Builder}: every setter returns the builder type itself. */
@Builder
@Getter
public class Person {

  private final String name;
  private final int    age;
}
