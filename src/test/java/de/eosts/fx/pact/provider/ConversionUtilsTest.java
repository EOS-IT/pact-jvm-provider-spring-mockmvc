package de.eosts.fx.pact.provider;

import au.com.dius.pact.model.Interaction;
import au.com.dius.pact.model.Pact;
import de.eosts.fx.pact.util.PactTestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class ConversionUtilsTest {

    public Stream<Pact> pacts;

    @Before
    public void setUp() throws Exception {
        pacts = Stream.of(PactLoader.loadPactGeneric(PactTestUtils.getPactString("p1", "c1", "s1", "d1")),
                PactLoader.loadPactGeneric(PactTestUtils.getPactString("p1", "c1", "s2", "d1")),
                PactLoader.loadPactGeneric(PactTestUtils.getPactString("p2", "c1", "s1", "d2")));
    }

    @Test
    public void getInteractionsByState() {
        Stream<Interaction> interactions = ConversionUtils.getInteractions(pacts, Optional.of("s1"), Optional.empty());
        List<Interaction> interactionList = interactions.collect(Collectors.toList());
        assertEquals(2, interactionList.size());
        interactionList.forEach(interaction -> assertEquals("s1", interaction.getProviderState()));
    }

    @Test
    public void getInteractionsByDescription() {
        Stream<Interaction> interactions = ConversionUtils.getInteractions(pacts, Optional.empty(), Optional.of("d1"));
        List<Interaction> interactionList = interactions.collect(Collectors.toList());
        assertEquals(2, interactionList.size());
        interactionList.forEach(interaction -> assertEquals("d1", interaction.getDescription()));
    }

    @Test
    public void getInteractionsByStateAndDescription() {
        Stream<Interaction> interactions = ConversionUtils.getInteractions(pacts, Optional.of("s1"), Optional.of("d1"));
        List<Interaction> interactionList = interactions.collect(Collectors.toList());
        assertEquals(1, interactionList.size());
        interactionList.forEach(interaction -> {
            assertEquals("s1", interaction.getProviderState());
            assertEquals("d1", interaction.getDescription());
        });
    }

    @Test
    public void getInteractionsByStateAndDescriptionIgnoreCase() {
        Stream<Interaction> interactions = ConversionUtils.getInteractions(pacts, Optional.of("S1"), Optional.of("D1"));
        List<Interaction> interactionList = interactions.collect(Collectors.toList());
        assertEquals(1, interactionList.size());
        interactionList.forEach(interaction -> {
            assertEquals("s1", interaction.getProviderState());
            assertEquals("d1", interaction.getDescription());
        });
    }

    @Test
    public void getAllInteractions() {
        Stream<Interaction> interactions = ConversionUtils.getInteractions(pacts, Optional.empty(), Optional.empty());
        assertEquals(3, interactions.count());
    }

    @Test
    public void getMissingStateInteractions() {
        Stream<Interaction> interactions = ConversionUtils.getInteractions(pacts, Optional.of("s3"), Optional.empty());
        assertEquals(0, interactions.count());
    }

    @Test
    public void getMissingDescInteractions() {
        Stream<Interaction> interactions = ConversionUtils.getInteractions(pacts, Optional.empty(), Optional.of("d3"));
        assertEquals(0, interactions.count());
    }

    @Test
    public void getNoInteractionWhenNoPacts() {
        Stream<Interaction> interactions = ConversionUtils
                .getInteractions(Stream.empty(), Optional.of("s1"), Optional.empty());
        assertEquals(0, interactions.count());
    }

}
