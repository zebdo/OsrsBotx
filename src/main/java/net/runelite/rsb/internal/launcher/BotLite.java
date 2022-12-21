package net.runelite.rsb.internal.launcher;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;

import net.runelite.client.game.ItemManager;
import net.runelite.client.modified.RuneLite;

import net.runelite.rsb.internal.ScriptHandler;
import net.runelite.rsb.internal.input.InputManager;

import net.runelite.rsb.internal.client_wrapper.RSClient;

import net.runelite.rsb.plugin.ScriptSelector;

import java.util.concurrent.Executors;

@Singleton
@Slf4j
public class BotLite extends RuneLite {

    private InputManager im;
    private ScriptHandler sh;
    private RSClient proxy;

    public InputManager getInputManager() {
        return im;
    }

    public ScriptHandler getScriptHandler() {
        return sh;
    }

	public RSClient getProxy() {
        return proxy;
    }

    public BotLite() throws Exception {
		sh = new ScriptHandler(this);

        Executors.newSingleThreadScheduledExecutor().submit(() -> {
            while (injector.getInstance(Client.class) == null) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

			proxy = new RSClient(injector.getInstance(Client.class),
								 injector.getInstance(ClientThread.class));

			im = new InputManager(this, proxy);

			});
    }

}
