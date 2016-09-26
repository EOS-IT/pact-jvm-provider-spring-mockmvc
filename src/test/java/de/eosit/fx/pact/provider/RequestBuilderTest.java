package de.eosit.fx.pact.provider;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import au.com.dius.pact.model.OptionalBody;
import au.com.dius.pact.model.Request;
import au.com.dius.pact.model.RequestResponseInteraction;

public class RequestBuilderTest {

	private RequestResponseInteraction interaction;

	@Before
	public void setUp() throws Exception {
		interaction = new RequestResponseInteraction("a dummy request", "state");

		Request request = new Request();
		request.setBody(OptionalBody.nullBody());
		request.setMethod("GET");
		request.setPath("/to/service");
		request.setQuery(newHashMap());
		request.getQuery().put("argList", newArrayList("item1", "item2", "lastitem"));
		request.getQuery().put("arg2", newArrayList("v%a=l?u&e"));
		request.setHeaders(newHashMap());
		request.getHeaders().put("HEADER_KEY", "header_value");
		interaction.setRequest(request);
		System.out.println(interaction);
	}

	@Test
	public void test() {
		RequestBuilder.buildRequest(interaction);
		fail("Not yet implemented");
	}

}
