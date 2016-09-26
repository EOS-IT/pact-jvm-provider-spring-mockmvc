package de.eosit.fx.pact.provider;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Lists;

import au.com.dius.pact.model.Pact;

public class PactLoaderTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void getResources() throws Exception {
        writePactContent(tempFolder.newFile("1.json"));
        writePactContent(tempFolder.newFile("2.json"));
        writePactContent(tempFolder.newFile("3.txt"));
        File subfolder = tempFolder.newFolder("subfolder");
        writePactContent(new File(subfolder, "4.json"));
        writePactContent(new File(subfolder, "5.txt"));

        List<Pact> pacts = PactLoader.loadPactsByResourceFolder("file:" + tempFolder.getRoot().getPath());
        Assert.assertEquals(3, pacts.size());
    }

    private File writePactContent(File file) throws IOException {
        Files.write(file.toPath(), Lists.newArrayList(getRandomPact()), Charset.forName("UTF-8"));
        return file;
    }

    private String getRandomPact() {
        StringBuilder sb = new StringBuilder("{\"provider\": {\"name\": \"a\"},\"consumer\": {\"name\": \"b\"},");
        sb.append(
                "\"interactions\": [{\"description\": \"a dummy request\",\"request\": {\"method\": \"GET\",\"path\": \"/to/service\"},");
        sb.append("\"response\": {\"status\": 200,\"body\": {}},\"providerState\": \"");
        sb.append(UUID.randomUUID());
        sb.append(
                "\"}],\"metadata\": {\"pact-specification\": {\"version\": \"3.0.0\"},\"pact-jvm\": {\"version\": \"3.2.10\"}}}");
        return sb.toString();
    }
}
