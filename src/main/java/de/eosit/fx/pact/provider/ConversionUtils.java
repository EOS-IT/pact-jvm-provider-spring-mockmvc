package de.eosit.fx.pact.provider;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import au.com.dius.pact.model.Interaction;
import au.com.dius.pact.model.Pact;

/**
 * Utility class providing functionality to extract an {@link Interaction} from
 * a collection of {@link Pact}s.
 */
public class ConversionUtils {

	private ConversionUtils() {
		// Not intended to instantiate utility class
	}

	/**
	 * Retrieve the first {@link Interaction} from the collection of
	 * {@link Pact}s that matches the given <code>providerState</code>. If no
	 * pacts are given or no {@link Interaction} among the pacts matches the
	 * given <code>providerState</code> an empty {@link Optional} is returned.
	 * If multiple {@link Interaction}s match the given
	 * <code>providerState</code> the first one (arbitrary) is returned.
	 * 
	 * @param pacts
	 *            The {@link Collection} of {@link Pact}s to get the matching
	 *            {@link Interaction} from.
	 * @param providerState
	 *            The provider state to match.
	 * @return Returns an {@link Optional} containing the matching
	 *         {@link Interaction} or an empty {@link Optional} in case of no
	 *         match.
	 */
	public static Optional<Interaction> getInteraction(Collection<Pact> pacts, String providerState) {
		if (pacts == null) {
			return Optional.empty();
		}

		return getInteraction(pacts.stream(), providerState);
	}

	/**
	 * Retrieve the first {@link Interaction} from the stream of {@link Pact}s
	 * that matches the given <code>providerState</code>. If no pacts are given
	 * or no {@link Interaction} among the pacts matches the given
	 * <code>providerState</code> an empty {@link Optional} is returned. If
	 * multiple {@link Interaction}s match the given <code>providerState</code>
	 * the first one (arbitrary) is returned.
	 * 
	 * @param pactStream
	 *            The {@link Stream} of {@link Pact}s to get the matching
	 *            {@link Interaction} from.
	 * @param providerState
	 *            The provider state to match.
	 * @return Returns an {@link Optional} containing the matching
	 *         {@link Interaction} or an empty {@link Optional} in case of no
	 *         match.
	 */
	public static Optional<Interaction> getInteraction(Stream<Pact> pactStream, String providerState) {
		if (providerState == null) {
			return Optional.empty();
		}

		return pactStream.flatMap(pact -> pact.getInteractions().stream())
				.filter(interaction -> providerState.equals(interaction.getProviderState())).findFirst();
	}
}
