package pitest.darwin;

import java.util.List;
import java.util.Map;

import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.config.MutatorGroup;

/**
 * Registers {@code FLUENT} as a selectable name covering every darwin operator.
 * <p>
 * {@code gather} throws if asked for a name no factory registered, so this list has to track the
 * service file exactly.
 */
public class FluentMutatorGroup implements MutatorGroup {

  @Override
  public void register(final Map<String, List<MethodMutatorFactory>> mutators) {
    mutators.put("FLUENT", gather(mutators,
        StreamRemoveMutator.DARWIN_STREAM_REMOVE.name(),
        StreamSubstituteMutator.DARWIN_STREAM_SUBSTITUTE.name(),
        OptionalRemoveMutator.DARWIN_OPTIONAL_REMOVE.name(),
        OptionalSubstituteMutator.DARWIN_OPTIONAL_SUBSTITUTE.name(),
        FluentRemoveMutator.DARWIN_FLUENT_REMOVE.name()));
  }
}
