package net.runelite.rsb.internal.launcher;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;

import net.runelite.client.game.ItemManager;
import net.runelite.client.modified.RuneLite;

import net.runelite.rsb.internal.ScriptHandler;
import net.runelite.rsb.internal.input.Canvas;
import net.runelite.rsb.internal.input.InputManager;

import net.runelite.rsb.internal.client_wrapper.RSClient;

import net.runelite.rsb.plugin.ScriptSelector;

import java.applet.Applet;

import java.util.concurrent.Executors;

@Singleton
@Slf4j
@SuppressWarnings("removal")
public class BotLite extends RuneLite {

    private InputManager im;
    private ScriptHandler sh;
    private RSClient proxy;

    private Canvas canvas;

    public Client getClient() {
        return client = injector.getInstance(Client.class);
    }
	// XXX used in inventory code to get selected item
	// XXX can't we just runelite api?
    public ItemManager getItemManager() {
		return injector.getInstance(ItemManager.class);
	}

    public InputManager getInputManager() {
        return im;
    }

    public ScriptHandler getScriptHandler() {
        return sh;
    }

	public RSClient getProxy() {
        return proxy;
    }

    /**
     * Gets the canvas object while checking to make sure we don't do this before it has actually
     * loaded
     * @return  The Canvas if the client is loaded otherwise null
     */
    public Canvas getCanvas() {
        if (client == null) {
            return null;
        }

        if (client.getCanvas() == null) {
            return null;
        }

        if (canvas == null) {
            canvas = new Canvas(client.getCanvas());
            return canvas;
        }

        return canvas;
    }

    public BotLite() throws Exception {
		sh = new ScriptHandler(this);

        Executors.newSingleThreadScheduledExecutor().submit(() -> {
            while (this.getClient() == null) {
			}

			proxy = new RSClient(injector.getInstance(Client.class),
								 injector.getInstance(ClientThread.class));

			im = new InputManager(this, proxy);

			});
    }

}
