package pitest.darwin.fixtures;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class Dog extends Animal {

  private final String breed;
}
