package pitest.darwin;

final class Names {

  private Names() {
  }

  /** {@code java/util/stream/Stream} to {@code Stream}, for mutant descriptions. */
  static String simpleName(final String internalName) {
    return internalName.substring(internalName.lastIndexOf('/') + 1);
  }
}
