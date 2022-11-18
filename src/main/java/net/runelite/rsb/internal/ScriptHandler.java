package net.runelite.rsb.internal;

import lombok.extern.slf4j.Slf4j;

import net.runelite.rsb.script.Script;
import net.runelite.rsb.script.ScriptManifest;
import net.runelite.rsb.internal.launcher.BotLite;

import java.util.*;

@Slf4j
public class ScriptHandler {
	private Script theScript;
	private Thread scriptThread;
	private final BotLite bot;

	public ScriptHandler(BotLite bot) {
		this.bot = bot;
	}

	public boolean scriptRunning() {
		return theScript != null;
	}

	public boolean pauseScript() {
		if (theScript != null) {
			theScript.setPaused(!theScript.isPaused());
			log.info("pauseScript(): script was paused {}", theScript.isPaused());
			return true;
		}

		log.info("pauseScript(): no script running");
		return false;
	}

	public void runScript(Script script) {
		if (theScript == null) {
			script.init(bot.getMethodContext());

			Thread t = new Thread(script, "SCRIPT");
			theScript = script;
			scriptThread = t;
			t.start();

			ScriptManifest prop = script.getClass().getAnnotation(ScriptManifest.class);
			log.info("starting new script/thread: {}", prop.name());

		} else {
			ScriptManifest prop = theScript.getClass().getAnnotation(ScriptManifest.class);
			log.info("runScript(): already running: {}", prop.name());
		}
	}

	public void stopScript() {
		if (theScript != null) {
			ScriptManifest prop = theScript.getClass().getAnnotation(ScriptManifest.class);
			log.info("script is running: {}", prop.name());

			Thread curThread = Thread.currentThread();
			if (curThread == scriptThread) {
				if (theScript != null) {
					log.info("stopping thread:", prop.name());
					theScript.deactivate();
					theScript = null;
					return;
				}
			} else {
				// will stop eventually... but...
				theScript.stopScript();
			}

			if (curThread == null) {
				log.info("Doing ThreadDeath");
				throw new ThreadDeath();
			}
		} else {
			log.info("No script running");
		}
	}

}
