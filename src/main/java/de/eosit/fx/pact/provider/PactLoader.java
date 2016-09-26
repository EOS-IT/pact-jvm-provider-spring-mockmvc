package de.eosit.fx.pact.provider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;

import au.com.dius.pact.model.Pact;
import au.com.dius.pact.model.PactReader;

import static com.google.common.collect.Lists.newArrayList;

public class PactLoader {

    private static final String FILE_EXTENSION = ".json";

    public static Pact loadPactByResource(String pactResource) {
        Resource resource = new DefaultResourceLoader().getResource(pactResource);
        return PactReader.loadPact(resourceToInStream(resource));
    }

    public static List<Pact> loadPactsByResourceFolder(String pactFolder) {
        return loadPactsByResourceFolder(pactFolder, true);
    }

    public static List<Pact> loadPactsByResourceFolder(String pactFolder, boolean recursive) {
        StringBuilder locationPattern = new StringBuilder(pactFolder);
        if (recursive) {
            locationPattern.append("/**");
        }
        locationPattern.append("/*");
        locationPattern.append(FILE_EXTENSION);

        Resource[] resources;
        try {
            resources = ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader())
                    .getResources(locationPattern.toString());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load pact pactresources matching: " + locationPattern, e);
        }
        return Arrays.stream(resources).map(r -> PactReader.loadPact(resourceToInStream(r)))
                .collect(Collectors.toList());
    }

    public static List<Pact> loadPactsByFile(File file) {
        return loadPactsByFile(file, true);
    }

    public static List<Pact> loadPactsByFile(File file, boolean recursive) {
        if (file == null || !file.exists()) {
            return newArrayList();
        }

        if (file.isFile()) {
            return newArrayList(PactReader.loadPact(file));
        }

        File[] files = file
                .listFiles(f -> (f.isDirectory() && recursive) || (f.isFile() && f.getName().endsWith(FILE_EXTENSION)));
        if (files != null) {
            return Arrays.stream(files).flatMap(f -> loadPactsByFile(f, recursive).stream())
                    .collect(Collectors.toList());
        }

        return newArrayList();
    }

    public Pact loadPactGeneric(Object source) {
        return PactReader.loadPact(source);
    }

    private static InputStream resourceToInStream(Resource resource) {
        try {
            return resource.getInputStream();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load pact file: " + resource.getDescription(), e);
        }
    }
}
