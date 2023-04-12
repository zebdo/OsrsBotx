		// KKK This is the virtual mouse cursor
        //BotLite bot = RuneLite.getInjector().getInstance(BotLite.class);
		//if (bot.getInputManager() != null) {
		//	graphics2d.setColor(Color.red);
	//		graphics2d.drawOval(bot.getInputManager().getX() - 7, bot.getInputManager().getY() - 7, 14, 14);
	//		graphics2d.fillOval(bot.getInputManager().getX() - 2, bot.getInputManager().getY() - 2, 4, 4);
	//	}


package net.runelite.client.plugins.bot;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;

import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

import net.runelite.rsb.internal.ScriptHandler;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@PluginDescriptor(
        name = "Bot panel",
        description = "Bot panel",
        loadWhenOutdated = true
)
@Slf4j
public class BotPlugin extends Plugin {

    @Inject
    private BotConfig config;

    @Inject
    private ClientToolbar clientToolbar;

    private NavigationButton navButton;

    private ScriptPanel scriptPanel;
    private ScriptHandler sh;

    public BotPlugin() {
    }

    @Provides
    BotConfig provideConfig(ConfigManager configManager) {
        return (BotConfig)configManager.getConfig(BotConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        log.info("BotPlugin startUp():");
        log.info("RUNELITE_DIR {}", RuneLite.RUNELITE_DIR);
        log.info("CACHE_DIR {}", RuneLite.CACHE_DIR);
        log.info("PLUGINS_DIR {}", RuneLite.PLUGINS_DIR);
        log.info("SCREENSHOT_DIR {}", RuneLite.SCREENSHOT_DIR);
        log.info("DIR {}", RuneLite.LOGS_DIR);
        log.info("USER_AGENT {}", RuneLite.USER_AGENT);

        this.sh = new ScriptHandler();

        BufferedImage icon = imageToBufferedImage(BotPlugin.class.getResourceAsStream("rsb.png"));

        scriptPanel = new ScriptPanel(sh);
        BotPanel panel = new BotPanel(scriptPanel);

        navButton = NavigationButton.builder()
                .tooltip("Botlite Interface")
                .icon(icon)
                .priority(10)
                .panel(panel)
                .build();
        clientToolbar = injector.getInstance(ClientToolbar.class);
        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown() {
        clientToolbar.removeNavigation(navButton);
    }

    public static BufferedImage imageToBufferedImage(InputStream is) throws IOException {
        Image im = ImageIO.read(is);
        BufferedImage bi = new BufferedImage
                (im.getWidth(null),im.getHeight(null),BufferedImage.TYPE_INT_RGB);
        Graphics bg = bi.getGraphics();
        bg.drawImage(im, 0, 0, null);
        bg.dispose();
        return bi;
    }

}
