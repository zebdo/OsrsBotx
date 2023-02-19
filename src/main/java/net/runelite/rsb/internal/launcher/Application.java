package net.runelite.rsb.internal.launcher;

import net.runelite.client.modified.RuneLite;

public class Application {
    public static void main(final String[] args) throws Throwable {
        var rl = new RuneLite();
        rl.launch(args);
    }
}
