package de.eosit.fx.pact.provider;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
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

public class RequestBuilder {

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

    public static MockHttpServletRequestBuilder buildRequest(Request request) {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(decode(request.getPath()))
                .query(decode(toQuery(request.getQuery()))).build();

        return createBuilder(request, uriComponents);
    }

    private static MockHttpServletRequestBuilder createBuilder(Request request, UriComponents components) {
        MockHttpServletRequestBuilder builder = createBuilderByHttpMethod(request, components);
        buildReqBody(buildCookies(buildReqHeaders(builder, request), request), request);

        return builder;
    }

    private static String toQuery(Map<String, List<String>> map) {
        if (map == null) {
            return null;
        }

        return map.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue().stream().map(v -> decode(v)).collect(Collectors.joining(";")))
                .collect(Collectors.joining("&"));
    }

    private static String decode(String s) {
        if (s == null) {
            return null;
        }

        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
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
