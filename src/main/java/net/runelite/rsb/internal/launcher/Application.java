package net.runelite.rsb.internal.launcher;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class Application {
    public static void main(final String[] args) throws Throwable {
        var b = new BotLite();
        b.launch(args);
    }
}
