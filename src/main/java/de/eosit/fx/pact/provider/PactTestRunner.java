package de.eosit.fx.pact.provider;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.experimental.results.ResultMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.HeaderResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.common.collect.Sets;

import au.com.dius.pact.model.Interaction;
import au.com.dius.pact.model.OptionalBody;
import au.com.dius.pact.model.Pact;
import au.com.dius.pact.model.RequestResponseInteraction;
import au.com.dius.pact.model.Response;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Instances of this class are responsible to verify a certain interaction
 * between a consumer and a producer. It is based on a set of {@link Pact}s from
 * which the {@link Interaction} is retrieved by the provider state. For this
 * {@link Interaction} a spring mock mvc request is build from the interactions
 * request parameter. The request is sent to the provided {@link MockMvc} and
 * the response is validated against the response parameters of the interaction.
 */
public class PactTestRunner {

    private Set<Pact> pacts = newHashSet();
    private Optional<MockMvc> mockMvc = Optional.empty();
    private Optional<String> consumer = Optional.empty();
    private Optional<String> provider = Optional.empty();
    private Optional<String> providerState = Optional.empty();
    private Optional<String> contextPath = Optional.empty();
    private Consumer<? super MockHttpServletRequestBuilder> requestCallback = null;
    private Set<ResultMatcher> resultMatchers = newHashSet();
    private Consumer<? super ResultActions> responseCallback = null;

    /**
     * Constructor setting the available {@link Pact}s.
     *
     * @param pacts
     *            The available {@link Pact}s.
     */
    public PactTestRunner(Iterable<Pact> pacts) {
        if (pacts != null) {
            this.pacts = newHashSet(pacts);
        }
    }

    /**
     * Constructor setting the available {@link Pact}.
     *
     * @param pact
     *            The available {@link Pact}.
     */
    public PactTestRunner(Pact pact) {
        if (pact != null) {
            this.pacts = newHashSet(pact);
        }
    }

    /**
     * This method does all the work. Depending on the parameters set, an
     * {@link Interaction} is retrieved from the available {@link Pact}s and the
     * described request is send to the {@link MockMvc}. The response is
     * validated against the response information from the {@link Interaction}.
     * Any configured callbacks will be called to intercept the execution
     * lifecycle.
     *
     * @throws Throwable
     *             In case any error occurs during the execution.
     */
    public void run() throws Throwable {
        Optional<Interaction> interaction = findInteraction();

        Optional<MockHttpServletRequestBuilder> request = RequestBuilder.buildRequest(interaction.orElse(null));
        request.ifPresent(r -> r.contextPath(contextPath.orElse(null)));

        if (requestCallback != null) {
            request.ifPresent(requestCallback);
        }

        if (request.isPresent()) {
            MockMvc server = mockMvc
                    .orElseThrow(() -> new IllegalStateException("A MockMvc must be provided to perform the request."));

            ResultActions response = server.perform(request.get());
            Set<ResultMatcher> responseMatchers = responseMatchers(interaction.orElse(null));
            responseMatchers.addAll(resultMatchers);

            for (ResultMatcher matcher : responseMatchers) {
                response.andExpect(matcher);
            }

            if (responseCallback != null) {
                responseCallback.accept(response);
            }
        }

    }

    /**
     * Gets a copy of the available {@link Pact}s.
     *
     * @return The availabe set of {@link Pact}s.
     */
    public Set<Pact> getPacts() {
        return newHashSet(pacts);
    }

    /**
     * The consumer name to filter the {@link Pact}s.
     *
     * @return The configured consumer name for filtering purposes.
     */
    public Optional<String> getConsumer() {
        return consumer;
    }

    /**
     * Sets the consumer name to filter the {@link Pact}s. Set to
     * <code>null</code> to reset that filter and consider all {@link Pact}s.
     *
     * @param consumer
     *            The name of the consumer to filter.
     * @return Returns the current {@link PactTestRunner}.
     */
    public PactTestRunner setConsumer(String consumer) {
        this.consumer = Optional.ofNullable(consumer);
        return this;
    }

    /**
     * The provider name to filter the {@link Pact}s.
     *
     * @return The configured provider name for filtering purposes.
     */
    public Optional<String> getProvider() {
        return provider;
    }

    /**
     * Sets the provider name to filter the {@link Pact}s. Set to
     * <code>null</code> to reset that filter and consider all {@link Pact}s.
     *
     * @param provider
     *            The name of the provider to filter.
     * @return Returns the current {@link PactTestRunner}.
     */
    public PactTestRunner setProvider(String provider) {
        this.provider = Optional.ofNullable(provider);
        return this;
    }

    /**
     * The provider state that is used to retrieve the {@link Interaction} from
     * the available {@link Pact}s.
     *
     * @return The configured provider state.
     */
    public Optional<String> getProviderState() {
        return providerState;
    }

    /**
     * Sets the provider state to retrieve the {@link Interaction} from the
     * available {@link Pact}s. Set to <code>null</code> to reset that value,
     * but consider that a provider state is needed during execution to get the
     * {@link Interaction}.
     *
     * @param providerState
     *            The provider state to get the interaction for.
     * @return Returns the current {@link PactTestRunner}.
     */
    public PactTestRunner setProviderState(String providerState) {
        this.providerState = Optional.ofNullable(providerState);
        return this;
    }

    /**
     * The {@link MockMvc} that is used to execute the requests.
     *
     * @return The configured {@link MockMvc}.
     */
    public Optional<MockMvc> getMockMvc() {
        return mockMvc;
    }

    /**
     * Sets the {@link MockMvc} to use for executing the request. This is
     * required during execution.
     *
     * @param mockMvc
     *            The {@link MockMvc} to use for executing the requests.
     * @return Returns the current {@link PactTestRunner}.
     */
    public PactTestRunner setMockMvc(MockMvc mockMvc) {
        this.mockMvc = Optional.ofNullable(mockMvc);
        return this;
    }

    /**
     * The context path of the requests.
     *
     * @return The configured context path.
     */
    public Optional<String> getContextPath() {
        return contextPath;
    }

    /**
     * Sets the context path to use for executing the requests. Set to
     * <code>null</code> to reset the context path.
     *
     * @param contextPath
     *            The context path of the requests.
     * @return Returns the current {@link PactTestRunner}.
     */
    public PactTestRunner setContextPath(String contextPath) {
        this.contextPath = Optional.ofNullable(contextPath);
        return this;
    }

    /**
     * The currently configured request callback that is called during execution
     * lifecycle to modify the request.
     *
     * @return The configured request callback.
     */
    public Consumer<? super MockHttpServletRequestBuilder> getRequestCallback() {
        return requestCallback;
    }

    /**
     * Sets the request callback to call during execution lifecycle to modify
     * the request before the request is sent.
     *
     * @param requestCallback
     *            The callback to use.
     * @return Returns the current {@link PactTestRunner}.
     */
    public PactTestRunner setRequestCallback(Consumer<? super MockHttpServletRequestBuilder> requestCallback) {
        this.requestCallback = requestCallback;
        return this;
    }

    /**
     * The currently configured {@link ResultMatcher}s that are additionally
     * used to verify the result.
     *
     * @return The configured additional {@link ResultMatcher}s.
     */
    public Set<ResultMatcher> getResultMatchers() {
        return resultMatchers;
    }

    /**
     * Adds additional {@link ResultMatchers} that will be used to verify the
     * response. These {@link ResultMatcher}s are verified additional to the
     * matchers generated from the pact response description.
     *
     * @param resultMatchers
     *            The additional {@link ResultMatcher}s to use.
     * @return Returns the current {@link PactTestRunner}.
     */
    public PactTestRunner addResultMatchers(Collection<ResultMatcher> resultMatchers) {
        this.resultMatchers.addAll(resultMatchers);
        return this;
    }

    /**
     * Adds additional {@link ResultMatchers} that will be used to verify the
     * response. These {@link ResultMatcher}s are verified additional to the
     * matchers generated from the pact response description.
     *
     * @param resultMatchers
     *            The additional {@link ResultMatcher}s to use.
     * @return Returns the current {@link PactTestRunner}.
     */
    public PactTestRunner addResultMatchers(ResultMatcher... resultMatchers) {
        this.resultMatchers.addAll(newHashSet(resultMatchers));
        return this;
    }

    /**
     * The currently configured response callback that is called during
     * execution lifecycle to access the response of the call.
     *
     * @return The configured response callback.
     */
    public Consumer<? super ResultActions> getResponseCallback() {
        return responseCallback;
    }

    /**
     * Sets the response callback to call during execution lifecycle to access
     * the response of the call.
     *
     * @param responseCallback
     *            The callback to use.
     * @return Returns the current {@link PactTestRunner}.
     */
    public PactTestRunner setResponseCallback(Consumer<? super ResultActions> responseCallback) {
        this.responseCallback = responseCallback;
        return this;
    }

    /**
     * Find the interaction from the available {@link Pact}s by first applying
     * any configured consumer / provider filter and then determine the
     * {@link Interaction} from the remaining {@link Pact}s by the provider
     * state.
     *
     * @return The found {@link Interaction} or an empty {@link Optional} if no
     *         interaction matches the provider state.
     * @throws IllegalStateException
     *             In case no provider state is configured.
     */
    protected Optional<Interaction> findInteraction() {
        String state = providerState.orElseThrow(() -> new IllegalStateException(
                "No provider state defined. Set one explicitly or use the ProviderState annotation"));

        Stream<Pact> pactStream = pacts.stream();

        if (provider.isPresent()) {
            pactStream = pactStream.filter(pact -> provider.get().equals(pact.getProvider().getName()));
        }
        if (consumer.isPresent()) {
            pactStream = pactStream.filter(pact -> consumer.get().equals(pact.getConsumer().getName()));
        }

        return ConversionUtils.getInteraction(pactStream, state);
    }

    /**
     * Determines the default {@link ResultMatcher}s from the Interaction.
     *
     * @param interaction
     *            The interactions to get the matchers from.
     * @return The default {@link ResultMatcher}s.
     */
    protected Set<ResultMatcher> responseMatchers(Interaction interaction) {
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

    /**
     * Determines the default {@link ResultMatcher}s from the {@link Response}.
     *
     * @param response
     *            The {@link Response} to get the matchers from.
     * @return The default {@link ResultMatcher}s.
     */
    protected Set<ResultMatcher> responseMatchers(Response response) {
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
