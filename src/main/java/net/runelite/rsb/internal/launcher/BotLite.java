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
import net.runelite.rsb.internal.globval.GlobalConfiguration;
import net.runelite.rsb.internal.client_wrapper.RSClient;

import net.runelite.rsb.methods.MethodContext;
import net.runelite.rsb.plugin.ScriptSelector;

import net.runelite.rsb.wrappers.common.CacheProvider;

import java.applet.Applet;
import java.io.IOException;
import java.io.File;

import java.util.concurrent.Executors;

@Singleton
@Slf4j
@SuppressWarnings("removal")
public class BotLite extends RuneLite {

	// XXXX idea is not to have this on here
    private MethodContext ctx;

    private InputManager im;
    private ScriptHandler sh;
    private RSClient proxy;

    private Canvas canvas;

    public Client getClient() {
        return client = injector.getInstance(Client.class);
    }

    public Applet getApplet() {
		return applet = injector.getInstance(Applet.class);
	}

    public Applet getLoader() {
        return (Applet) this.getClient();
    }

    public ItemManager getItemManager() {
		return injector.getInstance(ItemManager.class);
	}

    public MethodContext getMethodContext() {
        return ctx;
    }

    public InputManager getInputManager() {
        return im;
    }

    public ScriptHandler getScriptHandler() {
        return sh;
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

	/**
	 * Checks if the cache exists and if it does, loads it
	 * if not it creates a new cache and saves it
	 *
	 * @throws IOException If the file isn't found or is inaccessible then an IOException has occurred.
	 */
	public void checkForCacheAndLoad() {
		try {
			String gameCacheLocation = GlobalConfiguration.Paths.getRuneLiteGameCacheDirectory();
			String objectCacheLocation = GlobalConfiguration.Paths.getObjectsCacheDirectory();
			String itemCacheLocation = GlobalConfiguration.Paths.getItemsCacheDirectory();
			String npcCacheLocation = GlobalConfiguration.Paths.getNPCsCacheDirectory();
			String spriteCacheLocation = GlobalConfiguration.Paths.getSpritesCacheDirectory();
			//TODO Some sort of better validation here
			//Add a version check
			if (!new File(itemCacheLocation).exists() && new File(itemCacheLocation).getTotalSpace() < 100) {
				String[] itemArgs = {"--cache", gameCacheLocation, "--items", itemCacheLocation};
				String[] objectArgs = {"--cache", gameCacheLocation, "--objects", objectCacheLocation};
				String[] npcArgs = {"--cache", gameCacheLocation, "--npcs", npcCacheLocation};
				String[] spriteArgs = {"--cache", gameCacheLocation, "--sprites", spriteCacheLocation};

				net.runelite.cache.Cache.main(itemArgs);
				net.runelite.cache.Cache.main(objectArgs);
				net.runelite.cache.Cache.main(npcArgs);

				if (!new File(spriteCacheLocation).exists()) {
					new File(spriteCacheLocation).mkdir();
					net.runelite.cache.Cache.main(spriteArgs);
				}
			} else {
				CacheProvider.fillFileCache();
			}
		} catch (Exception e) {
			log.warn("checkForCacheAndLoad failed " + e);
			e.printStackTrace();
		}
	}

    /**
     * The actual method associated with initializing the client-related data. Such as creating the client sizing and
     * binding the plethora of handlers, listeners, and managers to this particular RuneLite instance
     * (outside the injector binding)
     *
     * @throws Exception        Any exception the client, bot, or RuneLite might throw.
     */

	public void init() throws Exception {
		this.start();
	}

    public BotLite() throws Exception {
		this.checkForCacheAndLoad();
		sh = new ScriptHandler(this);

        Executors.newSingleThreadScheduledExecutor().submit(() -> {
            while (this.getClient() == null) {
			}

			im = new InputManager(this);
			proxy = new RSClient(injector.getInstance(Client.class),
								 injector.getInstance(ClientThread.class));

			ctx = new MethodContext(this, proxy);

			// starting thread
			final RemotePy py = new RemotePy(this, ctx, im, sh);
			py.startServer(2905);

			});
    }

}
