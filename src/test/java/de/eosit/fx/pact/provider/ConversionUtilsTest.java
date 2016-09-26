package de.eosit.fx.pact.provider;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import au.com.dius.pact.model.Interaction;
import au.com.dius.pact.model.Pact;
import de.eosit.fx.pact.util.PactTestUtils;

public class ConversionUtilsTest {

	public List<Pact> pacts;

	@Before
	public void setUp() throws Exception {
		pacts = newArrayList(PactLoader.loadPactGeneric(PactTestUtils.getPactString("p1", "c1", "s1")),
				PactLoader.loadPactGeneric(PactTestUtils.getPactString("p1", "c1", "s2")),
				PactLoader.loadPactGeneric(PactTestUtils.getPactString("p2", "c1", "s1")));
	}

	@Test
	public void getFirstInteraction() {
		Optional<Interaction> interaction = ConversionUtils.getInteraction(pacts, "s1");
		Assert.assertTrue(interaction.isPresent());
		Assert.assertEquals("s1", interaction.get().getProviderState());
	}

	@Test
	public void getInteraction() {
		Optional<Interaction> interaction = ConversionUtils.getInteraction(pacts, "s2");
		Assert.assertTrue(interaction.isPresent());
		Assert.assertEquals("s2", interaction.get().getProviderState());
	}

	@Test
	public void getMissingInteraction() {
		Optional<Interaction> interaction = ConversionUtils.getInteraction(pacts, "s3");
		Assert.assertFalse(interaction.isPresent());
	}

	@Test
	public void getNoInteractionWhenNoPacts() {
		pacts = null;
		Optional<Interaction> interaction = ConversionUtils.getInteraction(pacts, "s1");
		Assert.assertFalse(interaction.isPresent());
	}

	@Test
	public void getNoInteractionWhenNoProviderState() {
		Optional<Interaction> interaction = ConversionUtils.getInteraction(pacts, null);
		Assert.assertFalse(interaction.isPresent());
	}

}
