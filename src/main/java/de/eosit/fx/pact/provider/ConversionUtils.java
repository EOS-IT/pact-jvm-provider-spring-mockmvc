package de.eosit.fx.pact.provider;

import au.com.dius.pact.model.Interaction;
import au.com.dius.pact.model.Pact;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Utility class providing functionality to extract an {@link Interaction} from
 * a collection of {@link Pact}s.
 */
public class ConversionUtils {

    private ConversionUtils() {
        // Not intended to instantiate utility class
    }

    /**
     * Retrieve the {@link Interaction}s from the stream of {@link Pact}s
     * that match the given <code>providerState</code> and <code>interactionDescription</code>. If no pacts are given
     * or no {@link Interaction}s among the pacts match the given
     * <code>providerState</code> and <code>providerState</code> an empty {@link Stream} is returned.
     *
     * @param pactStream             The {@link Stream} of {@link Pact}s to get the matching
     *                               {@link Interaction}s from.
     * @param providerState          The provider state to match.
     * @param interactionDescription The interaction description to match.
     * @return Returns a {@link Stream} of matching
     * {@link Interaction}s or an empty {@link Stream} in case of no
     * match.
     */
    public static Stream<Interaction> getInteractions(Stream<Pact> pactStream, Optional<String> providerState,
            Optional<String> interactionDescription) {
        return pactStream.flatMap(pact -> pact.getInteractions().stream())
                .filter(interaction -> providerState.map(expectedState -> expectedState.equals(interaction.getProviderState())).orElse(true))
                .filter(interaction -> interactionDescription.map(expectedDesc -> expectedDesc.equals(interaction.getDescription())).orElse(true));
    }
}
