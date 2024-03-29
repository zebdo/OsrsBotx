package net.runelite.rsb.script;

import lombok.extern.slf4j.Slf4j;
import net.runelite.rsb.internal.ScriptHandler;

@Slf4j
public abstract class Script implements Runnable {
    protected volatile boolean running = false;
    protected volatile boolean paused = false;

    protected ScriptHandler sh;

    // internal - should not propagate to end user
    public abstract void onInit();
    public abstract void doRun();

    // all called from ScriptHandler
    public final void init(ScriptHandler sh) {
        this.sh = sh;
        onInit();
    }

    public final void cleanup() {
        // moves it offscreen
        sh.stopScript();
    }

    public final void deactivate() {
        this.running = false;
        this.paused = false;
    }

    public final boolean togglePaused() {
        boolean result = paused;
        if (running) {
            this.paused = !paused;
        }

        return result;
    }

    public final void run() {
        this.doRun();
    }

}
