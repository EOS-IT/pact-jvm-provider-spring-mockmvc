package de.eosit.fx.pact.provider;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

public class PactTestRunner {

    protected Optional<MockMvc> mockMvc = Optional.empty();

    private Set<Pact> pacts = newHashSet();
    private Optional<String> consumer = Optional.empty();
    private Optional<String> provider = Optional.empty();
    private Optional<String> providerState = Optional.empty();
    private Optional<String> contextPath = Optional.empty();
    private Consumer<? super MockHttpServletRequestBuilder> requestCallback = null;
    private Set<ResultMatcher> resultMatchers = newHashSet();
    private Consumer<? super ResultActions> responseCallback = null;

    public PactTestRunner() {
    }

    public PactTestRunner(Iterable<Pact> pacts) {
        if (pacts != null) {
            this.pacts = newHashSet(pacts);
        }
    }

    public PactTestRunner(Pact pact) {
        if (pact != null) {
            this.pacts = newHashSet(pact);
        }
    }

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

    public Set<Pact> getPacts() {
        return newHashSet(pacts);
    }

    public Optional<String> getConsumer() {
        return consumer;
    }

    public PactTestRunner setConsumer(String consumer) {
        this.consumer = Optional.ofNullable(consumer);
        return this;
    }

    public Optional<String> getProvider() {
        return provider;
    }

    public PactTestRunner setProvider(String provider) {
        this.provider = Optional.ofNullable(provider);
        return this;
    }

    public Optional<String> getProviderState() {
        return providerState;
    }

    public PactTestRunner setProviderState(String providerState) {
        this.providerState = Optional.ofNullable(providerState);
        return this;
    }

    public Optional<MockMvc> getMockMvc() {
        return mockMvc;
    }

    public PactTestRunner setMockMvc(MockMvc mockMvc) {
        this.mockMvc = Optional.ofNullable(mockMvc);
        return this;
    }

    public Optional<String> getContextPath() {
        return contextPath;
    }

    public PactTestRunner setContextPath(String contextPath) {
        this.contextPath = Optional.ofNullable(contextPath);
        return this;
    }

    public Consumer<? super MockHttpServletRequestBuilder> getRequestCallback() {
        return requestCallback;
    }

    public void setRequestCallback(Consumer<? super MockHttpServletRequestBuilder> requestCallback) {
        this.requestCallback = requestCallback;
    }

    public Set<ResultMatcher> getResultMatchers() {
        return resultMatchers;
    }

    public void addResultMatchers(Collection<ResultMatcher> resultMatchers) {
        this.resultMatchers.addAll(resultMatchers);
    }

    public void addResultMatchers(ResultMatcher... resultMatchers) {
        this.resultMatchers.addAll(newHashSet(resultMatchers));
    }

    public Consumer<? super ResultActions> getResponseCallback() {
        return responseCallback;
    }

    public void setResponseCallback(Consumer<? super ResultActions> responseCallback) {
        this.responseCallback = responseCallback;
    }

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
