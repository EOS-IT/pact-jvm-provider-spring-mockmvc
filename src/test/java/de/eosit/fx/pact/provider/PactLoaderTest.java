package de.eosit.fx.pact.provider;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Lists;

import au.com.dius.pact.model.Pact;
import groovy.json.JsonException;

public class PactLoaderTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File subfolder;

    @Before
    public void setup() throws Exception {
        writePactContent(tempFolder.newFile("1.json"));
        writePactContent(tempFolder.newFile("2.json"));
        writePactContent(tempFolder.newFile("3.txt"));
        subfolder = tempFolder.newFolder("subfolder");
        writePactContent(new File(subfolder, "4.json"));
        writePactContent(new File(subfolder, "5.txt"));
    }

    @Test
    public void loadPactByResource() throws Exception {
        Pact pact = PactLoader
                .loadPactByResource("file:" + new File(tempFolder.getRoot().getPath(), "1.json").getPath());
        Assert.assertNotNull(pact);

        pact = PactLoader.loadPactByResource("file:" + new File(subfolder.getPath(), "4.json").getPath());
        Assert.assertNotNull(pact);
    }

    @Test(expected = IllegalStateException.class)
    public void loadPactByResourceNotExisting() throws Exception {
        PactLoader.loadPactByResource("file:" + new File(tempFolder.getRoot().getPath(), "6.json").getPath());
    }

    @Test
    public void loadPactsByResourceFolderRecursive() throws Exception {
        List<Pact> pacts = PactLoader.loadPactsByResourceFolder("file:" + tempFolder.getRoot().getPath());
        Assert.assertEquals(3, pacts.size());

        pacts = PactLoader.loadPactsByResourceFolder("file:" + subfolder.getPath());
        Assert.assertEquals(1, pacts.size());
    }

    @Test
    public void loadPactsByResourceFolderNonRecursive() throws Exception {
        List<Pact> pacts = PactLoader.loadPactsByResourceFolder("file:" + tempFolder.getRoot().getPath(), false);
        Assert.assertEquals(2, pacts.size());

        pacts = PactLoader.loadPactsByResourceFolder("file:" + subfolder.getPath(), false);
        Assert.assertEquals(1, pacts.size());
    }

    @Test
    public void loadPactsByResourceFolderNotExisting() throws Exception {
        List<Pact> pacts = PactLoader.loadPactsByResourceFolder("file:" + subfolder.getPath() + "_temp");
        Assert.assertEquals(0, pacts.size());
    }

    @Test
    public void loadPactsByFileFolderRecursive() throws Exception {
        List<Pact> pacts = PactLoader.loadPactsByFile(tempFolder.getRoot());
        Assert.assertEquals(3, pacts.size());

        pacts = PactLoader.loadPactsByFile(subfolder);
        Assert.assertEquals(1, pacts.size());
    }

    @Test
    public void loadPactsByFileFolderNonRecursive() throws Exception {
        List<Pact> pacts = PactLoader.loadPactsByFile(tempFolder.getRoot(), false);
        Assert.assertEquals(2, pacts.size());

        pacts = PactLoader.loadPactsByFile(subfolder, false);
        Assert.assertEquals(1, pacts.size());
    }

    @Test
    public void loadPactsByFile() throws Exception {
        List<Pact> pacts = PactLoader.loadPactsByFile(new File(tempFolder.getRoot().getPath(), "1.json"));
        Assert.assertEquals(1, pacts.size());

        pacts = PactLoader.loadPactsByFile(new File(subfolder.getPath(), "4.json"));
        Assert.assertEquals(1, pacts.size());
    }

    @Test
    public void loadPactsByFileNonRecursive() throws Exception {
        List<Pact> pacts = PactLoader.loadPactsByFile(new File(tempFolder.getRoot().getPath(), "1.json"), false);
        Assert.assertEquals(1, pacts.size());

        pacts = PactLoader.loadPactsByFile(new File(subfolder.getPath(), "4.json"), false);
        Assert.assertEquals(1, pacts.size());
    }

    @Test
    public void loadPactsByFileNotExisting() throws Exception {
        List<Pact> pacts = PactLoader.loadPactsByFile(new File(tempFolder.getRoot().getPath(), "6.json"));
        Assert.assertEquals(0, pacts.size());
    }

    @Test
    public void loadPactsByFileNotExistingNonRecursive() throws Exception {
        List<Pact> pacts = PactLoader.loadPactsByFile(new File(tempFolder.getRoot().getPath(), "6.json"), false);
        Assert.assertEquals(0, pacts.size());
    }

    @Test
    public void loadPactGeneric() throws Exception {
        Pact pact = PactLoader.loadPactGeneric(new File(tempFolder.getRoot().getPath(), "1.json"));
        Assert.assertNotNull(pact);
    }

    @Test(expected = JsonException.class)
    public void loadPactGenericNotExisting() throws Exception {
        Pact pact = PactLoader.loadPactGeneric(new File(tempFolder.getRoot().getPath(), "6.json"));
        Assert.assertNotNull(pact);
    }

    @Test
    public void loadPactGenericFileString() throws Exception {
        Pact pact = PactLoader.loadPactGeneric(new File(tempFolder.getRoot().getPath(), "1.json").getPath());
        Assert.assertNotNull(pact);
    }

    @Test
    public void loadPactGenericFromPactJsonString() throws Exception {
        Pact pact = PactLoader.loadPactGeneric(getRandomPact());
        Assert.assertNotNull(pact);
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
