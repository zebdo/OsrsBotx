package net.runelite.rsb.internal.launcher;

import lombok.extern.slf4j.Slf4j;
import net.runelite.rsb.internal.globval.GlobalConfiguration;

import java.util.*;

@Slf4j
public class Application {

	private static BotLite theBot;
	private static ArgumentPreParser preParser;

	public static void main(final String[] args) throws Throwable {
		preParser = new ArgumentPreParser(args);
		if (preParser.pop("--bot-runelite")) {
			theBot = new BotLite();
			theBot.launch(preParser.toArray());
		} else {
			assert(false);
			net.runelite.client.RuneLite.main(args);
		}
	}

	public static BotLite getBot() {
		return theBot;
	}

	/**
	 * A class to handle bot related arguments before passing them off to RuneLite
	 */
	private static class ArgumentPreParser {

		private ArrayList<String> preArgs;
		public ArgumentPreParser(String[] args) {
			this.preArgs = new ArrayList<>(Arrays.asList(args));
		}

		public String[] toArray() {
			return preArgs.toArray(new String[0]);
		}

		public boolean pop(Object o) {
			int index = preArgs.indexOf(o);
			boolean within = index >= 0;
			if (within) {
				preArgs.remove(index);
			}
			return within;
		}
	}
}
