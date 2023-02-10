package net.runelite.client.plugins.mousetrail;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(MouseTrailPlugin.GROUP)
public interface MouseTrailConfig extends Config {

    @Range(min = 2, max = 7)
    @ConfigItem(keyName = "curveSizeMultiplier", name = "Curve Size Multiplier", description = "Curve size multiplier", position = 1)
    default int curveSizeMultiplier() {
        return 2;
    }

    @Range(min = 10, max = 100)
    @ConfigItem(keyName = "curvePoints", name = "Curve points", description = "Curve points", position = 1)
    default int curvePoints() {
        return 50;
    }

    @ConfigItem(keyName = "theme", name = "Theme", description = "The color theme used for highlighting things.", position = 0)
    default Theme theme() {
        return Theme.COLOR_TEMP;
    }

    @ConfigItem(keyName = "whichMouseTrailStyle", name = "Mouse Trail Style", description = "Which trail style to use.", position = 16)
    default MouseTrailStyle whichMouseTrailStyle() {
        return MouseTrailStyle.NONE;
    }


    enum MouseTrailStyle {NONE, ENABLED}

}
