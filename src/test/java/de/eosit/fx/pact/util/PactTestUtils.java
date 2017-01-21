package de.eosit.fx.pact.util;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.UUID;

public class PactTestUtils {


    public static File writePactContent(File file) throws IOException {
        return writePactContent(file, getRandomPact());
    }
    
    public static File writePactContent(File file, String content) throws IOException {
        Files.write(file.toPath(), Lists.newArrayList(content), Charset.forName("UTF-8"));
        return file;
    }

    public static String getRandomPact() {
        return getPactString("a", "b", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    public static String getPactString(String provider, String consumer, String providerState, String description) {
        StringBuilder sb = new StringBuilder("{\"provider\": {\"name\": \"");
        sb.append(provider);
        sb.append("\"},\"consumer\": {\"name\": \"");
        sb.append(consumer);
        sb.append("b\"},");
        sb.append("\"interactions\": [{\"description\": \"");
        sb.append(description);
        sb.append("\",\"request\": {\"method\": \"GET\",\"path\": \"/to/service\"},");
        sb.append("\"response\": {\"status\": 200,\"body\": {}},\"providerState\": \"");
        sb.append(providerState);
        sb.append(
                "\"}],\"metadata\": {\"pact-specification\": {\"version\": \"3.0.0\"},\"pact-jvm\": {\"version\": \"3.2.10\"}}}");
        return sb.toString();
    }
}
