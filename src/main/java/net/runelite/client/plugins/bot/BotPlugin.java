package net.runelite.client.plugins.bot;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.rsb.internal.launcher.BotLite;

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

    private static ScriptPanel scriptPanel;

    public BotPlugin() {
    }

    @Provides
    BotConfig provideConfig(ConfigManager configManager) {
        return (BotConfig)configManager.getConfig(BotConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        BufferedImage icon = imageToBufferedImage(BotPlugin.class.getResourceAsStream("rsb.png"));

        BotLite bot = injector.getInstance(BotLite.class);
        scriptPanel = new ScriptPanel(bot);

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
