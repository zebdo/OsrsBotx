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

/**
 * @author GigiaJ
 */
@Slf4j
public class FileScriptSource implements ScriptSource {

	private File file;

	public FileScriptSource(File file) {
		this.file = file;
	}

	public List<ScriptDefinition> list() {
		LinkedList<ScriptDefinition> scriptDefinitions = new LinkedList<>();
		if (file != null) {
			if (file.isDirectory()) {
				try {
					ClassLoader scriptLoader = new ScriptClassLoader(file.toURI().toURL());
					for (File file : Objects.requireNonNull(file.listFiles())) {
						if (isJar(file)) {
							log.warn(String.format("guess we get here! %s",file));
							load(new ScriptClassLoader(getJarUrl(file)), scriptDefinitions, new JarFile(file));
						} else {
							load(scriptLoader, scriptDefinitions, file, "");
						}
					}
				} catch (IOException ioEx) {
					log.debug("Failed to list files", ioEx);
				}
			} else if (isJar(file)) {
				try {
					ClassLoader scriptLoader = new ScriptClassLoader(getJarUrl(file));
					load(scriptLoader, scriptDefinitions, new JarFile(file));
				} catch (IOException ioEx) {
					log.debug("Failed to list files", ioEx);
				}
			}
		}

		return scriptDefinitions;
	}


	public Script load(ScriptDefinition def) throws ServiceException {
		log.warn("not here X!");
		if (!(def instanceof FileScriptDefinition fsd)) {
			throw new IllegalArgumentException("Invalid definition!");
		}
		try {
			return fsd.clazz.asSubclass(Script.class).newInstance();
		} catch (Exception ex) {
			throw new ServiceException(ex.toString());
		}
	}

	private void load(ClassLoader loader, LinkedList<ScriptDefinition> scripts, JarFile jar) {
		log.warn("guess we get here 2!");
		Enumeration<JarEntry> entries = jar.entries();
		while (entries.hasMoreElements()) {
			JarEntry e = entries.nextElement();
			String name = e.getName().replace('/', '.');
			String ext = ".class";
			if (name.endsWith(ext) && !name.contains("$")) {
				load(loader, scripts, name.substring(0, name.length() - ext.length()));
			}
		}
	}

	private void load(ClassLoader loader, LinkedList<ScriptDefinition> scripts, File file, String prefix) {
		log.warn("not here 1!");
		if (file.isDirectory()) {
			if (!file.getName().startsWith(".")) {
				for (File f : file.listFiles()) {
					load(loader, scripts, f, prefix + file.getName() + ".");
				}
			}
		} else {
			String name = prefix + file.getName();
			String ext = ".class";
			if (name.endsWith(ext) && !name.startsWith(".") && !name.contains("!") && !name.contains("$")) {
				name = name.substring(0, name.length() - ext.length());
				load(loader, scripts, name);
			}
		}
	}

	private void load(ClassLoader loader, LinkedList<ScriptDefinition> scripts, String name) {
		log.warn("and now here {}", name);

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
			log.warn("and we add to our scripts here (well the defintion) {}", name);
			FileScriptDefinition def = new FileScriptDefinition();
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

	private static class FileScriptDefinition extends ScriptDefinition {
		Class<?> clazz;
	}

	public String toString() {
		return this.file.getAbsolutePath();
	}

}
