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

import au.com.dius.pact.model.Pact;

import static com.google.common.collect.Sets.newHashSet;

public class PactMockMvcRule implements TestRule {

    private Set<Pact> pacts = newHashSet();
    private PactTestRunner runner = null;

    public PactMockMvcRule() {
        this(null);
    }

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

                    if (!runner.getProviderState().isPresent()) {
                        runner.setProviderState(retrieveDefaultProviderState(description).orElse(null));
                    }

                    runner.run();
                } finally {
                    runner = null;
                }

            }
        };
    }

    protected Optional<String> retrieveDefaultProviderState(Description description) {
        ProviderState stateAnnotation = description.getAnnotation(ProviderState.class);
        return Optional.ofNullable(stateAnnotation).map(s -> s.value());
    }

    public PactTestRunner configure() {
        return runner;
    }

    public static PactMockMvcRuleBuilder create() {
        return new PactMockMvcRuleBuilder();
    }

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
