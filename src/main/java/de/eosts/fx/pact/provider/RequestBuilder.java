package de.eosts.fx.pact.provider;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import au.com.dius.pact.model.Interaction;
import au.com.dius.pact.model.Request;
import au.com.dius.pact.model.RequestResponseInteraction;

/**
 * Utility class converting pact classes into Springs
 * {@link MockHttpServletRequestBuilder}. The
 * {@link MockHttpServletRequestBuilder} can be used to build requests that can
 * be executed against a mocked rest interface.
 */
public class RequestBuilder {

    /**
     * Creates a {@link MockHttpServletRequestBuilder} from the given
     * {@link Interaction}. This method will only return a value if the
     * interaction contains a {@link Request}. This means that the interaction
     * must be a {@link RequestResponseInteraction}. If no valid request can be
     * extracted from the interaction an empty {@link Optional} is returned.
     *
     * @param interaction
     *            The {@link Interaction} to get the request from. The
     *            {@link Request} is used to build the
     *            {@link MockHttpServletRequestBuilder}.
     * @return Returns an {@link Optional} of
     *         {@link MockHttpServletRequestBuilder} containing the values from
     *         the {@link Request}. In case no valid request is found, an empty
     *         {@link Optional} is returned.
     */
    public static Optional<MockHttpServletRequestBuilder> buildRequest(Interaction interaction) {
        RequestResponseInteraction reqResInteraction;
        if (interaction instanceof RequestResponseInteraction) {
            reqResInteraction = (RequestResponseInteraction) interaction;
        } else {
            return Optional.empty();
        }

        Request request = reqResInteraction.getRequest();
        if (request == null) {
            throw new IllegalStateException(
                    "No request information available in the current interaction: " + interaction);
        }

        return Optional.of(buildRequest(request));
    }

    /**
     * Creates a {@link MockHttpServletRequestBuilder} from the given
     * {@link Request}.
     *
     * @param request
     *            The {@link Request} to build the
     *            {@link MockHttpServletRequestBuilder}.
     * @return Returns a {@link MockHttpServletRequestBuilder} containing the
     *         values from the {@link Request}. In case no request is given,
     *         <code>null</code> is returned.
     */
    public static MockHttpServletRequestBuilder buildRequest(Request request) {
        if (request == null) {
            return null;
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(request.getPath());
        toQuery(request.getQuery(), uriBuilder);

        UriComponents uriComponents = uriBuilder.build();

        return createBuilder(request, uriComponents);
    }

    private static MockHttpServletRequestBuilder createBuilder(Request request, UriComponents components) {
        MockHttpServletRequestBuilder builder = createBuilderByHttpMethod(request, components);
        buildReqBody(buildCookies(buildReqHeaders(builder, request), request), request);

        return builder;
    }

    private static void toQuery(Map<String, List<String>> map, UriComponentsBuilder builder) {
        if (map == null || builder == null) {
            return;
        }

        for (Entry<String, List<String>> e : map.entrySet()) {
            builder.queryParam(e.getKey(), e.getValue().toArray());
        }
    }

    private static MockHttpServletRequestBuilder createBuilderByHttpMethod(Request request, UriComponents components) {
        URI uri = components.toUri();
        String method = request.getMethod() != null ? request.getMethod().toLowerCase() : "";

        switch (method) {
        case "get":
            return MockMvcRequestBuilders.get(uri);
        case "post":
            return MockMvcRequestBuilders.post(uri);
        case "put":
            return MockMvcRequestBuilders.put(uri);
        case "delete":
            return MockMvcRequestBuilders.delete(uri);
        case "options":
            return MockMvcRequestBuilders.options(uri);
        case "head":
            return MockMvcRequestBuilders.head(uri);

        default:
            throw new UnsupportedOperationException("Can't handle http method: " + method);
        }
    }

    private static MockHttpServletRequestBuilder buildReqHeaders(MockHttpServletRequestBuilder builder,
            Request request) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null) {
            return builder;
        }

        headers.entrySet().stream().forEach(e -> builder.header(e.getKey(), e.getValue()));

        return builder;
    }

    private static MockHttpServletRequestBuilder buildReqBody(MockHttpServletRequestBuilder builder, Request request) {
        return builder.content(request.getBody().orElse(""));
    }

    private static MockHttpServletRequestBuilder buildCookies(MockHttpServletRequestBuilder builder, Request request) {
        List<String> cookies = request.cookie();

        if (cookies == null) {
            return builder;
        }

        List<Cookie> cookieList = cookies.stream().map(c -> toCookie(c)).filter(c -> c != null)
                .collect(Collectors.toList());

        builder.cookie(cookieList.toArray(new Cookie[cookieList.size()]));

        return builder;
    }

    private static Cookie toCookie(String cookieString) {
        if (cookieString == null || cookieString.isEmpty()) {
            return null;
        }

        String[] arr = cookieString.split("=", 2);
        if (arr.length == 2) {
            return new Cookie(arr[0], arr[1]);
        } else if (arr.length == 1) {
            return new Cookie(arr[0], null);
        } else {
            return null;
        }
    }
}
