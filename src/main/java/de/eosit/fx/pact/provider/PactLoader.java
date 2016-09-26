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

    /**
     * Loads the {@link Pact} from the given <code>pactResource</code>. The
     * <code>pactResource</code>-Parameter is provided in a form like:
     * <ul>
     * <li>"file:../path/to/pact/a.json" or "file:/temp/pactfolder/b.json" for
     * file system related paths</li>
     * <li>"classpath:pactfolder/c.json" for classpath related paths</li>
     * <li>"WEB_INF/pacts/d.json" for relative file system paths. This might not
     * be supported by the underlying implementation.</li>
     * </ul>
     *
     * @param pactResource
     *            The resource location in the format described above.
     * @return Returns the found {@link Pact} from the given resource.
     */
    public static Pact loadPactByResource(String pactResource) {
        Resource resource = new DefaultResourceLoader().getResource(pactResource);
        return PactReader.loadPact(resourceToInStream(resource));
    }

    /**
     * Loads all {@link Pact}s from "*.json" files within the given
     * <code>pactFolder</code> and all of its sub-folders recursively. The
     * <code>pactFolder</code>-Parameter is provided in a form like:
     * <ul>
     * <li>"file:../path/to/pact" or "file:/temp/pactfolder" for file system
     * related paths</li>
     * <li>"classpath:pactfolder" for classpath related paths</li>
     * <li>"WEB_INF/pacts" for relative file system paths. This might not be
     * supported by the underlying implementation.</li>
     * </ul>
     *
     * A call to this function is equivalent to call
     * <code>loadPactsByFile(pactFolder, true)</code>
     *
     * @param pactFolder
     *            The resource folder location in the format described above.
     * @return Returns the found {@link Pact}s within the given folder
     *         (including sub-folders).
     */
    public static List<Pact> loadPactsByResourceFolder(String pactFolder) {
        return loadPactsByResourceFolder(pactFolder, true);
    }

    /**
     * Loads all {@link Pact}s from "*.json" files within the given
     * <code>pactFolder</code>. The <code>recursive</code>-Parameter controls if
     * sub-folders are recursively scanned or not. The
     * <code>pactFolder</code>-Parameter is provided in a form like:
     * <ul>
     * <li>"file:../path/to/pact" or "file:/temp/pactfolder" for file system
     * related paths</li>
     * <li>"classpath:pactfolder" for classpath related paths</li>
     * <li>"WEB_INF/pacts" for relative file system paths. This might not be
     * supported by the underlying implementation.</li>
     * </ul>
     *
     * @param pactFolder
     *            The resource folder location in the format described above.
     * @param recursive
     *            Set to <code>true</code> if all sub-folders should be scanned
     *            for pact files recursively. If set to <code>false</code> only
     *            the "*.json" files within the specified folder are considered.
     * @return Returns the found {@link Pact}s within the given folder.
     */
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

    /**
     * Loads all {@link Pact}s from the given <code>file</code>. If the given
     * <code>file</code>-Parameter points to a real file it is loaded directly.
     * If it is a folder it will be scanned for "*.json" files. All of its
     * sub-folders are scanned recursively as well.
     *
     * A call to this function is equivalent to call
     * <code>loadPactsByFile(file, true)</code>
     *
     * @param file
     *            The file pointing to a pact file or a folder containing pact
     *            files.
     * @return Returns the found {@link Pact}s.
     */
    public static List<Pact> loadPactsByFile(File file) {
        return loadPactsByFile(file, true);
    }

    /**
     * Loads all {@link Pact}s from the given <code>file</code>. If the given
     * <code>file</code>-Parameter points to a real file it is loaded directly.
     * If it is a folder it will be scanned for "*.json" files. If the
     * <code>recursive</code>-Parameter is set to <code>true</code>, all of the
     * folders sub-folders are scanned recursively as well.
     *
     * @param file
     *            The file pointing to a pact file or a folder containing pact
     *            files.
     * @param recursive
     *            Set to <code>true</code> if all sub-folders should be scanned
     *            for pact files recursively. If set to <code>false</code> only
     *            the "*.json" files within the specified folder are considered.
     *            This parameter has only an effect if the given
     *            <code>file</code> is a folder.
     * @return Returns the found {@link Pact}s.
     */
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

    /**
     * Directly load the {@link Pact} by calling
     * {@link PactReader#loadPact(Object)}.
     *
     * @param source
     *            The source of the pact. This can be a URI, File or direct
     *            Pact-Json.
     * @return The loaded {@link Pact}.
     */
    public static Pact loadPactGeneric(Object source) {
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
