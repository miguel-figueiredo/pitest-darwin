package pitest.darwin.fixtures;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/** Base of the {@code @SuperBuilder} hierarchy, whose setters return a type variable. */
@SuperBuilder
@Getter
public class Animal {

  private final String name;
}
