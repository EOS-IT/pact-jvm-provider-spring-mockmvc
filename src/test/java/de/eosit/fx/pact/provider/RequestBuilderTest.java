package de.eosit.fx.pact.provider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import au.com.dius.pact.model.OptionalBody;
import au.com.dius.pact.model.Request;
import au.com.dius.pact.model.RequestResponseInteraction;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class RequestBuilderTest {

    private static final String REQUEST_METHOD = "GET";
    private static final String REQUEST_PATH = "/to/service";
    private static final String QUERY_LIST_PARAM = "argList";
    private static final String LIST_VALUE_1 = "item1";
    private static final String LIST_VALUE_2 = "item2";
    private static final String LIST_VALUE_3 = "lastitem";
    private static final String QUERY_PARAM = "arg2";
    private static final String PARAM_VALUE = encode("v%a=l?u&e");
    private static final String HEADER_KEY = "HEADER_KEY";
    private static final String HEADER_VALUE = "header_value";
    private static final String COOKIE_KEY_1 = "TOKEN";
    private static final String COOKIE_VAL_1 = "abc";
    private static final String COOKIE_KEY_2 = "session";
    private static final String COOKIE_VAL_2 = "def";
    private static final String COOKIE_STRING = COOKIE_KEY_1 + "=" + COOKIE_VAL_1 + ";" + COOKIE_KEY_2 + "="
            + COOKIE_VAL_2;

    private RequestResponseInteraction interaction;

    @Before
    public void setUp() throws Exception {
        interaction = new RequestResponseInteraction("a dummy request", "state");

        Request request = new Request();
        request.setBody(OptionalBody.nullBody());
        request.setMethod(REQUEST_METHOD);
        request.setPath(REQUEST_PATH);
        request.setQuery(newHashMap());
        request.getQuery().put(QUERY_LIST_PARAM, newArrayList(LIST_VALUE_1, LIST_VALUE_2, LIST_VALUE_3));
        request.getQuery().put(QUERY_PARAM, newArrayList(PARAM_VALUE));
        request.setHeaders(newHashMap());
        request.getHeaders().put(HEADER_KEY, HEADER_VALUE);
        request.getHeaders().put("cookie", COOKIE_STRING);
        interaction.setRequest(request);
    }

    @Test
    public void buildRequest() {
        Optional<MockHttpServletRequestBuilder> request = RequestBuilder.buildRequest(interaction);
        Assert.assertNotNull(request);
        Assert.assertTrue(request.isPresent());
        MockHttpServletRequest req = request.get().buildRequest(null);

        Assert.assertNotNull(req);
        Assert.assertEquals(HEADER_VALUE, req.getHeader(HEADER_KEY));
        Assert.assertEquals(REQUEST_METHOD, req.getMethod());
        Assert.assertEquals(3, req.getParameterMap().get(QUERY_LIST_PARAM).length);
        Assert.assertEquals(LIST_VALUE_1, req.getParameterMap().get(QUERY_LIST_PARAM)[0]);
        Assert.assertEquals(LIST_VALUE_2, req.getParameterMap().get(QUERY_LIST_PARAM)[1]);
        Assert.assertEquals(LIST_VALUE_3, req.getParameterMap().get(QUERY_LIST_PARAM)[2]);
        Assert.assertEquals(1, req.getParameterMap().get(QUERY_PARAM).length);
        Assert.assertEquals(PARAM_VALUE, req.getParameterMap().get(QUERY_PARAM)[0]);
        Assert.assertEquals(REQUEST_PATH, req.getPathInfo());
        Assert.assertEquals(2, req.getCookies().length);
        Assert.assertEquals(COOKIE_KEY_1, req.getCookies()[0].getName());
        Assert.assertEquals(COOKIE_VAL_1, req.getCookies()[0].getValue());
        Assert.assertEquals(COOKIE_KEY_2, req.getCookies()[1].getName());
        Assert.assertEquals(COOKIE_VAL_2, req.getCookies()[1].getValue());
    }

    @Test
    public void buildRequestForNullInteraction() {
        interaction = null;
        Optional<MockHttpServletRequestBuilder> request = RequestBuilder.buildRequest(interaction);
        Assert.assertNotNull(request);
        Assert.assertFalse(request.isPresent());
    }

    @Test
    public void buildRequestForNullRequest() {
        MockHttpServletRequestBuilder request = RequestBuilder.buildRequest((Request) null);
        Assert.assertNull(request);
    }

    private static String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
