package net.runelite.rsb.botLauncher;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MainBufferProvider;
import net.runelite.client.game.ItemManager;
import net.runelite.client.modified.RuneLite;

import net.runelite.rsb.internal.ScriptHandler;
import net.runelite.rsb.internal.input.Canvas;
import net.runelite.rsb.internal.input.InputManager;

import net.runelite.rsb.methods.MethodContext;
import net.runelite.rsb.plugin.ScriptSelector;
import net.runelite.rsb.service.ScriptDefinition;
import java.applet.Applet;
import java.awt.*;
import java.awt.image.BufferedImage;

import java.util.concurrent.Executors;

@Singleton
@Slf4j
@SuppressWarnings("removal")
public class BotLite extends RuneLite implements BotLiteInterface {
    private MethodContext ctx;
    private Component panel;
    private BufferedImage backBuffer;
    private Image image;
    private InputManager im;
    private ScriptHandler sh;
    private Canvas canvas;

    /**
     * Whether or not user input is allowed despite a script's preference.
     */
    public volatile boolean overrideInput = false;

    /**
     * Whether or not rendering is enabled.
     */
    public volatile boolean disableRendering = false;

    /**
     * Whether or not the canvas is enabled.
     */
    public volatile boolean disableCanvas = false;

    /**
     * Set the canvas to the opposite state
     */
    public void changeCanvasState() {
        if (disableCanvas) {
            getLoader().setVisible(false);
            return;
        }
        getLoader().setVisible(true);
    }

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

    public Image getImage() {
        return image;
    }

    public BufferedImage getBackBuffer() {
        return backBuffer;
    }

    /**
     * Grabs the graphics visible on the canvas from the main buffer using the associated provider
     * @param mainBufferProvider    An object that provides the main buffer (canvas info) for this client instance
     * @return  The graphics of the Canvas
     */
    public Graphics getBufferGraphics(MainBufferProvider mainBufferProvider) {
        image = mainBufferProvider.getImage();
        Graphics back = mainBufferProvider.getImage().getGraphics();
        back.dispose();
        back.drawImage(backBuffer, 0, 0, null);
        return back;
    }

    public Component getPanel() {
        return this.panel;
    }

    public void setPanel(Component c) {
        this.panel = c;
    }

    /**
     * Returns the size of the panel that clients should be drawn into. For
     * internal use.
     *
     * @return The client panel size.
     */
    public Dimension getPanelSize() {
		if (this.getPanel() == null) {
			return null;
		}

		return this.getPanel().getSize();
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
     * Assigns this instance of the RuneLite (Bot) a method context for calling bot api methods
     * as well as assigns bank constants here.
     */
    public void setMethodContext() {
        ctx = new MethodContext(this);
        ctx.bank.assignConstants();
    }

    /**
     * Stops and shuts down the current bot instance
     */
    public void shutdown() {
        getLoader().stop();
        getLoader().setVisible(false);
        sh.stopScript();
    }

    public BotLite getInstance() {
        return this;
    }

    /**
     * The actual method associated with initializing the client-related data. Such as creating the client sizing and
     * binding the plethora of handlers, listeners, and managers to this particular RuneLite instance
     * (outside the injector binding)
     *
     * @param  startClientBare  Whether to launch the client without any additional initialization settings or not
     * @throws Exception        Any exception the client, bot, or RuneLite might throw.
     */
    public void init() throws Exception {
		this.start();
	}

    public BotLite() throws Exception {
        im = new InputManager(this);
        sh = new ScriptHandler(this);

        Executors.newSingleThreadScheduledExecutor().submit(() -> {
            while (this.getClient() == null) {
			}

            setMethodContext();
            if (getPanelSize() != null) {
                final Dimension size = getPanelSize();
                backBuffer = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
                image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
            }
        });
    }

    // public void runScript(String scriptName) {
    //     ScriptSelector ss = new ScriptSelector(this);
    //     ss.load();
    //     ScriptDefinition def = ss.getScripts().stream().
	// 		filter(x -> x.name.replace(" ", "").equals(scriptName))
	// 		.findFirst().get();
    //     try {
    //         getInjectorInstance().getScriptHandler().runScript(def.source.load(def));
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    // public void stopScript() {
    //     sh.stopScript();
    // }

}
