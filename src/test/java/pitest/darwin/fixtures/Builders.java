package pitest.darwin.fixtures;

/** Builder call sites - the shape darwin mutates for everything outside Stream and Optional. */
public class Builders {

  public static Person adult(final String name) {
    return Person.builder().name(name).age(18).build();
  }

  public static String describe(final String name, final int age) {
    return new StringBuilder().append(name).append(" is ").append(age).toString();
  }

  /** {@code @SuperBuilder} setters return a type variable, so removability is not uniform here. */
  public static Dog dog(final String name, final String breed) {
    return Dog.builder().breed(breed).name(name).build();
  }
}
