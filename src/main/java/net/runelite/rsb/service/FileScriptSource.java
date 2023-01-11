package net.runelite.rsb.service;

import lombok.extern.slf4j.Slf4j;
import net.runelite.rsb.script.Script;
import net.runelite.rsb.script.ScriptManifest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.lang.reflect.InvocationTargetException;

/**
 * @author GigiaJ
 */
@Slf4j
public class FileScriptSource {

    private File file;

    public FileScriptSource(File file) {
        this.file = file;
    }

    public List<ScriptDefinition> list() {
        LinkedList<ScriptDefinition> scriptDefinitions = new LinkedList<>();
        assert (file != null);
        assert (file.isDirectory());

        try {
            ClassLoader scriptLoader = new ScriptClassLoader(file.toURI().toURL());
            for (File file : Objects.requireNonNull(file.listFiles())) {
                assert(isJar(file));
                load(new ScriptClassLoader(getJarUrl(file)), scriptDefinitions, new JarFile(file));
            }
        } catch (IOException ioEx) {
            log.debug("Failed to list files", ioEx);
            ioEx.printStackTrace();
        }

        scriptDefinitions.sort((x, y) -> x.name.compareTo(y.name));

        return scriptDefinitions;
    }

    public Script instantiate(ScriptDefinition def) {
        try {
            return def.clazz.asSubclass(Script.class).getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void load(ClassLoader loader, LinkedList<ScriptDefinition> scripts, JarFile jar) {
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry e = entries.nextElement();
            String name = e.getName().replace('/', '.');
            String ext = ".class";
            if (name.endsWith(ext) && !name.contains("$")) {
                loadClazz(loader, scripts, name.substring(0, name.length() - ext.length()));
            }
        }
    }

    private void loadClazz(ClassLoader loader, LinkedList<ScriptDefinition> scripts, String name) {
        Class<?> clazz;
        try {
            clazz = loader.loadClass(name);
        } catch (Exception ex) {
            log.warn("Exception occurred " + name + " is not a valid script and was ignored!", ex);
            return;
        } catch (VerifyError verEx) {
            log.warn("VerifyError exception occurred " + name + " is not a valid script and was ignored!", verEx);
            return;
        }

        if (clazz.isAnnotationPresent(ScriptManifest.class)) {
            ScriptDefinition def = new ScriptDefinition();
            ScriptManifest manifest = clazz.getAnnotation(ScriptManifest.class);
            def.name = manifest.name();
            def.authors = manifest.authors();
            def.clazz = clazz;
            def.source = this;
            scripts.add(def);
        }
    }

    private boolean isJar(File file) {
        return file.getName().endsWith(".jar") || file.getName().endsWith(".dat");
    }

    private URL getJarUrl(File file) throws IOException {
        URL url = file.toURI().toURL();
        url = new URL("jar:" + url.toExternalForm() + "!/");
        return url;
    }

}
