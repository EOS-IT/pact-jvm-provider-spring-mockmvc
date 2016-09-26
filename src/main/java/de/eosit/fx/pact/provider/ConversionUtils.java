package de.eosit.fx.pact.provider;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.HeaderResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.common.collect.Sets;

import au.com.dius.pact.model.Interaction;
import au.com.dius.pact.model.OptionalBody;
import au.com.dius.pact.model.Pact;
import au.com.dius.pact.model.RequestResponseInteraction;
import au.com.dius.pact.model.Response;

public class ConversionUtils {

    public static Optional<Interaction> getInteraction(Collection<Pact> pacts, String providerState) {
        if (pacts == null) {
            return Optional.empty();
        }

        return getInteraction(pacts.stream(), providerState);
    }

    public static Optional<Interaction> getInteraction(Stream<Pact> pactStream, String providerState) {
        if (providerState == null) {
            return Optional.empty();
        }

        return pactStream.flatMap(pact -> pact.getInteractions().stream())
                .filter(interaction -> providerState.equals(interaction.getProviderState())).findFirst();
    }

    public static Set<ResultMatcher> responseMatchers(Interaction interaction) {
        RequestResponseInteraction reqResInteraction;
        if (interaction instanceof RequestResponseInteraction) {
            reqResInteraction = (RequestResponseInteraction) interaction;
        } else {
            return Sets.newHashSet();
        }

        Response response = reqResInteraction.getResponse();
        if (response == null) {
            return Sets.newHashSet();
        }

        return responseMatchers(response);
    }

    public static Set<ResultMatcher> responseMatchers(Response response) {
        Set<ResultMatcher> result = Sets.newHashSet();

        if (response.getStatus() != null) {
            result.add(MockMvcResultMatchers.status().is(response.getStatus()));
        }

        OptionalBody body = response.getBody();
        if (body.isPresent()) {
            result.add(MockMvcResultMatchers.content().json(body.getValue()));
        }

        Map<String, String> headers = response.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            HeaderResultMatchers headerMatchers = MockMvcResultMatchers.header();
            headers.entrySet().stream().forEach(e -> result.add(headerMatchers.string(e.getKey(), e.getValue())));
        }

        return result;
    }
}
