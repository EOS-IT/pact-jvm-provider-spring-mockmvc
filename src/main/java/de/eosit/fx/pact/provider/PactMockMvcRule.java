package de.eosit.fx.pact.provider;

import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.test.web.servlet.MockMvc;

import au.com.dius.pact.model.Pact;

import static com.google.common.collect.Sets.newHashSet;

/**
 * The {@link PactMockMvcRule} allows to execute and verify a request described
 * by pact against the expected response. A test method must configure:
 * <ol>
 * <li>The {@link MockMvc} to use</li>
 * <li>And a provider state by either configuring the rule or by using the
 * annotation {@link ProviderState}. If both methods are used in parallel the
 * state configured on the rule has precedence.</li>
 * </ol>
 *
 * <p>
 * Example of usage:
 *
 * <pre>
 * &#064;RunWith(SpringRunner.class)
 * &#064;WebMvcTest(controllers = MyController.class)
 * public static class PactTest {
 *     &#064;Rule
 *     public PactMockMvcRule pactRule = PactMockMvcRule.create().withFile("file:pacts/myPact.json").build();
 *
 *     &#064;Autowired
 *     private MockMvc mockMvc;
 *
 *     &#064;Test
 *     &#064;ProviderState("my provider state")
 *     public void testInteraction() throws Exception {
 *         pactRule.configure().mockMvc(mockMvc).contextPath("/app");
 *     }
 * }
 * </pre>
 */
public class PactMockMvcRule implements TestRule {

    private Set<Pact> pacts = newHashSet();
    private PactTestRunner runner = null;

    /**
     * Constructs a {@link PactMockMvcRule} without any available {@link Pact}s.
     * So this rule will not execute any requests.
     */
    public PactMockMvcRule() {
        this(null);
    }

    /**
     * Constructs a {@link PactMockMvcRule} with the given {@link Pact}s
     * available for executing requests.
     */
    public PactMockMvcRule(Iterable<Pact> pacts) {
        if (pacts == null) {
            this.pacts = newHashSet();
        } else {
            this.pacts = newHashSet(pacts);
        }
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                runner = new PactTestRunner(pacts);

                try {
                    base.evaluate();

                    if (!runner.providerState().isPresent()) {
                        runner.providerState(retrieveDefaultProviderState(description).orElse(null));
                    }

                    runner.run();
                } finally {
                    runner = null;
                }

            }
        };
    }

    /**
     * Retrieves the provider from the {@link ProviderState} annotation on the
     * test method.
     *
     * @param description
     *            The test description.
     * @return The provider state if the annotation is present or an empty
     *         {@link Optional} if not found.
     */
    protected Optional<String> retrieveDefaultProviderState(Description description) {
        ProviderState stateAnnotation = description.getAnnotation(ProviderState.class);
        return Optional.ofNullable(stateAnnotation).map(s -> s.value());
    }

    /**
     * Returns the internally used {@link PactTestRunner} to configure the
     * parameters.
     *
     * @return The internally used {@link PactTestRunner} instance.
     */
    public PactTestRunner configure() {
        return runner;
    }

    /**
     * Creates a {@link PactMockMvcRuleBuilder} to build a
     * {@link PactMockMvcRule}.
     *
     * @return The {@link PactMockMvcRuleBuilder} instance for building the
     *         rule.
     */
    public static PactMockMvcRuleBuilder create() {
        return new PactMockMvcRuleBuilder();
    }

    /**
     * A builder for the {@link PactMockMvcRule} that helps to extract
     * {@link Pact}s from files and / or folders.
     */
    public static class PactMockMvcRuleBuilder {
        private Set<Pact> pacts = new HashSet<>();
        private Optional<String> consumer = Optional.empty();
        private Optional<String> provider = Optional.empty();

        public PactMockMvcRuleBuilder withFile(String pactFile) {
            pacts.add(PactLoader.loadPactByResource(pactFile));
            return this;
        }

        public PactMockMvcRuleBuilder withAllFrom(String pactFolder) {
            pacts.addAll(PactLoader.loadPactsByResourceFolder(pactFolder));
            return this;
        }

        public PactMockMvcRuleBuilder withAllFrom(File pactFile) {
            pacts.addAll(PactLoader.loadPactsByFile(pactFile));
            return this;
        }

        public PactMockMvcRuleBuilder withPactSource(String pactSource) {
            pacts.add(PactLoader.loadPactGeneric(pactSource));
            return this;
        }

        public PactMockMvcRuleBuilder forConsumer(String consumer) {
            this.consumer = Optional.ofNullable(consumer);
            return this;
        }

        public PactMockMvcRuleBuilder forProvider(String provider) {
            this.provider = Optional.ofNullable(provider);
            return this;
        }

        public PactMockMvcRule build() {
            Stream<Pact> pactStream = pacts.stream();
            if (provider.isPresent()) {
                pactStream = pactStream.filter(pact -> provider.get().equals(pact.getProvider().getName()));
            }
            if (consumer.isPresent()) {
                pactStream = pactStream.filter(pact -> consumer.get().equals(pact.getConsumer().getName()));
            }

            return new PactMockMvcRule(pactStream.collect(Collectors.toList()));
        }
    }
}
